package it.uninsubria.laboratorioa.objects.users;

import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Owner Tests")
class OwnerTest {

    private Owner owner;
    private Location location;

    @BeforeEach
    void setUp() {
        location = new Location(Nation.ITALY, "Venice", 45.4408, 12.3155, "Piazza San Marco");
        owner = new Owner("restaurateur", "securePass123", "Giuseppe", "Bianchi", location, LocalDate.of(1970, 4, 12));
    }

    @Test
    @DisplayName("Should create owner with valid data")
    void testOwnerCreation() {
        assertNotNull(owner);
        assertEquals("restaurateur", owner.getUsername());
        assertEquals("Giuseppe", owner.getName());
        assertEquals("Bianchi", owner.getLastName());
        assertEquals(UserRole.OWNER, owner.getRole());
        assertNotNull(owner.getRestaurantsById());
        assertTrue(owner.getRestaurantsById().isEmpty());
    }

    @Test
    @DisplayName("Should add restaurant successfully")
    void testAddRestaurant() {
        Restaurant restaurant = new Restaurant(
                UUID.randomUUID(), "My Restaurant", "Delicious food",
                "https://myrestaurant.com", owner, "+39 041 12345", location,
                PriceRange.LUXURY, true, true, Award.TWO_STARS, true, null, null
        );

        assertTrue(owner.addRestaurant(restaurant));
        assertTrue(owner.getRestaurantsById().containsKey(restaurant.getId()));
        assertTrue(owner.getRestaurantsByName().containsKey("My Restaurant"));
    }

    @Test
    @DisplayName("Should not add null restaurant")
    void testAddNullRestaurant() {
        assertFalse(owner.addRestaurant(null));
    }

    @Test
    @DisplayName("Should not add duplicate restaurant")
    void testAddDuplicateRestaurant() {
        Restaurant restaurant = new Restaurant(
                UUID.randomUUID(), "My Restaurant", "Delicious food",
                "https://myrestaurant.com", owner, "+39 041 12345", location,
                PriceRange.LUXURY, true, true, Award.TWO_STARS, true, null, null
        );

        owner.addRestaurant(restaurant);
        assertFalse(owner.addRestaurant(restaurant));
    }

    @Test
    @DisplayName("Should remove restaurant successfully")
    void testRemoveRestaurant() {
        Restaurant restaurant = new Restaurant(
                UUID.randomUUID(), "My Restaurant", "Delicious food",
                "https://myrestaurant.com", owner, "+39 041 12345", location,
                PriceRange.LUXURY, true, true, Award.TWO_STARS, true, null, null
        );

        owner.addRestaurant(restaurant);
        assertTrue(owner.removeRestaurant(restaurant));
        assertFalse(owner.getRestaurantsById().containsKey(restaurant.getId()));
    }

    @Test
    @DisplayName("Should rename restaurant successfully")
    void testRenameRestaurant() {
        Restaurant restaurant = new Restaurant(
                UUID.randomUUID(), "Old Name", "Delicious food",
                "https://myrestaurant.com", owner, "+39 041 12345", location,
                PriceRange.LUXURY, true, true, Award.TWO_STARS, true, null, null
        );

        owner.addRestaurant(restaurant);
        assertTrue(owner.renameRestaurant(restaurant.getId(), "New Name"));
        assertEquals("New Name", restaurant.getName());
    }

    @Test
    @DisplayName("Should modify restaurant description")
    void testModifyRestaurantDescription() {
        Restaurant restaurant = new Restaurant(
                UUID.randomUUID(), "My Restaurant", "Old description",
                "https://myrestaurant.com", owner, "+39 041 12345", location,
                PriceRange.LUXURY, true, true, Award.TWO_STARS, true, null, null
        );

        owner.addRestaurant(restaurant);
        assertTrue(owner.modifyRestaurantDescription(restaurant, "New description"));
        assertEquals("New description", restaurant.getDescription());
    }

    @Test
    @DisplayName("Should modify restaurant price range")
    void testModifyRestaurantPriceRange() {
        Restaurant restaurant = new Restaurant(
                UUID.randomUUID(), "My Restaurant", "Description",
                "https://myrestaurant.com", owner, "+39 041 12345", location,
                PriceRange.MODERATE, true, true, Award.NONE, false, null, null
        );

        owner.addRestaurant(restaurant);
        assertTrue(owner.modifyRestaurantPriceRange(restaurant, PriceRange.LUXURY));
        assertEquals(PriceRange.LUXURY, restaurant.getPriceRange());
    }
}
