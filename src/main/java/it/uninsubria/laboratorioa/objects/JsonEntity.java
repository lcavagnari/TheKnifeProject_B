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

public abstract class JsonEntity {
    protected static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
    }

    @Getter
    protected final UUID id;
    @Getter
    protected final File saveFolder;
    @Getter
    protected File saveFile;
    @Getter protected ObjectNode jsonObject;


    public JsonEntity(String folderName) {
        this.jsonObject = mapper.createObjectNode();
        this.id = UUID.randomUUID();

        jsonObject.put("id", String.valueOf(id));

        this.saveFolder = new File(Constants.ROOT, folderName);
        this.saveFile = new File(saveFolder, id + ".json");
    }

    public JsonEntity(UUID id) {
        this.id = id;
        this.saveFolder = this.saveFile = null;

        this.jsonObject = mapper.createObjectNode();
        if (id != null)
            jsonObject.put("id", String.valueOf(id));
    }

    public JsonEntity() {
        this.jsonObject = mapper.createObjectNode();

        this.saveFolder = this.saveFile = null;
        this.id = null;
    }


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

    public void rebuild() {
        jsonObject.removeAll();        // Purge old data

        jsonObject.put("id", String.valueOf(id));

        build();                      // Invoke subclass builder
    }

    public String toPrettyString() {
        return jsonObject.toPrettyString();
    }

    @Override
    public String toString() {
        return "id=" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JsonEntity that = (JsonEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected abstract void build();
}
