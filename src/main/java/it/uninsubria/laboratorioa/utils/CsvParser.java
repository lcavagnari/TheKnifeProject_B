package it.uninsubria.laboratorioa.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.CuisineType;
import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import it.uninsubria.laboratorioa.ui.IO;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class CsvParser {

    private final static SecureRandom rd = new SecureRandom();

    private static String[] retrieveLocData(String address, String location) {
        String[] cityAndNation = location.split(",");
        String city;
        String nation;

        // Città con nomi particolari
        if (cityAndNation.length > 2) {
            StringBuilder tmp = new StringBuilder();
            for (int i = 0; i < cityAndNation.length - 2; i++) tmp.append(cityAndNation[i]);

            city = tmp.toString();
            nation = cityAndNation[cityAndNation.length - 1];

            // Nome città mancante, ottenerlo dall'indirizzo
        } else if (cityAndNation.length == 1) {
            String[] fields = address.split(",");
            int N = fields.length;

            nation = cityAndNation[0];

            // Mitigazione delle inconsistenze nell'indice relativo alla città
            city = (N >= 3 && fields[N - 2].matches("\\d+")) ? fields[N - 3] : fields[N - 2];

            // Caso base; città e nazione separate da ','
        } else {
            city = cityAndNation[0];
            nation = cityAndNation[1];
        }

        String[] tmp = address.split(",");
        address = address.replaceAll(city + "|" + nation + "|" + tmp[tmp.length - 1], "")
                .replace(", ,", ",")
                .replaceAll(",$", "");

        // 0:nation , 1: city , 2:address
        return new String[]{
                city,
                nation.trim().replaceAll("[\\s\\-]", "_").toUpperCase(),
                address
        };
    }


    private static String getNationalPrefix(String phoneNumber, Nation nation) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String regionCode = nation.getIsoCode();
        if (regionCode == null || regionCode.isEmpty()) return "";

        try {
            Phonenumber.PhoneNumber parsed = phoneUtil.parse(phoneNumber, regionCode);
            int countryCode = parsed.getCountryCode();
            return "+" + countryCode;
        } catch (NumberParseException e) {
            return "";
        }
    }


    private static Restaurant createRestaurant(String[] fields) {
        // Lettura dati posizione
        Location location = null;
        try {
            // 0: city , 1:nation , 2: address
            String[] locData = retrieveLocData(fields[1], fields[2]);

            location = new Location(
                    Nation.valueOf(locData[1]),
                    locData[0],
                    Double.parseDouble(fields[5]),
                    Double.parseDouble(fields[6]),
                    locData[2]
            );

        } catch (Exception ignored) {
        }

        // stili di cucina
        String[] cuisines = fields[4].split(", ");
        Set<CuisineType> cuisineTypes = new HashSet<>();

        for (String c : cuisines) {
            String[] parts = c.toUpperCase()
                    .replace(" CUISINE", "")
                    .replace(" INFLUENCES", "")
                    .replace("-", "_")
                    .replace("&", "AND")
                    .split(",|AND");

            for (String part : parts) {
                String key = part.trim().replace(" ", "_");
                try {
                    cuisineTypes.add(CuisineType.valueOf(key));
                } catch (IllegalArgumentException ignored) {
                    // Skip unknown values
                }
            }
        }


        // Accessi e servizi

        Set<String> services = new HashSet<>();
        if (fields.length >= 14 && !fields[13].isBlank()) {
            String[] tmp = fields[13].split(",");

            services = Arrays.stream(tmp).filter(Objects::nonNull).collect(Collectors.toSet());
        }

        // Ottieni il prefisso nazionale per il numero
        String nationalPrefix = "";
        if (location != null)
            nationalPrefix = getNationalPrefix(fields[7], location.getNation());


        // Ottieni il numero di stelle
        Award award = Award.NONE;
        try {
            String rating = fields[10].toLowerCase();
            if (rating.matches("[0-9].*")) {
                int val = Integer.parseInt(rating.replaceAll(" \\w*", ""));
                award = Award.fromInt(val);

            } else award = Award.valueOf(rating.toUpperCase().replace(" ", "_"));

        } catch (NumberFormatException ignored) {
        }


        // GreenStar parsing

        boolean greenStar = false;
        try {
            greenStar = Integer.parseInt(fields[11]) == 1;

        } catch (NumberFormatException ignored) {
        }


        // Costruzione dell'oggetto

        Restaurant c = new Restaurant(,
                // Nome
                fields[0],                                         // Descrizione
                fields[13],                                          // Url pagina web
                fields[9],
                null,                          // Contatto telefonico con prefisso nazionale
                nationalPrefix + fields[7],                                           // Posizione geografica
                location,       // Fascia di prezzo
                PriceRange.byDollarAmount(fields[3].length()),                                   // Disponibilità alla consegna a domicilio
                rd.nextBoolean(),                                   // Disponibilità per la prenotazione online
                rd.nextBoolean(),                                              // Stelle Michelin
                award,                                           // "Green Star", certificato Michelin di sostenibilità
                greenStar,                                        // Stili culinari offerti
                cuisineTypes,                                             // Servizi offerti o regole
                services);


        return c;
    }



    private final static ObjectMapper mapper = new ObjectMapper();
    /**
     * index:      0	   1		2     3	      4		   5		6		  7		   8	  9		  10	  11			12					13   <br>
     * field csv: Name,Address,Location,Price,Cuisine,Longitude,Latitude,PhoneNumber,Url,WebsiteUrl,Award,GreenStar,FacilitiesAndServices,Description
     * <br>
     *
     */
    public static void parseFromDataset(String path) {
        File f;
        try {
            f = new File(Paths.get(path).toUri());

            if (!f.exists() || !f.isFile() || !f.getName().endsWith(".csv")) {
                IO.printErrorMessage("File or path "+path+" does not exist, check and try again.");
                return;
            }
        } catch (Exception ignored) {
            IO.printErrorMessage("File or path "+path+" does not exist, check and try again.");
            return;
        }

        ArrayNode restaurants = mapper.createArrayNode();
        try (Stream<String> lines = Files.lines(f.toPath())) {
            List<String> t = new ArrayList<>();
            lines.skip(1) // Optional: skip header
                    .map(line -> line.split(";"))
                    .forEach(fields -> {
                        try {
                            Restaurant r = createRestaurant(fields);

                            r.build();
                            restaurants.add(r.getJsonObject());

                        } catch (Exception ignored) {}
                    });

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(Constants.ROOT,"michelin_my_maps.json"), restaurants);

        } catch (IOException | SecurityException e) {
            System.out.println("Error while parsing csv database: " + e);
        }
    }
}
