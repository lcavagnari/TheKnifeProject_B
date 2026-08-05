package it.uninsubria.laboratoriob.users;

import it.uninsubria.laboratoriob.enums.Award;
import it.uninsubria.laboratoriob.enums.Nation;
import it.uninsubria.laboratoriob.enums.PriceRange;
import it.uninsubria.laboratoriob.enums.UserRole;
import it.uninsubria.laboratoriob.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Client Tests")
class CustomerTest {

    private Customer customer;
    private Location location;

    @BeforeEach
    void setUp() {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);

        location = new Location(Nation.ITALY, "Florence", 43.7696, 11.2558, "Piazza della Signoria");
        customer = new Customer(UUID.randomUUID(),"johndoe", "securePass123",salt, "JohnName", "DoeLastName", location, LocalDate.of(1995, 8, 25));
    }

    @Test
    @DisplayName("Should create client with valid credentials")
    void testClientCreation() {
        assertNotNull(customer);
        assertEquals("johndoe", customer.getUsername());
        assertEquals("JohnName", customer.getName());
        assertEquals("DoeLastName", customer.getLastName());
        assertEquals(UserRole.CLIENT, customer.getRole());
        assertNotNull(customer.getFavouriteRestourants());
        assertTrue(customer.getFavouriteRestourants().isEmpty());
    }

    @Test
    @DisplayName("Should add favourite restaurant")
    void testAddFavourite() {
        Owner owner = new Owner("owner1", "pass123456", "Mario", "Rossi", location, LocalDate.of(1980, 1, 1));
        Restaurant restaurant = new Restaurant(
                UUID.randomUUID(), "Test Restaurant", "Great place",
                "https://test.com", owner, "+39 055 12345", location,
                PriceRange.MODERATE, false, true, Award.NONE, false, null, null
        );

        assertTrue(customer.addFavourite(restaurant));
        assertTrue(customer.getFavouriteRestourants().contains(restaurant.getId()));
    }

    @Test
    @DisplayName("Should not add null restaurant to favourites")
    void testAddNullFavourite() {
        assertFalse(customer.addFavourite(null));
    }

    @Test
    @DisplayName("Should remove favourite restaurant")
    void testRemoveFavourite() {
        Owner owner = new Owner("owner1", "pass123456", "Mario", "Rossi", location, LocalDate.of(1980, 1, 1));
        Restaurant restaurant = new Restaurant(
                UUID.randomUUID(), "Test Restaurant", "Great place",
                "https://test.com", owner, "+39 055 12345", location,
                PriceRange.MODERATE, false, true, Award.NONE, false, null, null
        );

        customer.addFavourite(restaurant);
        assertTrue(customer.removeFavourite(restaurant));
        assertFalse(customer.getFavouriteRestourants().contains(restaurant.getId()));
    }
}
