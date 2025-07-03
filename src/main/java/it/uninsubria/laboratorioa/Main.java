package it.uninsubria.laboratorioa;

import it.uninsubria.laboratorioa.jsonentities.Company;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {


    /*
    Name,Address,Location,Price,Cuisine,Longitude,Latitude,PhoneNumber,Url,WebsiteUrl,Award,GreenStar,FacilitiesAndServices,Description
     */
    private static List<Company> loadCompany() {
        List<Company> companies = new ArrayList<>();

        File f = new File(Constants.ROOT,"michelin_my_maps.csv");
        try (Stream<String> lines = Files.lines(f.toPath())) {
            lines.skip(1) // Optional: skip header
                    .map(line -> line.split(";"))
                    .forEach(fields -> {

                        Company c = new Company(
                                fields[0],
                                fields[1],
                                fields[7],
                                "",
                                0
                        );

                        companies.add(c);
                    });
        } catch (IOException e) {
            System.out.println("Error while parsing csv database");
        }

        return companies;
    }

    @SneakyThrows
    public static void main(String[] args) {
        /*

        Company company = new Company(
                "Omnissiah Industries","Mars Forge 42",
                "+39 06 555 1234","info@omnissiah.mars",2025
        );



        company.addEmployee("John", "Doe", "1980-01-15");
        company.addEmployee("Jane", "Smith", "1990-06-22");
        company.addEmployee("Deez", "Nuts", "1990-06-22");

        //Person p = new Person("John","Doe", "1980-01-15");
        //Review r = new Review(company,p,2,"Deeznuts");
        //company.addReview(r);

        //company.rebuild();

        System.out.println(company);
        System.out.println(company.toPrettyString());

        company.save();

        */

        List<Company> d = loadCompany();
        System.out.println(d.get(1));
        //new DataHandler().loadFromFile();
    }
}
