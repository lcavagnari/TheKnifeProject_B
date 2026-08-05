package it.uninsubria.laboratoriob.objects;

import it.uninsubria.laboratoriob.objects.enums.*;
import it.uninsubria.laboratoriob.objects.users.Owner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Restaurant Tests")
class RestaurantTest {

    private Restaurant restaurant;
    private Owner owner;
    private Location location;

    @BeforeEach
    void setUp() {
        location = new Location(Nation.ITALY, "Milan", 45.4642, 9.1900, "Via Garibaldi 5");
        owner = new Owner("owneruser", "password123", "Mario", "Rossi", location, LocalDate.of(1980, 5, 15));

        Set<CuisineType> cuisines = new HashSet<>(Arrays.asList(CuisineType.ITALIAN, CuisineType.MEDITERRANEAN));
        Set<String> services = new HashSet<>(Arrays.asList("WiFi", "Parking"));

        restaurant = new Restaurant(
                UUID.randomUUID(),
                "Ristorante Bella Vista",
                "A beautiful restaurant with great views",
                "https://bellavista.com",
                owner,
                "+39 02 1234567",
                location,
                PriceRange.EXPENSIVE,
                true,
                true,
                Award.ONE_STAR,
                true,
                cuisines,
                services
        );
    }

    @Test
    @DisplayName("Should create restaurant with all properties")
    void testRestaurantCreation() {
        assertNotNull(restaurant);
        assertEquals("Ristorante Bella Vista", restaurant.getName());
        assertEquals(owner, restaurant.getOwner());
        assertEquals(PriceRange.EXPENSIVE, restaurant.getPriceRange());
        assertEquals(Award.ONE_STAR, restaurant.getAward());
        assertTrue(restaurant.isGreenStar());
        assertTrue(restaurant.isHasDelivery());
        assertTrue(restaurant.isHasOnlineBooking());
    }

    @Test
    @DisplayName("Should handle null name with default value")
    void testRestaurantWithNullName() {
        Restaurant r = new Restaurant(
                null, null, null, owner, null, location, null, false, false, null, false, null, null, null
        );
        assertEquals("Restaurant", r.getName());
    }

    @Test
    @DisplayName("Should add cuisine type successfully")
    void testAddCuisineType() {
        assertTrue(restaurant.addCuisineType(CuisineType.FRENCH));
        assertTrue(restaurant.getCuisinesTypes().contains(CuisineType.FRENCH));
    }

    @Test
    @DisplayName("Should not add null cuisine type")
    void testAddNullCuisineType() {
        assertFalse(restaurant.addCuisineType(null));
    }

    @Test
    @DisplayName("Should remove cuisine type successfully")
    void testRemoveCuisineType() {
        assertTrue(restaurant.removeCuisineType(CuisineType.ITALIAN));
        assertFalse(restaurant.getCuisinesTypes().contains(CuisineType.ITALIAN));
    }

    @Test
    @DisplayName("Should add service successfully")
    void testAddService() {
        assertTrue(restaurant.addService("Outdoor Seating"));
        assertTrue(restaurant.getServices().contains("Outdoor Seating"));
    }

    @Test
    @DisplayName("Should remove service successfully")
    void testRemoveService() {
        assertTrue(restaurant.removeService("WiFi"));
        assertFalse(restaurant.getServices().contains("WiFi"));
    }

    @Test
    @DisplayName("Should build JSON object correctly")
    void testBuildJsonObject() {
        restaurant.build();
        assertNotNull(restaurant.getJsonObject());
        assertEquals("Ristorante Bella Vista", restaurant.getJsonObject().get("name").asText());
        assertEquals("$$$", restaurant.getJsonObject().get("priceRange").asText());
    }

    @Test
    @DisplayName("Should return formatted toString")
    void testToString() {
        String result = restaurant.toString();
        assertTrue(result.contains("Ristorante Bella Vista"));
        assertTrue(result.contains("ONE_STAR") || result.contains("1 star"));
    }

    @Test
    @DisplayName("Should save restaurant successfully")
    void testSave() {
        assertTrue(restaurant.save());
    }
}
