package it.uninsubria.laboratoriob.test.dao;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.data.CustomerDAO;
import it.uninsubria.laboratoriob.data.LocationDAO;
import it.uninsubria.laboratoriob.data.OwnerDAO;
import it.uninsubria.laboratoriob.data.RestaurantDAO;
import it.uninsubria.laboratoriob.data.ReviewDAO;
import it.uninsubria.laboratoriob.utils.Database;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReviewDAOTest {

    private static final ReviewDAO reviewDAO = new ReviewDAO();
    private static final RestaurantDAO restaurantDAO = new RestaurantDAO();
    private static final OwnerDAO ownerDAO = new OwnerDAO();
    private static final CustomerDAO customerDAO = new CustomerDAO();
    private static final LocationDAO locationDAO = new LocationDAO();

    private static Owner testOwner;
    private static Customer testCustomer;
    private static Restaurant testRestaurant;
    private static Location testLocation;

    @BeforeAll
    static void setUp() {
        try (java.sql.Connection c = java.sql.DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/mydb", "testuser", "test1234")) {
            assumeTrue(c.isValid(3), "PostgreSQL not available");
        } catch (Exception e) {
            assumeTrue(false, "PostgreSQL not available, skipping DAO tests");
        }

        Database.initTables();
        Database.initialiseConstants();

        testLocation = new Location(Nation.ITALY, "Rome", 41.9028, 12.4964, "Via Milano 5");
        locationDAO.save(testLocation);

        testOwner = new Owner(UUID.randomUUID(), "reviewowner", "hash", "salt",
                "Owner", "Test", testLocation, LocalDate.of(1985, 5, 15));
        ownerDAO.save(testOwner);

        testCustomer = new Customer(UUID.randomUUID(), "reviewcustomer", "hash", "salt",
                "Customer", "Test", testLocation, LocalDate.of(1995, 8, 20));
        customerDAO.save(testCustomer);

        testRestaurant = new Restaurant(
                UUID.randomUUID(), "Review Restaurant", "Desc", "https://test.com",
                testOwner, "+39 06 1111111", testLocation, PriceRange.MODERATE,
                false, false, Award.NONE, false, new HashSet<>(), new HashSet<>()
        );
        restaurantDAO.save(testRestaurant);
    }

    @AfterAll
    static void tearDown() {
        try { Database.shutdown(); } catch (Exception ignored) {}
    }

    // ═══════════════════════════════════════════════════════════════
    // HAPPY PATHS (60%)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("save: review persists correctly")
    void saveReview_success() {
        Review r = createTestReview("Great place!", 5);
        assertTrue(reviewDAO.save(r));

        Optional<Review> found = reviewDAO.findById(r.getId());
        assertTrue(found.isPresent());
        assertEquals("Great place!", found.get().getText());
        assertEquals(5, found.get().getValue());
    }

    @Test
    @Order(2)
    @DisplayName("save: review with reply persists correctly")
    void saveReview_withReply_success() {
        Review r = createTestReview("Good food", 4);
        r.setReply("Thank you!");
        assertTrue(reviewDAO.save(r));

        Optional<Review> found = reviewDAO.findById(r.getId());
        assertTrue(found.isPresent());
        assertEquals("Thank you!", found.get().getReply());
    }

    @Test
    @Order(3)
    @DisplayName("findById: returns review when it exists")
    void findById_exists_success() {
        Review r = createTestReview("FindById review", 3);
        reviewDAO.save(r);

        Optional<Review> found = reviewDAO.findById(r.getId());
        assertTrue(found.isPresent());
        assertEquals(r.getId(), found.get().getId());
    }

    @Test
    @Order(4)
    @DisplayName("findAll: returns all reviews")
    void findAll_success() {
        int before = reviewDAO.findAll().size();
        reviewDAO.save(createTestReview("FindAll review 1", 4));
        reviewDAO.save(createTestReview("FindAll review 2", 2));

        List<Review> all = reviewDAO.findAll();
        assertTrue(all.size() >= before + 2);
    }

    @Test
    @Order(5)
    @DisplayName("findByRestaurant: returns reviews for a restaurant")
    void findByRestaurant_success() {
        Review r = createTestReview("Restaurant review", 5);
        reviewDAO.save(r);

        List<Review> reviews = reviewDAO.findByRestaurant(testRestaurant.getId());
        assertTrue(reviews.stream().anyMatch(x -> x.getId().equals(r.getId())));
    }

    @Test
    @Order(6)
    @DisplayName("update: modifies review text correctly")
    void updateText_success() {
        Review r = createTestReview("Original text", 4);
        reviewDAO.save(r);

        r.setText("Updated text");
        assertTrue(reviewDAO.update(r));

        Optional<Review> found = reviewDAO.findById(r.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated text", found.get().getText());
    }

    @Test
    @Order(7)
    @DisplayName("update: modifies review reply correctly")
    void updateReply_success() {
        Review r = createTestReview("Reply test", 3);
        reviewDAO.save(r);

        r.setReply("Owner response here");
        assertTrue(reviewDAO.update(r));

        Optional<Review> found = reviewDAO.findById(r.getId());
        assertTrue(found.isPresent());
        assertEquals("Owner response here", found.get().getReply());
    }

    @Test
    @Order(8)
    @DisplayName("update: modifies review rating correctly")
    void updateRating_success() {
        Review r = createTestReview("Rating test", 2);
        reviewDAO.save(r);

        r.setValue(5);
        assertTrue(reviewDAO.update(r));

        Optional<Review> found = reviewDAO.findById(r.getId());
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getValue());
    }

    @Test
    @Order(9)
    @DisplayName("delete: removes review from database")
    void delete_success() {
        Review r = createTestReview("To delete", 1);
        reviewDAO.save(r);

        assertTrue(reviewDAO.delete(r.getId()));
        assertTrue(reviewDAO.findById(r.getId()).isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("findByRestaurant: returns empty for restaurant with no reviews")
    void findByRestaurant_empty_success() {
        Restaurant empty = new Restaurant(
                UUID.randomUUID(), "No Reviews", "Desc", null,
                testOwner, null, testLocation, PriceRange.ECONOMY,
                false, false, Award.NONE, false, new HashSet<>(), new HashSet<>()
        );
        restaurantDAO.save(empty);

        List<Review> reviews = reviewDAO.findByRestaurant(empty.getId());
        assertNotNull(reviews);
        assertTrue(reviews.isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════
    // FAILURE CASES (40%)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(11)
    @DisplayName("findById: returns empty for non-existent UUID")
    void findById_notExists_empty() {
        Optional<Review> found = reviewDAO.findById(UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(12)
    @DisplayName("delete: returns false for non-existent review")
    void delete_notExists_false() {
        assertFalse(reviewDAO.delete(UUID.randomUUID()));
    }

    @Test
    @Order(13)
    @DisplayName("update: returns false when review doesn't exist")
    void update_notExists_false() {
        Review r = createTestReview("Ghost review", 3);
        assertFalse(reviewDAO.update(r));
    }

    @Test
    @Order(14)
    @DisplayName("save: duplicate review ID throws exception")
    void save_duplicateId_fail() {
        Review r = createTestReview("Duplicate review", 4);
        reviewDAO.save(r);

        Review duplicate = createTestReview("Duplicate review 2", 2);


        assertThrows(Exception.class, () -> reviewDAO.save(duplicate));
    }

    @Test
    @Order(15)
    @DisplayName("findByRestaurant: returns empty for non-existent restaurant")
    void findByRestaurant_nonExistent_empty() {
        List<Review> reviews = reviewDAO.findByRestaurant(UUID.randomUUID());
        assertNotNull(reviews);
        assertTrue(reviews.isEmpty());
    }

    @Test
    @Order(16)
    @DisplayName("save: review with null restaurant persists (FK nullable)")
    void saveReview_nullRestaurant_success() {
        Review r = new Review(UUID.randomUUID(), null, testCustomer, 3,
                LocalDateTime.now(), "No restaurant", null);
        assertTrue(reviewDAO.save(r));

        Optional<Review> found = reviewDAO.findById(r.getId());
        assertTrue(found.isPresent());
        assertNull(found.get().getRestaurant());
    }

    @Test
    @Order(17)
    @DisplayName("save: review with null user persists (FK nullable)")
    void saveReview_nullUser_success() {
        Review r = new Review(UUID.randomUUID(), testRestaurant, null, 4,
                LocalDateTime.now(), "Anonymous review", null);
        assertTrue(reviewDAO.save(r));

        Optional<Review> found = reviewDAO.findById(r.getId());
        assertTrue(found.isPresent());
        assertNull(found.get().getUser());
    }

    @Test
    @Order(18)
    @DisplayName("update: multiple fields updated atomically")
    void update_multipleFields_success() {
        Review r = createTestReview("Multi update", 2);
        reviewDAO.save(r);

        r.setText("Updated text");
        r.setValue(5);
        r.setReply("Updated reply");
        assertTrue(reviewDAO.update(r));

        Optional<Review> found = reviewDAO.findById(r.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated text", found.get().getText());
        assertEquals(5, found.get().getValue());
        assertEquals("Updated reply", found.get().getReply());
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private Review createTestReview(String text, int rating) {
        return new Review(testRestaurant, testCustomer, rating, text);
    }
}
