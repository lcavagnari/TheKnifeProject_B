package it.uninsubria.laboratoriob;

import it.uninsubria.laboratoriob.ui.IO;
import it.uninsubria.laboratoriob.ui.menus.GuestMenus;
import it.uninsubria.laboratoriob.utils.Database;
import it.uninsubria.laboratoriob.utils.Loader;

public class TheKnife {

    /**
     * Metodo principale
     *
     * @param args Argomenti cli
     */
    public static void main(String[] args) {
        Database.initTables();
        Database.initialiseConstants();

        if (args.length > 1 && args[0].equals("--update")) {
            Loader.updateMichelinDataset(args[1]);
            return;

        } else if (args.length > 0 && args[0].equals("--update")) {
            Loader.updateMichelinDataset(null);
            return;
        }

        IO.printSuccessMessage("Loading The Knife...");


        Loader.initialiseMaps();
        new GuestMenus().openMenu();
        Database.shutdown();
    }
}
