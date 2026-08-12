package it.uninsubria.laboratoriob.server.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.server.data.LocationDAO;
import it.uninsubria.laboratoriob.server.data.OwnerDAO;
import it.uninsubria.laboratoriob.server.data.RestaurantDAO;
import it.uninsubria.laboratoriob.server.ui.IO;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDate;
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

    private final static RestaurantDAO RESTAURANT_DAO = new RestaurantDAO();
    private final static LocationDAO LOCATION_DAO = new LocationDAO();
    private final static OwnerDAO OWNER_DAO = new OwnerDAO();

    public static final UUID SYSTEM_OWNER_ID = UUID.nameUUIDFromBytes("theknife-system-owner".getBytes());
    private static final String SYSTEM_OWNER_USERNAME = "system";
    private static final String SYSTEM_OWNER_SALT = PasswordHasher.generateSalt();
    private static final String SYSTEM_OWNER_PASSWORD_HASH = PasswordHasher.hash(UUID.randomUUID().toString(), SYSTEM_OWNER_SALT);

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

        return new String[]{city, nation, address};
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
            double lat = Double.parseDouble(fields[5]);
            double lon = Double.parseDouble(fields[6]);

            if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
                double tmp = lat;
                lat = lon;
                lon = tmp;
            }

            if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
                return null;
            }

            return new Location(
                    nation,
                    locData[0],
                    lat,
                    lon,
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

    public static Owner getOrCreateSystemOwner() {
        try (java.sql.Connection conn = Database.getConnection();
             java.sql.PreparedStatement check = conn.prepareStatement(
                     "SELECT 1 FROM \"user\" WHERE id = ? AND is_owner = true")) {
            check.setObject(1, SYSTEM_OWNER_ID, java.sql.Types.OTHER);
            try (java.sql.ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    return new Owner(SYSTEM_OWNER_ID, SYSTEM_OWNER_USERNAME,
                            SYSTEM_OWNER_PASSWORD_HASH, SYSTEM_OWNER_SALT,
                            "System", "Michelin", null,
                            LocalDate.of(2000, 1, 1), true);
                }
            }
        } catch (java.sql.SQLException e) {
            IO.printErrorMessage("Errore verifica system owner: " + e.getMessage());
        }

        Owner systemOwner = new Owner(
                SYSTEM_OWNER_ID,
                SYSTEM_OWNER_USERNAME,
                SYSTEM_OWNER_PASSWORD_HASH,
                SYSTEM_OWNER_SALT,
                "System",
                "Michelin",
                null,
                LocalDate.of(2000, 1, 1),
                true
        );

        OWNER_DAO.save(systemOwner);

        return systemOwner;
    }

    private static Restaurant createRestaurant(String[] fields, Owner owner) {
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
                owner,
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

        try (Stream<String> lines = Files.lines(path)) {
            List<String> csvLines = lines.skip(1).toList();
            System.out.println("Processando " + csvLines.size() + " ristoranti...");

            Owner systemOwner = getOrCreateSystemOwner();

            List<Restaurant> restaurants = csvLines.stream()
                    .map(line -> line.split(";"))
                    .map(fields -> createRestaurant(fields, systemOwner))
                    .toList();

            System.out.println("✅ Elaborati " + restaurants.size() + "/" + csvLines.size() + " ristoranti");

            CompletableFuture<Void> persistFuture = CompletableFuture.runAsync(() -> {
                OWNER_DAO.save(systemOwner);
                for (Restaurant r : restaurants) {
                    try {
                        if (r.getLocation() != null)
                            LOCATION_DAO.save(r.getLocation());
                        RESTAURANT_DAO.save(r);
                    } catch (Exception ex) {
                        IO.printErrorMessage("Errore salvataggio su database per " + r.getName() + ": " + ex.getMessage());
                    }
                }
                System.out.println("✅ Ristoranti salvati su database!");
            });

            CompletableFuture<Void> cacheFuture = CompletableFuture.runAsync(() -> {
                Loader.addUser(systemOwner);
                for (Restaurant r : restaurants) Loader.addRestaurant(r);
                System.out.println("✅ Ristoranti aggiunti alla cache in memoria!");
            });

            CompletableFuture.allOf(persistFuture, cacheFuture).join();

        } catch (IOException | SecurityException e) {
            IO.printErrorMessage("❌ Error while parsing csv database: " + e.getMessage());
        }
    }
}
