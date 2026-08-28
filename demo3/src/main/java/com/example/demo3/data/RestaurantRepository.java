package com.example.demo3.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Legge tutti i file .json dentro data/restaurants/ e li converte negli oggetti
 * Restaurant "veri" di common-api. Sostituisce la vecchia RestaurantService, che
 * mappava su una classe Restaurant locale/duplicata.
 * <p>
 * Come per {@link UserRepository}, la conversione JSON è manuale perché
 * common-api non è annotata per Jackson. Questa classe è anch'essa una
 * soluzione ponte in attesa del server: quando ci sarà, la ricerca/esplorazione
 * ristoranti passerà per una chiamata di rete invece che per la lettura di file
 * locali.
 */
public class RestaurantRepository {

    private static final String RESTAURANTS_DIR = "data/restaurants";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<Restaurant> caricaTutti() {
        List<Restaurant> risultato = new ArrayList<>();

        File dir = new File(RESTAURANTS_DIR);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));

        if (files == null) {
            System.err.println("Cartella ristoranti non trovata: " + dir.getAbsolutePath());
            return risultato;
        }

        for (File file : files) {
            try {
                JsonNode node = MAPPER.readTree(file);
                risultato.add(fromJson(node));
            } catch (IOException e) {
                System.err.println("Errore leggendo " + file.getName() + ": " + e.getMessage());
            }
        }

        return risultato;
    }

    public List<Restaurant> cerca(String keyword) {
        List<Restaurant> tutti = caricaTutti();
        if (keyword == null || keyword.isBlank()) {
            return tutti;
        }

        String kw = keyword.trim().toLowerCase(Locale.ROOT);
        List<Restaurant> risultato = new ArrayList<>();
        for (Restaurant r : tutti) {
            boolean matchNome = r.getName() != null && r.getName().toLowerCase(Locale.ROOT).contains(kw);
            boolean matchCitta = r.getLocation() != null && r.getLocation().getCity() != null
                    && r.getLocation().getCity().toLowerCase(Locale.ROOT).contains(kw);
            boolean matchCucina = r.getCuisinesTypes() != null && r.getCuisinesTypes().stream()
                    .anyMatch(c -> c != null && c.toString().toLowerCase(Locale.ROOT).contains(kw));

            if (matchNome || matchCitta || matchCucina) {
                risultato.add(r);
            }
        }
        return risultato;
    }

    // ---- conversione manuale Restaurant <-> JSON ----

    private Restaurant fromJson(JsonNode node) {
        UUID id = node.hasNonNull("id") ? UUID.fromString(node.get("id").asText()) : UUID.randomUUID();
        String name = node.path("name").asText(null);
        String description = node.path("description").asText(null);
        String websiteUrl = node.path("websiteUrl").asText(node.path("website").asText(null));
        String phone = node.path("phone").asText(null);

        Location location = null;
        JsonNode locNode = node.get("location");
        if (locNode != null && !locNode.isNull()) {
            Nation nation = Nation.fromString(locNode.path("nation").asText(null));
            location = new Location(
                    nation,
                    locNode.path("city").asText(null),
                    locNode.path("latitude").asDouble(locNode.path("lat").asDouble(0.0)),
                    locNode.path("longitude").asDouble(locNode.path("lon").asDouble(0.0)),
                    locNode.path("address").asText(null));
        }

        PriceRange priceRange = parseEnum(PriceRange.class, node.path("priceRange").asText(null), PriceRange.MODERATE);
        boolean hasDelivery = node.path("hasDelivery").asBoolean(node.path("delivery").asBoolean(false));
        boolean hasOnlineBooking = node.path("hasOnlineBooking").asBoolean(node.path("onlineBooking").asBoolean(false));
        Award award = parseEnum(Award.class, node.path("award").asText(null), Award.NONE);
        boolean greenStar = node.path("greenStar").asBoolean(false);

        UUID ownerId = node.hasNonNull("ownerId") ? UUID.fromString(node.get("ownerId").asText()) : UUID.randomUUID();
        Owner owner = stubOwner(ownerId);

        Set<CuisineType> cuisineTypes = new HashSet<>();
        if (node.has("cuisineTypes")) {
            node.get("cuisineTypes").forEach(n -> {
                CuisineType c = parseEnum(CuisineType.class, n.asText(null), null);
                if (c != null) cuisineTypes.add(c);
            });
        }

        Set<String> services = new HashSet<>();
        if (node.has("services")) {
            node.get("services").forEach(n -> services.add(n.asText()));
        }

        Restaurant restaurant = new Restaurant(id, name, description, websiteUrl, owner, phone, location, priceRange,
                hasDelivery, hasOnlineBooking, award, greenStar, cuisineTypes, services);

        JsonNode reviewsNode = node.get("reviews");
        if (reviewsNode != null && reviewsNode.isArray()) {
            reviewsNode.forEach(rn -> restaurant.addReview(reviewFromJson(rn, restaurant)));
        }

        return restaurant;
    }

    private Review reviewFromJson(JsonNode node, Restaurant restaurant) {
        UUID id = node.hasNonNull("id") ? UUID.fromString(node.get("id").asText()) : UUID.randomUUID();
        int value = node.path("value").asInt(5);
        String text = node.path("text").asText("Ottima esperienza consigliata a tutti");
        LocalDateTime timestamp = node.hasNonNull("timestamp")
                ? LocalDateTime.parse(node.get("timestamp").asText())
                : LocalDateTime.now();
        Customer author = stubCustomer(UUID.randomUUID());
        return new Review(id, restaurant, author, value, timestamp, text, null);
    }

    /**
     * Come {@link #stubOwner(UUID)}, ma per l'autore di una recensione: finché le
     * recensioni arrivano da file JSON locali senza un vero utente Customer
     * associato, costruiamo un Customer segnaposto.
     */
    private Customer stubCustomer(UUID customerId) {
        return new Customer(customerId, "customer", "", "", "Cliente", "Anonimo",
                null, LocalDate.now().minusYears(25));
    }

    /**
     * Un ristorante di common-api richiede un Owner reale, non solo un ID: finché
     * i dati arrivano da file JSON locali (senza un vero utente Owner associato),
     * costruiamo un Owner "segnaposto" che porta solo l'id utile a identificarlo.
     * Andrà rimosso quando i ristoranti verranno caricati dal server, che potrà
     * fornire l'Owner completo.
     */
    private Owner stubOwner(UUID ownerId) {
        return new Owner(ownerId, "owner", "", "", "Proprietario", "Sconosciuto",
                null, LocalDate.now().minusYears(30));
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, E fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
