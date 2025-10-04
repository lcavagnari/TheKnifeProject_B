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
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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

        nation = nation.trim()
                .replaceAll("[\\s\\-]", "_")
                .toUpperCase();


        // 0:nation , 1: city , 2:address
        return new String[]{
                city,
                nation,
                address
        };
    }

    private static Location createLocation(String[] locData, String[] fields) {
        if (locData == null) return null;

        Nation nation;
        String nationName = null;
        try {
            nationName = locData[1].trim().toUpperCase()
                    .replace("_MAINLAND", "");

            nation = Nation.valueOf(nationName);

        } catch (IllegalArgumentException ex) {
            nation = Nation.fromString(nationName);
            if (nation == null) {
                IO.printErrorMessage("Errore creazione location: " + ex.getMessage());
                IO.printErrorMessage("loc data " + Arrays.toString(locData));
                IO.printErrorMessage(fields[6] + " " + fields[5]);
                return null;
            }
        }

        try {
            // locData: 0=city, 1=nation, 2=address
            return new Location(
                    nation,
                    locData[0],
                    Double.parseDouble(fields[5]),
                    Double.parseDouble(fields[6]),
                    locData[2]
            );
        } catch (Exception ex) {
            IO.printErrorMessage("Errore creazione location: " + ex.getMessage());
            IO.printErrorMessage("loc data " + Arrays.toString(locData));
            IO.printErrorMessage(fields[6] + " " + fields[5]);
            return null;
        }
    }

    private static String getNationalPrefix(String phoneNumber, Nation nation) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String regionCode = nation.getIsoCode();
        if (regionCode == null || phoneNumber == null || regionCode.isEmpty() || phoneNumber.isBlank()) return "";

        try {
            Phonenumber.PhoneNumber parsed = phoneUtil.parse(phoneNumber, regionCode);
            int countryCode = parsed.getCountryCode();
            return "+" + countryCode;
        } catch (NumberParseException e) {
            return "";
        }
    }



    // TODO: Sistemare qui la gestione degli input a cazzo nel dataset del diopo-
    private static Set<String> parseServices(String facilitiesAndServices) {
        Set<String> services = new HashSet<>();

        if (facilitiesAndServices != null && !facilitiesAndServices.isBlank()) {
            String[] serviceArray = facilitiesAndServices.split(",");
            services = Arrays.stream(serviceArray)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .collect(Collectors.toSet());
        }

        return services;
    }

    private static Award parseAward(String ratingField) {
        try {
            String rating = ratingField.toLowerCase();
            if (rating.matches("[0-9].*")) {
                int val = Integer.parseInt(rating.replaceAll(" \\w*", ""));
                return Award.fromInt(val);
            } else {
                return Award.valueOf(rating.toUpperCase().replace(" ", "_"));
            }
        } catch (NumberFormatException ex) {
            return Award.NONE;
        }
    }

    private static boolean parseGreenStar(String greenStarField) {
        try {
            return Integer.parseInt(greenStarField) == 1;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static Set<CuisineType> parseCuisineTypes(String cuisinesField) {
        Set<CuisineType> cuisineTypes = new HashSet<>();
        if (cuisinesField == null || cuisinesField.isBlank()) return cuisineTypes;

        // Split iniziale anche se manca lo spazio dopo la virgola
        String[] cuisines = cuisinesField.split("\\s*,\\s*");


        for (String c : cuisines) {
            String normalised = c.toUpperCase()
                    .replace(" ", "_");

            try {
                cuisineTypes.add(CuisineType.valueOf(normalised));
            } catch (IllegalArgumentException ignored) {

            }
        }

        return cuisineTypes;
    }


    private static Restaurant createRestaurant(String[] fields) {
        // === OPERAZIONI ASINCRONE ===
        // Avvia le operazioni costose in parallelo
        CompletableFuture<String[]> locDataFuture = CompletableFuture
                .supplyAsync(() -> retrieveLocData(fields[1], fields[2]))
                .exceptionally(ex -> {
                    IO.printErrorMessage("Errore caricamento location: " + ex.getMessage());
                    return null;
                });

        CompletableFuture<Set<CuisineType>> cuisinesFuture = CompletableFuture
                .supplyAsync(() -> parseCuisineTypes(fields[4]))
                .exceptionally(ex -> {
                    IO.printErrorMessage("Errore parsing cucine: " + ex.getMessage());
                    return new HashSet<>();
                });

        CompletableFuture<Set<String>> servicesFuture = CompletableFuture
                .supplyAsync(() -> parseServices(fields[12]))
                .exceptionally(ex -> {
                    IO.printErrorMessage("Errore parsing servizi: " + ex.getMessage());
                    return new HashSet<>();
                });

        // === OPERAZIONI SINCRONE (veloci) ===
        Award award = parseAward(fields[10]);
        boolean greenStar = parseGreenStar(fields[11]);

        // === ATTESA E ASSEMBLAGGIO ===
        // Aspetta che le operazioni asincrone terminino
        CompletableFuture.allOf(locDataFuture, cuisinesFuture, servicesFuture).join();

        // Raccogli i risultati
        String[] locData = locDataFuture.join();
        Set<CuisineType> cuisineTypes = cuisinesFuture.join();
        Set<String> services = servicesFuture.join();

        // Costruisci la Location
        Location location = createLocation(locData, fields);

        // Ottieni il prefisso nazionale
        String nationalPrefix = (location != null) ?
                getNationalPrefix(fields[7], location.getNation()) : "";

        // === COSTRUZIONE FINALE ===
        return new Restaurant(
                UUID.randomUUID(),                                    // Identificatore
                fields[0],                                           // Nome
                fields[13],                                          // Descrizione
                fields[9],                                           // Url pagina web
                null,                                                // Proprietario del ristorante
                nationalPrefix + fields[7],                          // Contatto telefonico
                location,                                            // Posizione geografica
                PriceRange.byDollarAmount(fields[3].length()),       // Fascia di prezzo
                rd.nextBoolean(),                                    // Prenotazione online
                rd.nextBoolean(),                                    // Consegna a domicilio
                award,                                               // Stelle Michelin
                greenStar,                                           // Green Star
                cuisineTypes,                                        // Stili culinari
                services                                             // Servizi offerti
        );
    }

    /**
     * index:      0       1      2     3          4          5      6       7          8     9         10     11         12             13   <br>
     * field csv: Name,Address,Location,Price,Cuisine,Longitude,Latitude,PhoneNumber,Url,WebsiteUrl,Award,GreenStar,FacilitiesAndServices,Description
     * <br>
     */
    public static void parseFromDataset(Path path) {
        if (path == null) return;

        try (Stream<String> lines = Files.lines(path)) {
            // === PREPARAZIONE DATI ===
            List<String> csvLines = lines.skip(1).toList();
            System.out.println("Processando " + csvLines.size() + " ristoranti...");

            // === ELABORAZIONE PARALLELA ===
            List<CompletableFuture<Restaurant>> restaurantFutures = csvLines.stream()
                    .map(line -> line.split(";"))
                    .map(fields -> CompletableFuture
                            .supplyAsync(() -> createRestaurant(fields))
                            .exceptionally(ex -> {
                                System.err.println("Errore processando ristorante " +
                                        (fields.length > 0 ? fields[0] : "sconosciuto") +
                                        ": " + ex.getMessage());
                                return null; // Restaurant fallito
                            })
                    )
                    .collect(Collectors.toList());

            // Progress tracking (opzionale)
            showProgressAsync(restaurantFutures);

            // === ATTESA E RACCOLTA RISULTATI ===
            System.out.println("Aspettando completamento elaborazione...");
            CompletableFuture.allOf(restaurantFutures.toArray(new CompletableFuture[0])).join();

            List<Restaurant> restaurants = restaurantFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)  // Filtra i ristoranti falliti
                    .toList();

            System.out.println("✅ Elaborati " + restaurants.size() + "/" + csvLines.size() + " ristoranti");

            // === COSTRUZIONE JSON E SCRITTURA ===
            CompletableFuture<Void> writeFileFuture = CompletableFuture.runAsync(() -> {
                try {
                    final ObjectMapper mapper = new ObjectMapper();
                    ArrayNode restaurantsJson = mapper.createArrayNode();

                    for (Restaurant r : restaurants) {
                        try {
                            r.build();
                            restaurantsJson.add(r.getJsonObject());
                        } catch (Exception ex) {
                            System.err.println("Errore building JSON per " + r.getName() + ": " + ex.getMessage());
                        }
                    }

                    // Scrittura su file
                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValue(new File(Constants.ROOT, "michelin_my_maps.json"), restaurantsJson);

                    System.out.println("✅ File michelin_my_maps.json scritto con successo!");

                } catch (IOException ex) {
                    System.err.println("❌ Errore scrittura file: " + ex.getMessage());
                    throw new RuntimeException(ex);
                }
            });

            writeFileFuture.join(); // Aspetta che la scrittura finisca

        } catch (IOException | SecurityException e) {
            System.err.println("❌ Error while parsing csv database: " + e.getMessage());
        }
    }

    /**
     * Mostra il progresso dell'elaborazione in modo asincrono
     */
    private static void showProgressAsync(List<CompletableFuture<Restaurant>> futures) {
        CompletableFuture.runAsync(() -> {
            int total = futures.size();
            int previousCompleted = 0;

            while (true) {
                int completed = (int) futures.stream().mapToLong(f -> (f.isDone() && !f.isCompletedExceptionally()) ? 1 : 0).sum();

                if (completed != previousCompleted) {
                    System.out.println("Progresso: " + completed + "/" + total +
                            " (" + (completed * 100 / total) + "%)");
                    previousCompleted = completed;
                }

                if (completed == total) break;
            }
        });
    }
}
