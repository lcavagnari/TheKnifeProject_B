package it.uninsubria.laboratorioa.objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.CuisineType;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;

@Getter
public class Restaurant extends JsonEntity {

    private final Set<CuisineType> cuisinesTypes;
    private final Set<String> services;
    private final Map<UUID, Review> reviews;

    @Getter(AccessLevel.NONE)
    private final ArrayNode cuisinesTypesArray;
    @Getter(AccessLevel.NONE)
    private final ArrayNode reviewsArray;
    @Getter(AccessLevel.NONE)
    private final ArrayNode servicesArray;


    private String name;
    private final String description;
    private final String websiteUrl;
    private final String phone;

    private final Location loc;
    private final PriceRange priceRange;

    private final boolean hasDelivery;
    private final boolean hasOnlineBooking;

    private final Award award;
    private final boolean greenStar;

    public Restaurant(String name, String description, String websiteUrl, String phone, Location loc, PriceRange priceRange,
                      boolean hasDelivery, boolean hasOnlineBooking, Award award, boolean greenStar, Set<CuisineType> cuisinesTypes,
                      Map<UUID, Review> reviews, Set<String> services) {

        super("companies");

        this.name = (name == null || name.isBlank()) ? "Restaurant" : name;
        this.description = (description == null || description.isBlank()) ? "" : description;
        this.websiteUrl = (websiteUrl == null || websiteUrl.isBlank()) ? "" : websiteUrl;
        this.phone = (phone == null || phone.isBlank()) ? "" : phone;
        this.loc = loc;
        this.priceRange = (priceRange == null) ? PriceRange.MODERATE : priceRange;
        this.hasDelivery = hasDelivery;
        this.hasOnlineBooking = hasOnlineBooking;
        this.award = (award == null) ? Award.NONE : award;
        this.greenStar = greenStar;

        this.cuisinesTypes = (cuisinesTypes == null) ? new HashSet<>() : cuisinesTypes;
        this.services = (services == null) ? new HashSet<>() : services;
        this.reviews = (reviews == null) ? new HashMap<>() : reviews;

        this.reviewsArray = mapper.createArrayNode();
        this.cuisinesTypesArray = mapper.createArrayNode();
        this.servicesArray = mapper.createArrayNode();

        this.cuisinesTypes.forEach(c -> cuisinesTypesArray.add(c.toString()));
        this.services.forEach(servicesArray::add);
        this.reviews.forEach((u, r) -> reviewsArray.add(r.jsonObject));

        build();
    }

    public Restaurant(String name, String description, String websiteUrl, String phone, Location loc,
                      PriceRange priceRange, boolean hasDelivery, boolean hasOnlineBooking) {
        this(name, description, websiteUrl, phone, loc, priceRange, hasDelivery, hasOnlineBooking, Award.NONE, false, null, null, null);
    }

    public Restaurant(Restaurant other) {
        this(other.name, other.description, other.websiteUrl, other.phone, other.loc, other.priceRange,
                other.hasDelivery, other.hasOnlineBooking, other.award, other.greenStar, other.cuisinesTypes,
                other.reviews, other.services);

        this.jsonObject = other.jsonObject.deepCopy();

        this.cuisinesTypesArray.addAll(other.cuisinesTypesArray);
        this.reviewsArray.addAll(other.reviewsArray);
        this.servicesArray.addAll(other.servicesArray);
    }

    @Override
    protected void build() {
        jsonObject.put("name", name).put("address", description).put("phone", phone).put("award", award.getValue()).put("greenStar", greenStar);

        if (loc != null) jsonObject.set("location", loc.jsonObject);

        cuisinesTypesArray.removeAll();
        cuisinesTypes.forEach(c -> cuisinesTypesArray.add(c.toString()));
        jsonObject.set("cuisinesTypes", cuisinesTypesArray);

        servicesArray.removeAll();
        services.forEach(servicesArray::add);
        jsonObject.set("services", servicesArray);

        reviewsArray.removeAll();
        reviews.forEach((u, r) -> reviewsArray.add(r.jsonObject));
        jsonObject.set("reviews", reviewsArray);
    }

    public boolean setName(String newName) {
        if (newName == null || newName.isBlank()) return false;
        this.name = newName;
        rebuild();
        return true;
    }

    public void addReview(Review r) {
        if (r == null || reviews.containsValue(r)) return;
        reviews.put(r.id, r);
        reviewsArray.add(r.jsonObject);
        rebuild();
    }

    public void removeReview(Review r) {
        if (r == null || !reviews.containsValue(r)) return;
        reviews.remove(r.id);
        reviewsArray.removeAll();
        reviews.values().forEach(rv -> reviewsArray.add(rv.jsonObject));
        rebuild();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant that)) return false;
        if (!super.equals(o)) return false;
        return greenStar == that.greenStar && hasDelivery == that.hasDelivery && hasOnlineBooking == that.hasOnlineBooking && award == that.award && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(websiteUrl, that.websiteUrl) && Objects.equals(phone, that.phone) && Objects.equals(loc, that.loc) && priceRange == that.priceRange && Objects.equals(cuisinesTypes, that.cuisinesTypes) && Objects.equals(services, that.services) && Objects.equals(reviews, that.reviews);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, websiteUrl, phone, loc, priceRange, hasDelivery, hasOnlineBooking, award, greenStar, cuisinesTypes, services, reviews);
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "cuisinesTypes=" + cuisinesTypes +
                ", services=" + services +
                ", reviews=" + reviews +
                ", cuisinesTypesArray=" + cuisinesTypesArray +
                ", reviewsArray=" + reviewsArray +
                ", servicesArray=" + servicesArray +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", websiteUrl='" + websiteUrl + '\'' +
                ", phone='" + phone + '\'' +
                ", loc=" + loc +
                ", priceRange=" + priceRange +
                ", hasDelivery=" + hasDelivery +
                ", hasOnlineBooking=" + hasOnlineBooking +
                ", award=" + award +
                ", greenStar=" + greenStar +
                ", id=" + id +
                ", saveFile=" + saveFile +
                ", saveFolder=" + saveFolder +
                '}';
    }
}
