import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.objects.users.Client;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

public class UserTest {

    private static final List<String> sampleCities = List.of(
            "Neo Milan", "Techburg", "Omnissiah's Reach", "Rusthaven", "Cogwheel Bay",
            "Ferrumgrad", "Servoheim", "Nullport", "Gasketown", "Binary Creek"
    );

    private static final List<String> sampleAddresses = List.of(
            "42 Machine Spirit Blvd.", "13 Warp Drive", "7 Sprocket Ave", "101 Servo St.",
            "88 Logic Gate Ln.", "1 Holy Dataway", "404 Not Found Rd."
    );

    private static final SecureRandom rd = new SecureRandom();

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


    public static void main(String[] args) {
        System.out.println("dddd");

        Client c = new Client("111", "kibafo33", "name", "last", generateRandomLocation(), LocalDate.now());
        System.out.println(c);

        //c.addFavourite(new Restaurant());

        c.save();
    }
}
