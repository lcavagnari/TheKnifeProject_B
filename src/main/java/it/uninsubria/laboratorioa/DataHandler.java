package it.uninsubria.laboratorioa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.uninsubria.laboratorioa.jsonentities.Company;

import java.io.*;
import java.util.UUID;

public class DataHandler {

    private static final File ROOT = Constants.ROOT;
    private static final File COMPANIES_ROOT = new File(ROOT,"companies");
    private static final File USERS_ROOT = new File(ROOT,"users");


    public void loadFromFile() {

        File[] companies;
        File[] users;
        try {
            if (!ROOT.exists()) return;

            companies = COMPANIES_ROOT.listFiles();
            users = USERS_ROOT.listFiles();

        } catch (SecurityException ex) {
            System.out.println("Access denied");
            return;
        }

        for (File f : companies) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(f);

                Company company = new Company(
                        UUID.fromString(jsonNode.get("id").asText()),
                        jsonNode.get("name").asText(),
                        jsonNode.get("address").asText(),
                        jsonNode.get("phone").asText(),
                        jsonNode.get("email").asText(),
                        jsonNode.get("foundationYear").asInt(0)
                );

                System.out.println(jsonNode.toPrettyString());
                System.out.println(company);

            } catch (IOException e) {
                System.out.println("ERROR while parsing "+f.getName()+", cause:"+e.getMessage());
            } catch (SecurityException e) {
                System.out.println("Access is denied to "+f.getName());
            }
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