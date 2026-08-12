package it.uninsubria.laboratoriob.test;

import it.uninsubria.laboratoriob.utils.CsvParser;

import java.io.File;

public class ParserTest {

    public static void main(String[] args) {
        CsvParser.parseFromDataset(new File("michelin_my_maps.csv").toPath());
    }
}
