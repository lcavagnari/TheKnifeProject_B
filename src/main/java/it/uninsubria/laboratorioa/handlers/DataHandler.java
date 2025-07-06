package it.uninsubria.laboratorioa.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.utils.Constants;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        int count = 0;
        for (File f : rFiles) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(f);


                count++;
                return;

            } catch (IOException e) {
                System.out.println("ERROR while parsing " + f.getName() + ", cause:" + e.getMessage());
                return;
            } catch (SecurityException e) {
                System.out.println("Access is denied to " + f.getName());
                return;
            }
        }

        System.out.println(count);
    }

    private void loadUsers(File[] uFiles) {
        if (uFiles == null) return;

        final ObjectMapper mapper = new ObjectMapper();
        for (File f : uFiles) {
            try {

                JsonNode jsonNode = mapper.readTree(f);
                if (jsonNode == null) continue;

                String userName = jsonNode.asText("");
                String firstName = jsonNode.asText("");
                String lastName = jsonNode.asText("");
                //LocalDate dateOfBirth = LocalDate.parse()

                Location loc;
                try {
                    loc = new Location(
                            Nation.valueOf(jsonNode.get("nation").asText()),
                            jsonNode.get("city").asText(),
                            jsonNode.get("latitude").asDouble(),
                            jsonNode.get("longitude").asDouble(),
                            jsonNode.get("address").asText()
                    );
                } catch (Exception ignored) {
                }


                /*
                User user;
                if (Objects.equals(jsonNode.get("role").asText(""), "Owner"))
                    user = new Owner();

                else user = new Client();


                 */

            } catch (IOException e) {
                System.out.println("ERROR while parsing " + f.getName() + ", cause:" + e.getMessage());
            } catch (SecurityException e) {
                System.out.println("Access is denied to " + f.getName());
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