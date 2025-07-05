import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.CuisineType;
import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import it.uninsubria.laboratorioa.utils.Constants;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParsingTest {

    private static final Random rd = new Random();

    /**
     * Ottiene i dati relativi alla posizione del ristorante dai dati csv
     *
     * @param address
     * @param location
     * @return Array ordinato -> [città,nazione,indirizzo completo]
     */
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

    public static String getNationalPrefix(String phoneNumber, Nation nation) {
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



    /**
     * index:      0	   1		2     3	      4		   5		6		  7		   8	  9		  10	  11			12					13
     * field csv: Name,Address,Location,Price,Cuisine,Longitude,Latitude,PhoneNumber,Url,WebsiteUrl,Award,GreenStar,FacilitiesAndServices,Description
     *
     * @return Dataset letto come collection di ristoranti.
     */
    private static List<Restaurant> parseFromDataset() {
        List<Restaurant> companies = new ArrayList<>();

        File f = new File(Constants.ROOT, "michelin_my_maps.csv");
        try (Stream<String> lines = Files.lines(f.toPath())) {
            List<String> t = new ArrayList<>();

            lines.skip(1) // Optional: skip header
                    .map(line -> line.split(";"))
                    .forEach(fields -> {

                        // Lettura dati posizione
                        Location loc = null;
                        try {
                            // 0: city , 1:nation , 2: address
                            String[] locData = retrieveLocData(fields[1], fields[2]);

                            loc = new Location(
                                    Nation.valueOf(locData[1]),
                                    locData[0],
                                    Double.parseDouble(fields[5]),
                                    Double.parseDouble(fields[6]),
                                    locData[2]
                            );

                        } catch (Exception e) {
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


                        String nationalPrefix = getNationalPrefix(fields[7],loc.getNation());

                        String awardRaw = fields[10].trim().replaceAll("\"", "");
                        String greenStarRaw = fields[11].trim().replaceAll("\"", "");

// Award parsing
                        Award award = Award.NONE;
                        try {
                            if (!awardRaw.isEmpty() && !awardRaw.equals("0")) {
                                String[] awardParts = awardRaw.split(" ");
                                int awardVal = Integer.parseInt(awardParts[0]);
                                award = Award.fromInt(awardVal);
                            }
                        } catch (Exception ignored) {}

// GreenStar parsing
                        boolean greenStar = false;
                        try {
                            greenStar = greenStarRaw.equals("1") || greenStarRaw.equalsIgnoreCase("true");
                        } catch (Exception ignored) {}


                        Restaurant c = new Restaurant(
                                fields[0], // Name
                                fields[13], // Description
                                fields[9],  // WebsiteUrl
                                nationalPrefix + fields[7], // Phone with prefix
                                loc,
                                PriceRange.byDollarAmount(fields[3].length()),
                                rd.nextBoolean(), // Delivery flag
                                rd.nextBoolean(), // OnlineBooking flag
                                cuisineTypes,
                                new HashMap<>(), // Empty reviews
                                award,
                                greenStar
                        );


                        companies.add(c);
                    });

        } catch (IOException e) {
            System.out.println("Error while parsing csv database");
        }

        return companies;
    }


    @SneakyThrows
    public static void main(String[] args) {
        List<Restaurant> d = parseFromDataset();

        //d.get(1).save();
//        for (Restaurant c : d) System.out.println(c);

        System.out.println(d.size());

        Restaurant r = d.get(rd.nextInt(d.size())+1);
        System.out.println(r);

        r.save();
        //new DataHandler().loadFromFile();
    }

}
