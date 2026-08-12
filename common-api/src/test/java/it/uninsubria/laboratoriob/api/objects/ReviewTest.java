package it.uninsubria.laboratoriob.api.objects;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Review Tests")
class ReviewTest {

    private Review review;
    private Restaurant restaurant;
    private Customer customer;

    @BeforeEach
    void setUp() {
        Location location = new Location(Nation.ITALY, "Rome", 41.9028, 12.4964, "Via Roma 1");
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);

        Owner owner = new Owner(UUID.randomUUID(),"owner1", "pass123456",salt, "Mario", "Rossi", location, LocalDate.of(1975, 3, 10));
        customer = new Customer(UUID.randomUUID(),"client1", "pass123456",salt, "Luigi", "Verdi", location, LocalDate.of(1990, 7, 20));

        restaurant = new Restaurant(
                UUID.randomUUID(),
                "Test Restaurant",
                "Great food",
                "https://test.com",
                owner,
                "+39 06 12345",
                location,
                PriceRange.MODERATE,
                false,
                true,
                Award.NONE,
                false,
                new HashSet<>(),
                new HashSet<>(),
                new HashMap<>()
        );

        review = new Review(restaurant, customer, 4, "Excellent service and food");
    }

    @Test
    @DisplayName("Should create review with valid data")
    void testReviewCreation() {
        assertNotNull(review);
        assertEquals(4, review.getValue());
        assertEquals("Excellent service and food", review.getText());
        assertEquals(customer, review.getUser());
        assertEquals(restaurant, review.getRestaurant());
        assertNotNull(review.getTimestamp());
    }

    @Test
    @DisplayName("Should create review with timestamp and reply")
    void testReviewWithTimestampAndReply() {
        LocalDateTime timestamp = LocalDateTime.now().minusDays(5);
        Review r = new Review(restaurant, customer, 5, timestamp, "Excellent food and service", "Thank you so much");

        assertEquals(5, r.getValue());
        assertEquals("Excellent food and service", r.getText());
        assertEquals("Thank you so much", r.getReply());
        assertEquals(timestamp, r.getTimestamp());
    }

    @Test
    @DisplayName("Should set value within valid range")
    void testSetValidValue() {
        review.setValue(5);
        assertEquals(5, review.getValue());
    }

    @Test
    @DisplayName("Should not set value below 1")
    void testSetValueBelowMinimum() {
        review.setValue(0);
        assertEquals(4, review.getValue()); // Should remain unchanged
    }

    @Test
    @DisplayName("Should not set value above 5")
    void testSetValueAboveMaximum() {
        review.setValue(6);
        assertEquals(4, review.getValue()); // Should remain unchanged
    }

    @Test
    @DisplayName("Should set text successfully")
    void testSetText() {
        review.setText("Updated review text");
        assertEquals("Updated review text", review.getText());
    }

    @Test
    @DisplayName("Should not set null or blank text")
    void testSetNullText() {
        String originalText = review.getText();
        review.setText(null);
        assertEquals(originalText, review.getText());

        review.setText("");
        assertEquals(originalText, review.getText());
    }

    @Test
    @DisplayName("Should set reply successfully")
    void testSetReply() {
        review.setReply("Thank you for your review!");
        assertEquals("Thank you for your review!", review.getReply());
    }


    @Test
    @DisplayName("Should format toString correctly")
    void testToString() {
        String result = review.toString();
        assertTrue(result.contains("4 / 5"));
        assertTrue(result.contains("Excellent service and food"));
    }
}

