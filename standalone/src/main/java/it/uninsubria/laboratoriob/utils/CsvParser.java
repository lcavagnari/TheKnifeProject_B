package it.uninsubria.laboratoriob.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import it.uninsubria.laboratoriob.objects.Location;
import it.uninsubria.laboratoriob.objects.Restaurant;
import it.uninsubria.laboratoriob.objects.enums.Award;
import it.uninsubria.laboratoriob.objects.enums.CuisineType;
import it.uninsubria.laboratoriob.objects.enums.Nation;
import it.uninsubria.laboratoriob.objects.enums.PriceRange;
import it.uninsubria.laboratoriob.ui.IO;
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

/**
 * Parser/trasformatore per il dataset CSV Michelin dell’applicazione.
 * <p>
 * Responsabilità:
 * <ul>
 *   <li>Parsing robusto di righe CSV “sporche” (address/location/phone formati in modo irregolare).</li>
 *   <li>Normalizzazione di città, nazione, indirizzo, premi, green star, cucine e servizi.</li>
 *   <li>Costruzione di entità {@link Restaurant} e serializzazione JSON per esportazione.</li>
 *   <li>Esecuzione parallela (asimmetrica) delle parti costose per aumentare il throughput.</li>
 * </ul>
 * </p>
 * <p><b>Nota:</b> i metodi pubblici non sollevano eccezioni non controllate verso l’esterno; gli errori
 * vengono loggati su console con messaggi chiari. I metodi privati possono gestire/ritornare fallback
 * coerenti (es. {@code null} o set vuoti) in caso di input non valido.</p>
 */
@UtilityClass
public class CsvParser {

    /** Sorgente casuale per campi sintetici (es. flag booleani). */
    private final static SecureRandom rd = new SecureRandom();

