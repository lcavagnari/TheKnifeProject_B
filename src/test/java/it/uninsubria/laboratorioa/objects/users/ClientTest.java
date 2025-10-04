package it.uninsubria.laboratorioa.objects.users;

import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Client Tests")
class ClientTest {

    private Client client;
    private Location location;

    @BeforeEach
    void setUp() {
        location = new Location(Nation.ITALY, "Florence", 43.7696, 11.2558, "Piazza della Signoria");
        client = new Client("johndoe", "securePass123", "JohnName", "DoeLastName", location, LocalDate.of(1995, 8, 25));
    }

    @Test
    @DisplayName("Should create client with valid credentials")
    void testClientCreation() {
        assertNotNull(client);
        assertEquals("johndoe", client.getUsername());
        assertEquals("JohnName", client.getName());
        assertEquals("DoeLastName", client.getLastName());
        assertEquals(UserRole.CLIENT, client.getRole());
        assertNotNull(client.getFavouriteRestourants());
        assertTrue(client.getFavouriteRestourants().isEmpty());
    }

    @Test
    @DisplayName("Should verify correct password")
    void testPasswordVerification() {
        assertTrue(client.verifyPassword("securePass123"));
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void testPasswordVerificationFails() {
        assertFalse(client.verifyPassword("wrongPassword"));
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

        assertTrue(client.addFavourite(restaurant));
        assertTrue(client.getFavouriteRestourants().contains(restaurant.getId()));
    }

    @Test
    @DisplayName("Should not add null restaurant to favourites")
    void testAddNullFavourite() {
        assertFalse(client.addFavourite(null));
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

        client.addFavourite(restaurant);
        assertTrue(client.removeFavourite(restaurant));
        assertFalse(client.getFavouriteRestourants().contains(restaurant.getId()));
    }

    @Test
    @DisplayName("Should build JSON object with favourites")
    void testBuildJsonObject() {
        client.build();
        assertNotNull(client.getJsonObject());
        assertEquals("johndoe", client.getJsonObject().get("username").asText());
        assertTrue(client.getJsonObject().has("favourites"));
    }
}
