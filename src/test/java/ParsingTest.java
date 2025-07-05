import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.utils.Constants;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class ParsingTest {


    /**
     *
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
            city = (N >= 3 && fields[N-2].matches("\\d+")) ? fields[N-3] : fields[N-2];

        // Caso base; città e nazione separate da ','
        } else {
            city = cityAndNation[0];
            nation = cityAndNation[1];
        }

        String[] tmp = address.split(",");
        address = address.replaceAll(city+"|"+nation+"|"+tmp[tmp.length-1],"")
                .replace(", ,",",")
                .replaceAll(",$","");

        // 0:nation , 1: city , 2:address
        return new String[]{
                city,
                nation.trim().replaceAll("[\\s\\-]", "_").toUpperCase(),
                address
        };
    }

    /**
     *
     * index:      0	   1		2     3	      4		   5		6		  7		   8	  9		  10	  11			12					13
     * field csv: Name,Address,Location,Price,Cuisine,Longitude,Latitude,PhoneNumber,Url,WebsiteUrl,Award,GreenStar,FacilitiesAndServices,Description
     *
     * @return Dataset letto come collection di ristoranti.
     */
    private static List<Restaurant> parseFromDataset() {
        List<Restaurant> companies = new ArrayList<>();

        Random rd = new Random();

        File f = new File(Constants.ROOT, "michelin_my_maps.csv");
        try (Stream<String> lines = Files.lines(f.toPath())) {
            List<String> t = new ArrayList<>();

            lines.skip(1) // Optional: skip header
                    .map(line -> line.split(";"))
                    .forEach(fields -> {

                        Restaurant c = new Restaurant(
                                fields[0],
                                fields[13],
                                fields[9],
                                fields[7],
                                null,
                                PriceRange.MODERATE,
                                rd.nextBoolean(),
                                rd.nextBoolean()
                        );

                        companies.add(c);


                        try {
                            // 0: city , 1:nation , 2: address
                            String[] locData = retrieveLocData(fields[1], fields[2]);

                            Location loc = new Location(
                                    Nation.valueOf(locData[1]),
                                    locData[0],
                                    Double.parseDouble(fields[5]),
                                    Double.parseDouble(fields[6]),
                                    locData[2]
                            );

                        } catch (Exception e) {}
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

        //System.out.println(d.size());
        //new DataHandler().loadFromFile();
    }

}
