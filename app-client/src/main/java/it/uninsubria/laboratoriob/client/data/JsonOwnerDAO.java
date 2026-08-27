package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;

public final class JsonOwnerDAO extends JsonUserDAO<Owner> {

    public JsonOwnerDAO(AuthServiceInter authService) {
        super(authService);
    }

    @Override
    protected boolean isOwner() {
        return true;
    }

    @Override
    protected Owner mapNode(JsonNode node) {
        return new Owner(
                readId(node),
                readString(node, "username"),
                readString(node, "passwordHash"),
                readString(node, "passwordSalt"),
                readString(node, "name"),
                readString(node, "lastName"),
                readLocation(node),
                readDate(node),
                readBoolean(node, "system", false)
        );
    }

    @Override
    protected ArrayNode toArrayNode() {
        ArrayNode array = mapper.createArrayNode();
        for (Owner owner : cacheById.values()) {
            ObjectNode node = mapper.createObjectNode();
            writeUserFields(node, owner);
            array.add(node);
        }
        return array;
    }
}
