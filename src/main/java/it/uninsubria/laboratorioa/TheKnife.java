package it.uninsubria.laboratorioa;

import it.uninsubria.laboratorioa.ui.GuestMenus;
import it.uninsubria.laboratorioa.ui.IO;
import it.uninsubria.laboratorioa.utils.CsvParser;
import it.uninsubria.laboratorioa.utils.Loader;

public class TheKnife {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--update")) {
            String path = "michelin_my_maps.csv";
            if (args.length > 1 && !args[1].isBlank()) path = args[1];

            IO.printErrorMessage("Updating michelin data from file...");

            long timestamp = System.currentTimeMillis();
            CsvParser.parseFromDataset(path);

            IO.clearScreen();
            IO.printSuccessMessage("Update completed in "+((System.currentTimeMillis() - timestamp)/1000));

            return;
        }

        IO.printSuccessMessage("Loading The Knife...");

        Loader.loadFromFile();
        new GuestMenus().openMenu();
    }
}