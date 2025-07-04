package it.uninsubria.laboratorioa.objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import it.uninsubria.laboratorioa.objects.enums.CuisineType;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.LocalDate;
import java.util.*;


@Getter
public class Restaurant extends JsonEntity {
    
    @Getter(AccessLevel.NONE) private final Set<CuisineType> cuisinesTypes = new HashSet<>();
    @Getter(AccessLevel.NONE) private final Map<UUID,Review> reviews = new HashMap<>();

    @Getter(AccessLevel.NONE) private final ArrayNode cuisinesTypesArray;
    @Getter(AccessLevel.NONE) private final ArrayNode reviewsArray;

    private String name;
    private String description;
    private String websiteUrl;

    private String phone;

    private Location loc;
    private PriceRange priceRange;

    private boolean hasDelivery;
    private boolean hasOnlineBooking;

    public Restaurant(String name, String description, String websiteUrl, String phone, Location loc,
                      PriceRange priceRange, boolean hasDelivery, boolean hasOnlineBooking) {

        super("companies");

        this.name = (name == null || name.isBlank()) ? "Resturant" : name;
        this.description = (description == null || description.isBlank()) ? "" : description;
        this.websiteUrl = (websiteUrl == null || websiteUrl.isBlank()) ? "" : websiteUrl;

        this.phone = (phone == null || phone.isBlank()) ? "" : phone;

        this.loc = (loc == null) ? new Location() : loc;
        this.priceRange = (priceRange == null) ? PriceRange.MODERATE : priceRange;

        this.hasDelivery = hasDelivery;
        this.hasOnlineBooking = hasOnlineBooking;

        this.reviewsArray = mapper.createArrayNode();
        this.cuisinesTypesArray = mapper.createArrayNode();
        build();
    }


    public Restaurant(Restaurant c) {
        this(c.name,c.description,c.websiteUrl,c.phone,c.loc,c.priceRange,c.hasDelivery,c.hasOnlineBooking);
        this.jsonObject = c.jsonObject.deepCopy();

        this.cuisinesTypesArray.addAll(c.cuisinesTypesArray);
        this.reviewsArray.addAll(c.reviewsArray);

    }

    @Override
    protected void build() {
        this.jsonObject.put("name", name)
                .put("address", description)
                .put("phone", phone);

        jsonObject.set("cuisinesTypes", cuisinesTypesArray);
        jsonObject.set("reviews", reviewsArray);
    }



    public boolean setName(String companyName) {
        if (companyName == null || companyName.isBlank()) return false;

        this.name = companyName;

        rebuild();

        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Restaurant restaurant = (Restaurant) o;
        return Objects.equals(description, restaurant.description) && Objects.equals(phone, restaurant.phone) && Objects.equals(name, restaurant.name) && Objects.equals(cuisinesTypes, restaurant.cuisinesTypes) && Objects.equals(reviews, restaurant.reviews);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description, phone, name, cuisinesTypes, reviews);
    }


    // Employees

    public void addEmployee(Person p) {
        if (cuisinesTypes.contains(p)) return;

        cuisinesTypes.add(p);
        cuisinesTypesArray.add(p.jsonObject);

        build();
    }

    public void addEmployee(String name, String lastName, String date) {
        addEmployee(new Person(name,lastName,date));
    }



    public void removeEmployee(Person p) {
        if (p == null && !cuisinesTypes.contains(p)) return;

        cuisinesTypesArray.removeAll();
        cuisinesTypes.remove(p);

        for (Person e : cuisinesTypes) cuisinesTypesArray.add(e.jsonObject);

        rebuild();
    }

    // Recensioni

    public void addReview(Review r) {
        if (r == null && reviews.containsValue(r)) return;

        reviews.put(r.id,r);
        reviewsArray.add(r.jsonObject);
    }

    public void removeReview(Review r) {
        if (r == null || !reviews.containsValue(r)) return;

        cuisinesTypesArray.removeAll();
        reviews.remove(r);

        reviewsArray.forEach(reviewsArray::add);

        rebuild();
    }

    @Override
    public String toString() {
        return "Company{" +
                "address='" + description + '\'' +
                ", phone='" + phone + '\'' +
                ", companyName='" + name + '\'' +
                ", employees=" + cuisinesTypes +
                ", reviews=" + reviews +
                ", employeesArray=" + cuisinesTypesArray +
                ", reviewsArray=" + reviewsArray +
                ", id=" + id +
                '}';
    }
}
