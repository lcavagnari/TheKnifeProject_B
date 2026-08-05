package it.uninsubria.laboratoriob.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import it.uninsubria.laboratoriob.data.LocationDAO;
import it.uninsubria.laboratoriob.data.RestaurantDAO;
import it.uninsubria.laboratoriob.enums.Award;
import it.uninsubria.laboratoriob.enums.CuisineType;
import it.uninsubria.laboratoriob.enums.Nation;
import it.uninsubria.laboratoriob.enums.PriceRange;
import it.uninsubria.laboratoriob.objects.Location;
import it.uninsubria.laboratoriob.objects.Restaurant;
import it.uninsubria.laboratoriob.ui.IO;
import lombok.experimental.UtilityClass;

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
 * <li>Parsing robusto di righe CSV “sporche” (address/location/phone formati in
 * modo irregolare).</li>
 * <li>Normalizzazione di città, nazione, indirizzo, premi, green star, cucine e
 * servizi.</li>
 * <li>Costruzione di entità {@link Restaurant} e persistenza su database e
 * cache.</li>
 * </ul>
 * </p>
 */
@UtilityClass
public class CsvParser {

    private final static SecureRandom rd = new SecureRandom();

    private static String[] retrieveLocData(String address, String location) {
        String[] cityAndNation = location.split(",");
        String city;
        String nation;

        if (cityAndNation.length > 2) {
            StringBuilder tmp = new StringBuilder();
            for (int i = 0; i < cityAndNation.length - 2; i++)
                tmp.append(cityAndNation[i]);

            city = tmp.toString();
            nation = cityAndNation[cityAndNation.length - 1];

        } else if (cityAndNation.length == 1) {
            String[] fields = address.split(",");
            int N = fields.length;

            nation = cityAndNation[0];
            city = (N >= 3 && fields[N - 2].matches("\\d+")) ? fields[N - 3] : fields[N - 2];

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

        return new String[] { city, nation, address };
    }

    private static Location createLocation(String[] locData, String[] fields) {
        if (locData == null)
            return null;

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
            return new Location(
                    nation,
                    locData[0],
                    Double.parseDouble(fields[5]),
                    Double.parseDouble(fields[6]),
                    locData[2]);
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
        if (regionCode == null || phoneNumber == null || regionCode.isEmpty() || phoneNumber.isBlank())
            return "";

        try {
            Phonenumber.PhoneNumber parsed = phoneUtil.parse(phoneNumber, regionCode);
            int countryCode = parsed.getCountryCode();
            return "+" + countryCode;
        } catch (NumberParseException e) {
            return "";
        }
    }

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
        if (cuisinesField == null || cuisinesField.isBlank())
            return cuisineTypes;

        String[] cuisines = cuisinesField.split("\\s*,\\s*");
        for (String c : cuisines) {
            String normalised = c.toUpperCase().replace(" ", "_");
            try {
                cuisineTypes.add(CuisineType.valueOf(normalised));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return cuisineTypes;
    }

    private static Restaurant createRestaurant(String[] fields) {
        String[] locData = retrieveLocData(fields[1], fields[2]);
        Set<CuisineType> cuisineTypes = parseCuisineTypes(fields[4]);
        Set<String> services = parseServices(fields[12]);

        Award award = parseAward(fields[10]);
        boolean greenStar = parseGreenStar(fields[11]);

        Location location = createLocation(locData, fields);

        String nationalPrefix = (location != null) ? getNationalPrefix(fields[7], location.getNation()) : "";

        return new Restaurant(
                UUID.randomUUID(),
                fields[0],
                fields[13],
                fields[9],
                null,
                nationalPrefix + fields[7],
                location,
                PriceRange.byDollarAmount(fields[3].length()),
                rd.nextBoolean(),
                rd.nextBoolean(),
                award,
                greenStar,
                cuisineTypes,
                services);
    }

    public static void parseFromDataset(Path path) {
        if (path == null)
            return;

        LocationDAO locationDAO = new LocationDAO();
        RestaurantDAO restaurantDAO = new RestaurantDAO();

        try (Stream<String> lines = Files.lines(path)) {
            List<String> csvLines = lines.skip(1).toList();
            System.out.println("Processando " + csvLines.size() + " ristoranti...");

            List<Restaurant> restaurants = csvLines.stream()
                    .map(line -> line.split(";"))
                    .map(CsvParser::createRestaurant)
                    .filter(Objects::nonNull)
                    .toList();

            System.out.println("✅ Elaborati " + restaurants.size() + "/" + csvLines.size() + " ristoranti");

            CompletableFuture<Void> persistFuture = CompletableFuture.runAsync(() -> {
                for (Restaurant r : restaurants) {
                    try {
                        if (r.getLocation() != null)
                            locationDAO.save(r.getLocation());
                        restaurantDAO.save(r);
                    } catch (Exception ex) {
                        System.err
                                .println("Errore salvataggio su database per " + r.getName() + ": " + ex.getMessage());
                    }
                }
                System.out.println("✅ Ristoranti salvati su database!");
            });

            CompletableFuture<Void> cacheFuture = CompletableFuture.runAsync(() -> {
                for (Restaurant r : restaurants) {
                    Loader.getRestaurantsById().put(r.getId(), r);
                    Loader.getRestaurantsByName().put(r.getName(), r);
                }
                System.out.println("✅ Ristoranti aggiunti alla cache in memoria!");
            });

            CompletableFuture.allOf(persistFuture, cacheFuture).join();

        } catch (IOException | SecurityException e) {
            System.err.println("❌ Error while parsing csv database: " + e.getMessage());
        }
    }
}
