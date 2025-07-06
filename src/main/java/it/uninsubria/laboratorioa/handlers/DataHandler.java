package it.uninsubria.laboratorioa.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.Review;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.CuisineType;
import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import it.uninsubria.laboratorioa.objects.users.Client;
import it.uninsubria.laboratorioa.objects.users.Owner;
import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.utils.Constants;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@UtilityClass
public class DataHandler {

    private static final File ROOT = Constants.ROOT;
    private static final File RESTAURANTS_ROOT = new File(ROOT, "companies");
    private static final File USERS_ROOT = new File(ROOT, "users");

    @Getter
    private final static Map<UUID, Restaurant> restaurantsById = new HashMap<>();
    @Getter
    private final static Map<String, Restaurant> restaurantsByName = new HashMap<>();
    @Getter
    private final static Map<UUID, Restaurant> restaurantsByOwnerId = new HashMap<>();


    @Getter
    private final static Map<UUID, User> usersById = new HashMap<>();
    @Getter
    private final static Map<String, User> usersByName = new HashMap<>();


    private void loadRestaurants(File[] rFiles) {
        if (rFiles == null) return;
        
        for (File f : rFiles) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(f);

// === Parse base fields ===
                UUID id = UUID.fromString(jsonNode.path("id").asText());
                String name = jsonNode.path("name").asText();
                String description = jsonNode.path("address").asText(); // maps to description
                String websiteUrl = jsonNode.path("websiteUrl").asText(""); // optional
                String phone = jsonNode.path("phone").asText();

                Award award = Award.fromInt(jsonNode.path("award").asInt());
                boolean greenStar = jsonNode.path("greenStar").asBoolean();
                boolean hasDelivery = jsonNode.path("hasDelivery").asBoolean(false); // optional default
                boolean hasBooking = jsonNode.path("hasOnlineBooking").asBoolean(false); // optional default

                // === Owner ===
                UUID ownerId = UUID.fromString(jsonNode.path("owner").asText());
                User owner = null;

                if (usersById.containsKey(ownerId))
                    owner = usersById.get(ownerId);

                // === Location ===
                JsonNode locNode = jsonNode.path("location");
                Location loc = new Location(
                        Nation.valueOf(locNode.path("nation").asText()),
                        locNode.path("city").asText(),
                        locNode.path("latitude").asDouble(),
                        locNode.path("longitude").asDouble(),
                        locNode.path("address").asText()
                        );

                // === Price Range ===
                String price = jsonNode.get("priceRange").asText();
                PriceRange priceRange = PriceRange.byDollarAmount(price.length());

                // === Cuisines ===
                Set<CuisineType> cuisines = new HashSet<>();
                for (JsonNode node : jsonNode.path("cuisinesTypes")) {
                    try {
                        cuisines.add(CuisineType.valueOf(node.asText().toUpperCase()));
                    } catch (IllegalArgumentException ignored) {}
                }

                // === Services ===
                Set<String> services = new HashSet<>();
                for (JsonNode node : jsonNode.path("services")) {
                    services.add(node.asText());
                }

                // === Construct Restaurant ===
                Restaurant restaurant = new Restaurant(
                        name,
                        description,
                        websiteUrl,
                        (Owner) owner,
                        phone,
                        loc,
                        priceRange,
                        hasDelivery,
                        hasBooking,
                        award,
                        greenStar,
                        cuisines,
                        services
                );

                // === Reviews ===
                Map<UUID, Review> reviews = new HashMap<>();
                for (JsonNode node : jsonNode.path("reviews")) {
                    UUID uId = UUID.fromString(node.path("user").asText());
                    User user = null;
                    if (!usersById.containsKey(uId)) continue;

                    int value = node.path("velue").asInt();
                    LocalDateTime time = LocalDateTime.parse(node.get("timestamp").asText());
                    String text = node.get("text").asText();

                    String reply = node.get("reply").asText();

                    Review r = new Review(restaurant,usersById.get(uId),value, time,text, reply);
                    restaurant.addReview(r);
                }


            } catch (IOException e) {
                System.out.println("ERROR while parsing " + f.getName() + ", cause:" + e.getMessage());
                continue;
            } catch (SecurityException e) {
                System.out.println("Access is denied to " + f.getName());
                continue;
            }
        }
        
    }

    private void loadUsers(File[] uFiles) {
        if (uFiles == null) return;

        final ObjectMapper mapper = new ObjectMapper();
        for (File f : uFiles) {
            try {

                JsonNode jsonNode = mapper.readTree(f);
                if (jsonNode == null) continue;

                String id = jsonNode.path("id").asText();
                String username = jsonNode.path("username").asText();
                String name = jsonNode.path("name").asText();
                String lastName = jsonNode.path("lastName").asText();
                LocalDate dateOfBirth = LocalDate.parse(jsonNode.get("dateOfBirth").asText());

                JsonNode location = jsonNode.path("location");
                Location loc = new Location(
                        Nation.valueOf(location.path("nation").asText()),
                        location.path("city").asText(),
                        location.path("latitude").asDouble(),
                        location.path("longitude").asDouble(),
                        location.path("address").asText()
                );

                final JsonNode passwordNode = jsonNode.path("password");
                final String salt = passwordNode.path("salt").asText();
                final String passwordHash = passwordNode.path("password").asText();

                User user;
                if (Objects.equals(jsonNode.get("role").asText(""), "Owner")) {
                    user = new Owner(
                            username,
                            passwordHash, salt,
                            name, lastName,
                            loc, dateOfBirth
                    );

                } else {
                    Set<UUID> favourites = new HashSet<>();
                    JsonNode favouritesNode = jsonNode.get("cuisines");
                    if (favouritesNode != null && favouritesNode.isArray()) {
                        for (JsonNode fav : favouritesNode)
                            favourites.add(UUID.fromString(fav.asText()));
                    }

                    user = new Client(
                            username,passwordHash,salt,
                            name,lastName,
                            loc,dateOfBirth,
                            favourites
                            );
                }


                usersById.put(UUID.fromString(id),user);
                usersByName.put(username,user);

            } catch (IOException | IllegalArgumentException e) {
                System.err.println("ERROR while parsing " + f.getName() + ", cause:" + e.getMessage());
                continue;
            } catch (SecurityException e) {
                System.err.println("Access is denied to " + f.getName());

            }
        }
    }


    public void loadFromFile() {

        File[] restaurants;
        File[] users;
        try {
            if (!ROOT.exists()) return;

            restaurants = RESTAURANTS_ROOT.listFiles();
            users = USERS_ROOT.listFiles();

            loadUsers(users);
            loadRestaurants(restaurants);

        } catch (SecurityException ex) {
            System.out.println("Access denied");
        }
    }

}


/*
public boolean read() {
        if (!inputFile.exists() || !inputFile.canRead()) return false;

        try (FileInputStream inputStream = new FileInputStream(inputFile.getName());
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] params = strLine
                        .replace(", ",",")
                        .split(",");

                if (params.length < 3) continue;
                Award award = new Award(Integer.parseInt(params[0]), params[1], params[2],Integer.parseInt(params[3]));
                awards.add(award);
            }
            return true;

        } catch (IOException e) {
            awards.clear();
            e.printStackTrace();
            return false;
        }
    }
 */