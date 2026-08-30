package it.uninsubria.laboratoriob.api;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Classe di utilità contenente costanti condivise nel progetto.
 * Include il percorso radice per il salvataggio dati e il formato standard per i timestamp.
 *
 * Annota con {@link UtilityClass} per impedire istanziazione.
 */
@Getter
@UtilityClass
public class Constants {
    /**
     * Cartella radice di salvataggio dati.
     */
    public static final File ROOT = new File("data");

    /**
     * Formato standard per timestamp nei file di log o salvataggi.<p>
     * Formato: "yyyy-MM-dd.HH-mm-ss.SSSS"
     */
    public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss.SSSS");
}
