package it.uninsubria.laboratoriob.client.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.uninsubria.laboratoriob.api.enums.UserRole;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;

public final class JsonOwnerDAO extends JsonUserDAO<Owner> {

    JsonOwnerDAO(AuthServiceInter authService) {
        super(Owner.class, UserRole.OWNER, authService);
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

    // TODO: Owner lacks add and remove restaurant cache methods
    // TODO: add function to restaurant RMI service to add restaurant to server data layer. / new ad-hoc service.
}
