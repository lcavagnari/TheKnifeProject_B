package it.uninsubria.laboratorioa.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratorioa.utils.Constants;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public abstract class JsonEntity {
    protected final ObjectMapper mapper = new ObjectMapper();
    protected final File saveFolder;
    protected ObjectNode jsonObject;

    @Getter protected File saveFile;
    @Getter protected UUID id;


    public JsonEntity(String folderName) {
        this.jsonObject = mapper.createObjectNode();
        this.id = UUID.randomUUID();

        jsonObject.put("id",String.valueOf(id));

        this.saveFolder = new File(Constants.ROOT, folderName);
        this.saveFile = new File(saveFolder, id + ".json");
    }


    public boolean save() {
        try {

            if (!saveFolder.exists()) saveFolder.mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(saveFile, jsonObject);
            return true;

        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    public void rebuild() {
        jsonObject.removeAll();        // Purge old data

        jsonObject.put("id",String.valueOf(id));

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
