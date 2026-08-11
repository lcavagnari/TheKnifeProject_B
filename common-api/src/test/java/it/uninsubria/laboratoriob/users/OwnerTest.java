package it.uninsubria.laboratoriob.users;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.enums.UserRole;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Owner Tests")
class OwnerTest {

    private Owner owner;
    private Location location;

    @BeforeEach
    void setUp() {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);

        location = new Location(Nation.ITALY, "Venice", 45.4408, 12.3155, "Piazza San Marco");
        owner = new Owner(UUID.randomUUID(),"restaurateur", "securePass123",salt, "Giuseppe", "Bianchi", location, LocalDate.of(1970, 4, 12));
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
