package it.uninsubria.laboratorioa.objects;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratorioa.utils.Constants;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe astratta base per tutte le entità serializzabili in JSON.<p>
 * Gestisce l'identificatore univoco UUID, il file di salvataggio e la rappresentazione JSON.<p>
 * Fornisce metodi per salvare, ricostruire e rappresentare l'entità.<p>
 * Utilizza {@link ObjectMapper} configurato per serializzazione e deserializzazione sicura.<p>
 * <p>
 * @author Luca Cavagnari
 * @version 1.0
 */
public abstract class JsonEntity {
    /**
     * Mapper JSON condiviso per tutte le entità.
     */
    protected static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
    }

    /**
     * Identificatore univoco dell'entità.
     */
    @Getter
    protected final UUID id;

    /**
     * Cartella di salvataggio dove si trova il file JSON.
     */
    @Getter
    protected final File saveFolder;

    /**
     * File di salvataggio JSON associato all'entità.
     */
    @Getter
    protected File saveFile;

    /**
     * Rappresentazione JSON interna dell'entità.
     */
    @Getter
    protected ObjectNode jsonObject;

    /**
     * Costruttore principale che crea una nuova entità con UUID casuale.<p>
     * Inizializza la struttura JSON e i riferimenti al file di salvataggio.<p>
     *
     * @param folderName nome della cartella di salvataggio relativa a Constants.ROOT
     */
    public JsonEntity(String folderName) {
        this.jsonObject = mapper.createObjectNode();
        this.id = UUID.randomUUID();

        jsonObject.put("id", String.valueOf(id));

        this.saveFolder = new File(Constants.ROOT, folderName);
        this.saveFile = new File(saveFolder, id + ".json");
    }

    /**
     * Costruttore che inizializza l'entità con un UUID esistente.<p>
     * La cartella e il file di salvataggio rimangono nulli.<p>
     *
     * @param id UUID esistente
     */
    public JsonEntity(UUID id) {
        this.id = id;
        this.saveFolder = this.saveFile = null;

        this.jsonObject = mapper.createObjectNode();
        if (id != null)
            jsonObject.put("id", String.valueOf(id));
    }

    /**
     * Costruttore di default che crea un'entità senza id né file di salvataggio.<p>
     */
    public JsonEntity() {
        this.jsonObject = mapper.createObjectNode();

        this.saveFolder = this.saveFile = null;
        this.id = null;
    }

    /**
     * Salva l'entità su file JSON, creando la cartella se non esiste.<p>
     * Costruisce prima la rappresentazione JSON chiamando {@link #build()}.<p>
     *
     * @return true se il salvataggio è andato a buon fine, false in caso di errore
     */
    public boolean save() {
        try {
            if (!saveFolder.exists()) saveFolder.mkdirs();
            build();
            mapper.writerWithDefaultPrettyPrinter().writeValue(saveFile, jsonObject);
            return true;

        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    /**
     * Ricostruisce la rappresentazione JSON eliminando i dati precedenti.<p>
     * Chiama {@link #build()} per rigenerare il contenuto.<p>
     */
    public void rebuild() {
        jsonObject.removeAll();        // Purga dati vecchi

        jsonObject.put("id", String.valueOf(id));

        build();                      // Invoca il builder della sottoclasse
    }

    /**
     * Restituisce la rappresentazione JSON formattata in stringa leggibile.<p>
     *
     * @return stringa JSON formattata
     */
    public String toPrettyString() {
        return jsonObject.toPrettyString();
    }

    /**
     * Restituisce una stringa base rappresentante l'entità.<p>
     *
     * @return stringa contenente l'id
     */
    @Override
    public String toString() {
        return "id=" + id;
    }

    /**
     * Controlla l'uguaglianza con un altro oggetto basandosi sull'UUID e la classe.<p>
     *
     * @param o oggetto da confrontare
     * @return true se uguali, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JsonEntity that = (JsonEntity) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Calcola l'hash code basandosi sull'UUID.<p>
     *
     * @return hash code calcolato
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Metodo astratto che costruisce la rappresentazione JSON specifica della sottoclasse.<p>
     * Deve essere implementato obbligatoriamente da ogni classe derivata.<p>
     */
    protected abstract void build();
}
