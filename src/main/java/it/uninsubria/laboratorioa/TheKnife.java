package it.uninsubria.laboratorioa;

import it.uninsubria.laboratorioa.ui.menus.GuestMenus;
import it.uninsubria.laboratorioa.ui.IO;
import it.uninsubria.laboratorioa.ui.exceptions.AbortOperationException;
import it.uninsubria.laboratorioa.utils.Constants;
import it.uninsubria.laboratorioa.utils.CsvParser;
import it.uninsubria.laboratorioa.utils.Loader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class TheKnife {


    private static void updateMichelinDataset(String path) {
        path = (path != null && !path.isBlank()) ? path : "michelin_my_maps.csv";

        Path inputPath;
        try {
            inputPath = Paths.get(path);

            File dataset = new File(inputPath.toUri());
            File parsedDataset = new File(Constants.ROOT, "michelin_my_maps.json");

            if (!dataset.exists() || !dataset.isFile() || !dataset.getName().endsWith(".csv")) {
                IO.printErrorMessage("File or path " + path + " does not exist or it is not supported by the program, please check and try again.");
                return;

                //
            } else if (parsedDataset.exists()) {
                File oldParsedDataset = new File(Constants.ROOT, "michelin_my_maps.old.json");
                if (oldParsedDataset.exists()) {
                    IO.printErrorMessage("Backup copy of parsed dataset found. \nWarning: this procedure will overwrite current backup copy, make . \n ");
                    IO.getUserInput("Type 'continue' to proceed");
                }

                Files.copy(parsedDataset.toPath(), new File(Constants.ROOT, "michelin_my_maps.old.json").toPath(), StandardCopyOption.REPLACE_EXISTING);
                parsedDataset.delete();
            }

        } catch (AbortOperationException ignored) {
            IO.printErrorMessage("Operazione annullata");
            return;
        } catch (Exception ignored) {
            IO.printErrorMessage("File or path " + path + " does not exist, check and try again.");
            return;
        }

        IO.printErrorMessage("Updating michelin data from file...");

        long timestamp = System.currentTimeMillis();
        CsvParser.parseFromDataset(inputPath);

        IO.clearScreen();
        IO.printSuccessMessage("Update completed in " + ((System.currentTimeMillis() - timestamp) + "ms"));
    }

    public static void main(String[] args) {
        if (args.length > 1 && args[0].equals("--update")) {
            updateMichelinDataset(args[1]);
            return;

        } else if (args.length > 0 && args[0].equals("--update")) {
            updateMichelinDataset(null);
            return;
        }

        IO.printSuccessMessage("Loading The Knife...");

        Loader.loadFromFile();
        new GuestMenus().openMenu();
    }
}