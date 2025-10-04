package it.uninsubria.laboratorioa;

import it.uninsubria.laboratorioa.ui.IO;
import it.uninsubria.laboratorioa.ui.menus.GuestMenus;
import it.uninsubria.laboratorioa.utils.Loader;

public class TheKnife {

    /**
     * Metodo principale
     * @param args Argomenti cli
     */
    public static void main(String[] args) {
        if (args.length > 1 && args[0].equals("--update")) {
            Loader.updateMichelinDataset(args[1]);
            return;

        } else if (args.length > 0 && args[0].equals("--update")) {
            Loader.updateMichelinDataset(null);
            return;
        }

        IO.printSuccessMessage("Loading The Knife...");

        Loader.loadFromFile();
        new GuestMenus().openMenu();
    }
}