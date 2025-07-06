import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.CuisineType;
import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import it.uninsubria.laboratorioa.objects.users.Owner;
import it.uninsubria.laboratorioa.utils.Constants;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
class Generators {
    private static final String CONSONANTS = "bcdfghjklmnpqrstvwxyz";
    private static final String VOWELS = "aeiou";

    private static final String USERNAME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    private static final String PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+[]{}|;:,.<>?";

    private static final List<String> sampleCities = List.of(
            "Neo Milan", "Techburg", "Omnissiah's Reach", "Rusthaven", "Cogwheel Bay",
            "Ferrumgrad", "Servoheim", "Nullport", "Gasketown", "Binary Creek"
    );

    private static final List<String> sampleAddresses = List.of(
            "42 Machine Spirit Blvd.", "13 Warp Drive", "7 Sprocket Ave", "101 Servo St.",
            "88 Logic Gate Ln.", "1 Holy Dataway", "404 Not Found Rd."
    );

    @Getter
    private static final SecureRandom rd = new SecureRandom();

    public static String generateRandomName(int minLen, int maxLen) {
        if (minLen < 2 || maxLen < minLen) {
            return "";
        }

        int length = minLen + rd.nextInt(maxLen - minLen + 1);
        StringBuilder name = new StringBuilder(length);

        // Start with uppercase consonant or vowel
        boolean useVowel = rd.nextBoolean();
        // Capitalize first letter
        char firstChar = useVowel
                ? Character.toUpperCase(VOWELS.charAt(rd.nextInt(VOWELS.length())))
                : Character.toUpperCase(CONSONANTS.charAt(rd.nextInt(CONSONANTS.length())));
        name.append(firstChar);

        // Append remaining characters alternating vowel/consonant
        for (int i = 1; i < length; i++) {
            useVowel = !useVowel;
            char c = useVowel
                    ? VOWELS.charAt(rd.nextInt(VOWELS.length()))
                    : CONSONANTS.charAt(rd.nextInt(CONSONANTS.length()));
            name.append(c);
        }

        return name.toString();
    }


    public static String generateUsername(int length) {
        if (length < 3) {
            throw new IllegalArgumentException("Username length must be at least 3");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(USERNAME_CHARS.charAt(rd.nextInt(USERNAME_CHARS.length())));
        }
        return sb.toString();
    }

    public static String generatePassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(rd.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    public static Location generateRandomLocation() {
        // Random nation
        Nation[] nations = Nation.values();
        Nation nation = nations[rd.nextInt(nations.length)];

        // Random city
        String city = sampleCities.get(rd.nextInt(sampleCities.size()));

        // Random coordinates
        double latitude = -90 + (180 * rd.nextDouble());
        double longitude = -180 + (360 * rd.nextDouble());

        // Random address
        String address = sampleAddresses.get(rd.nextInt(sampleAddresses.size()));

        return new Location(nation, city, latitude, longitude, address);
    }


    public static LocalDate generateRandomBirthdate(int minAge, int maxAge) {
        if (minAge < 0 || maxAge < minAge) {
            throw new IllegalArgumentException("Invalid age range");
        }

        LocalDate today = LocalDate.now();

        LocalDate maxBirthdate = today.minusYears(minAge); // youngest birthdate
        LocalDate minBirthdate = today.minusYears(maxAge); // oldest birthdate

        long daysBetween = ChronoUnit.DAYS.between(minBirthdate, maxBirthdate);
        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween + 1);

        return minBirthdate.plusDays(randomDays);
    }
}

public class ParsingTest {

    private static final SecureRandom rd = Generators.getRd();

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

        } catch (NumberFormatException ignored) {}


        // Generazione casuale owner

        String firstName = Generators.generateRandomName(4,20);
        String lastName = Generators.generateRandomName(4,24);
        String userName = Generators.generateUsername(8);
        String password = Generators.generatePassword(10);
        LocalDate birthdate = Generators.generateRandomBirthdate(20,80);
        Location loc = Generators.generateRandomLocation();

        Owner owner = new Owner(userName,password,firstName,lastName,loc,birthdate);
        owner.save();

        // Costruzione dell'oggetto

        Restaurant c = new Restaurant(
                fields[0],                                          // Nome
                fields[13],                                         // Descrizione
                fields[9],                                          // Url pagina web
                owner,
                nationalPrefix + fields[7],                         // Contatto telefonico con prefisso nazionale
                location,                                           // Posizione geografica
                PriceRange.byDollarAmount(fields[3].length()),      // Fascia di prezzo
                rd.nextBoolean(),                                   // Disponibilità alla consegna a domicilio
                rd.nextBoolean(),                                   // Disponibilità per la prenotazione online
                award,                                              // Stelle Michelin
                greenStar,                                          // "Green Star", certificato Michelin di sostenibilità
                cuisineTypes,                                       // Stili culinari offerti
                services                                            // Servizi offerti o regole
        );

        c.save();
        return c;
    }


    /**
     * index:      0	   1		2     3	      4		   5		6		  7		   8	  9		  10	  11			12					13   <br>
     * field csv: Name,Address,Location,Price,Cuisine,Longitude,Latitude,PhoneNumber,Url,WebsiteUrl,Award,GreenStar,FacilitiesAndServices,Description
     * <br>
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
                        try {
                            companies.add(createRestaurant(fields));
                        } catch (Exception ignored) {}
                    });
        } catch (IOException | SecurityException e) {
            System.out.println("Error while parsing csv database: "+ e);
        }

        return companies;
    }


    @SneakyThrows
    public static void main(String[] args) {
        long timestamp = System.currentTimeMillis();
        List<Restaurant> restaurants = parseFromDataset();

        System.out.println(restaurants.size());
        System.out.println(System.currentTimeMillis() - timestamp);
    }

}
