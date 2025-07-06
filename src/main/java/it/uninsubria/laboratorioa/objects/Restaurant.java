package it.uninsubria.laboratorioa.objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.CuisineType;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import it.uninsubria.laboratorioa.objects.users.Owner;
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
    private final String description;
    private final String websiteUrl;
    private final String phone;
    private final Owner owner;
    private final Location loc;
    private final PriceRange priceRange;
    private final boolean hasDelivery;
    private final boolean hasOnlineBooking;
    private final Award award;
    private final boolean greenStar;
    private String name;

    public Restaurant(String name, String description, String websiteUrl, Owner owner, String phone, Location loc, PriceRange priceRange,
                      boolean hasDelivery, boolean hasOnlineBooking, Award award, boolean greenStar, Set<CuisineType> cuisinesTypes,
                      Map<UUID, Review> reviews, Set<String> services) {

        super("restaurants");

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

        this.owner = owner;

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

    public Restaurant(String name, String description, String websiteUrl, Owner owner, String phone, Location loc, PriceRange priceRange,
                      boolean hasDelivery, boolean hasOnlineBooking, Award award, boolean greenStar, Set<CuisineType> cuisinesTypes, Set<String> services) {

        super("restaurants");
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

        this.owner = owner;

        this.cuisinesTypes = (cuisinesTypes == null) ? new HashSet<>() : cuisinesTypes;
        this.services = (services == null) ? new HashSet<>() : services;
        this.reviews = new HashMap<>();

        this.reviewsArray = mapper.createArrayNode();
        this.cuisinesTypesArray = mapper.createArrayNode();
        this.servicesArray = mapper.createArrayNode();
    }


    @Override
    protected void build() {
        jsonObject.put("owner", owner.getId().toString())
                .put("name", name)
                .put("address", description)
                .put("phone", phone)
                .put("award", award.getValue())
                .put("greenStar", greenStar);

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
                "\ncuisinesTypes=" + cuisinesTypes +
                "\nservices=" + services +
                "\nreviews=" + reviews +
                "\nname='" + name + '\'' +
                "\ndescription='" + description + '\'' +
                "\nwebsiteUrl='" + websiteUrl + '\'' +
                "\nphone='" + phone + '\'' +
                "\nloc=" + loc +
                "\npriceRange=" + priceRange +
                "\nhasDelivery=" + hasDelivery +
                "\nhasOnlineBooking=" + hasOnlineBooking +
                "\naward=" + award +
                "\ngreenStar=" + greenStar +
                "\nid=" + id +
                "\nsaveFile=" + saveFile +
                '}';
    }
}
