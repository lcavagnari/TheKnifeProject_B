import it.uninsubria.laboratorioa.utils.Constants;
import it.uninsubria.laboratorioa.objects.Restaurant;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    /*
    Name,Address,Location,Price,Cuisine,Longitude,Latitude,PhoneNumber,Url,WebsiteUrl,Award,GreenStar,FacilitiesAndServices,Description
     */
    private static List<Restaurant> loadCompany() {
        List<Restaurant> companies = new ArrayList<>();

        File f = new File(Constants.ROOT,"michelin_my_maps.csv");
        try (Stream<String> lines = Files.lines(f.toPath())) {
            lines.skip(1) // Optional: skip header
                    .map(line -> line.split(";"))
                    .forEach(fields -> {

                        Restaurant c = new Restaurant(
                                fields
                        );

                        companies.add(c);
                    });
        } catch (IOException e) {
            System.out.println("Error while parsing csv database");
        }

        return companies;
    }


    public static void

    @SneakyThrows
    public static void main(String[] args) {
        List<Restaurant> d = loadCompany();

        d.get(1).save();
//        for (Restaurant c : d) System.out.println(c);

        System.out.println(d.size());
        //new DataHandler().loadFromFile();
    }
    
}