    /**
     * Estrae e normalizza tripla <i>(city, nation, address)</i> a partire dai campi grezzi.
     * <p>
     * Gestisce casi anomali:
     * <ul>
     *   <li>Location con più virgole (città composte).</li>
     *   <li>Location con sola nazione (città dedotta dall’indirizzo).</li>
     *   <li>Rimozione di ridondanze dall’indirizzo finale.</li>
     * </ul>
     * </p>
     *
     * @param address  stringa indirizzo dal CSV (potenzialmente contenente città/nazione in coda)
     * @param location stringa location dal CSV (tipicamente “City, Nation” ma può variare)
     * @return array di 3 elementi: [0]=city, [1]=nation (UPPER_SNAKE_CASE), [2]=address ripulito
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

        nation = nation.trim()
                .replaceAll("[\\s\\-]", "_")
                .toUpperCase();

        // 0: city, 1: nation, 2: address
        return new String[]{ city, nation, address };
    }

    /**
     * Costruisce una {@link Location} valida dai dati normalizzati e dai campi CSV originali.
     *
     * @param locData tripla [city, nation, address] prodotta da {@link #retrieveLocData(String, String)}
     * @param fields  riga CSV splittata; in particolare usa latitude/longitude
     * @return location valida; {@code null} se il mapping della nazione o il parsing lat/lon fallisce
     */
    private static Location createLocation(String[] locData, String[] fields) {
        if (locData == null) return null;

        Nation nation;
        String nationName = null;
        try {
            nationName = locData[1].trim().toUpperCase().replace("_MAINLAND", "");
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

    /**
     * Ricava il prefisso telefonico internazionale a partire da un numero “grezzo”
     * e dalla nazione dedotta.
     *
     * @param phoneNumber numero come in CSV (potrebbe essere locale)
     * @param nation      nazione di riferimento (per parsing/region)
     * @return prefisso internazionale nel formato “+CC”; stringa vuota se non determinabile
     */
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

    /**
     * Parsing “soft” dell’elenco servizi (field FacilitiesAndServices).
     * <p>Divide su virgola, trimma e filtra null/blank.</p>
     *
     * @param facilitiesAndServices campo CSV dei servizi
     * @return insieme di servizi normalizzati minimalmente
     */
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

    /**
     * Converte il campo “Award” in {@link Award}.
     * <p>Gestisce sia interi (es. “1”, “2”, …) sia stringhe (es. “BIB GOURMAND”).</p>
     *
     * @param ratingField campo premi dal CSV
     * @return award corrispondente; {@link Award#NONE} in caso di input non interpretabile
     */
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

    /**
     * Interpreta il campo “GreenStar” come boolean (1 = true).
     *
     * @param greenStarField campo CSV GreenStar
     * @return true se 1, altrimenti false
     */
    private static boolean parseGreenStar(String greenStarField) {
        try {
            return Integer.parseInt(greenStarField) == 1;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Parsing e normalizzazione dei tipi di cucina.
     * <p>Divide su virgole con/without spazi, upper-case e underscore.</p>
     *
     * @param cuisinesField campo CSV con le cucine
     * @return insieme di {@link CuisineType}; ignora i valori non mappabili
     */
    private static Set<CuisineType> parseCuisineTypes(String cuisinesField) {
        Set<CuisineType> cuisineTypes = new HashSet<>();
        if (cuisinesField == null || cuisinesField.isBlank()) return cuisineTypes;

        String[] cuisines = cuisinesField.split("\\s*,\\s*");
        for (String c : cuisines) {
            String normalised = c.toUpperCase().replace(" ", "_");
            try {
                cuisineTypes.add(CuisineType.valueOf(normalised));
            } catch (IllegalArgumentException ignored) {
                // Silenziosamente ignora cucine non note all'enum
            }
        }
        return cuisineTypes;
    }

    /**
     * Costruisce un {@link Restaurant} a partire da una riga CSV splittata.
     * <p>
     * Esegue in parallelo:
     * <ul>
     *   <li>Parsing/normalizzazione location.</li>
     *   <li>Parsing cucine.</li>
     *   <li>Parsing servizi.</li>
     * </ul>
     * e aggrega i risultati, calcolando inoltre premio e green star.
     * </p>
     *
     * @param fields riga CSV splittata su “;” secondo il layout documentato
     * @return istanza popolata di {@link Restaurant} (owner null); mai {@code null} se i dati minimi ci sono,
     *         può restituire valore valido con location {@code null} se non ricostruibile
     */
    private static Restaurant createRestaurant(String[] fields) {
        // === OPERAZIONI ASINCRONE ===
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

        // === OPERAZIONI SINCRONE ===
        Award award = parseAward(fields[10]);
        boolean greenStar = parseGreenStar(fields[11]);

        // === ATTESA E ASSEMBLAGGIO ===
        CompletableFuture.allOf(locDataFuture, cuisinesFuture, servicesFuture).join();

        String[] locData = locDataFuture.join();
        Set<CuisineType> cuisineTypes = cuisinesFuture.join();
        Set<String> services = servicesFuture.join();

        Location location = createLocation(locData, fields);

        // Prefisso nazionale per il telefono
        String nationalPrefix = (location != null) ? getNationalPrefix(fields[7], location.getNation()) : "";

        // === COSTRUZIONE FINALE ===
        return new Restaurant(
                UUID.randomUUID(),                              // Identificatore
                fields[0],                                      // Nome
                fields[13],                                     // Descrizione
                fields[9],                                      // Url pagina web
                null,                                           // Proprietario
                nationalPrefix + fields[7],                     // Telefono
                location,                                       // Posizione
                PriceRange.byDollarAmount(fields[3].length()),  // Fascia di prezzo
                rd.nextBoolean(),                               // Prenotazione online (sintetico)
                rd.nextBoolean(),                               // Consegna a domicilio (sintetico)
                award,                                          // Stelle Michelin
                greenStar,                                      // Green Star
                cuisineTypes,                                   // Tipi di cucina
                services                                        // Servizi
        );
    }

    /**
     * Parsing del dataset CSV Michelin e serializzazione JSON risultante.
     * <p>
     * Layout dei campi (indice → campo):
     * <pre>
     * 0  Name
     * 1  Address
     * 2  Location
     * 3  Price
     * 4  Cuisine
     * 5  Longitude
     * 6  Latitude
     * 7  PhoneNumber
     * 8  Url
     * 9  WebsiteUrl
     * 10 Award
     * 11 GreenStar
     * 12 FacilitiesAndServices
     * 13 Description
     * </pre>
     * </p>
     * <p>Il metodo:</p>
     * <ol>
     *   <li>Legge tutte le righe (skip header).</li>
     *   <li>Avvia in parallelo la costruzione dei {@link Restaurant}.</li>
     *   <li>Attende il completamento e costruisce un array JSON tramite {@code ObjectMapper}.</li>
     *   <li>Scrive il file <code>michelin_my_maps.json</code> in {@link Constants#ROOT}.</li>
     * </ol>
     *
     * @param path percorso del file CSV; se {@code null} il metodo termina senza effetto
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
                                return null;
                            })
                    )
                    .collect(Collectors.toList());

            // Tracking (non bloccante)
            showProgressAsync(restaurantFutures);

            // === ATTESA E RACCOLTA RISULTATI ===
            System.out.println("Attesa completamento elaborazione...");
            CompletableFuture.allOf(restaurantFutures.toArray(new CompletableFuture[0])).join();

            List<Restaurant> restaurants = restaurantFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
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

                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValue(new File(Constants.ROOT, "michelin_my_maps.json"), restaurantsJson);

                    System.out.println("✅ File michelin_my_maps.json scritto con successo!");

                } catch (IOException ex) {
                    System.err.println("❌ Errore scrittura file: " + ex.getMessage());
                    throw new RuntimeException(ex);
                }
            });

            writeFileFuture.join();

        } catch (IOException | SecurityException e) {
            System.err.println("❌ Error while parsing csv database: " + e.getMessage());
        }
    }

    /**
     * Mostra l’avanzamento complessivo dell’elaborazione in un task separato.
     * <p>Non blocca il thread chiamante; stampa “completed/total (%)”.</p>
     *
     * @param futures elenco dei task di costruzione {@link Restaurant}
     */
    private static void showProgressAsync(List<CompletableFuture<Restaurant>> futures) {
        CompletableFuture.runAsync(() -> {
            int total = futures.size();
            int previousCompleted = 0;

            while (true) {
                int completed = (int) futures.stream()
                        .mapToLong(f -> (f.isDone() && !f.isCompletedExceptionally()) ? 1 : 0)
                        .sum();

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
