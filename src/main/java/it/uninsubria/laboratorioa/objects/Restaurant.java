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

    @Getter(AccessLevel.NONE) private final Set<CuisineType> cuisinesTypes;
    @Getter(AccessLevel.NONE) private final Map<UUID, Review> reviews;

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

    private Award award;
    private boolean greenStar;

    public Restaurant(String name,
                      String description,
                      String websiteUrl,
                      String phone,
                      Location loc,
                      PriceRange priceRange,
                      boolean hasDelivery,
                      boolean hasOnlineBooking,
                      Set<CuisineType> cuisinesTypes,
                      Map<UUID, Review> reviews,
                      Award award,
                      boolean greenStar) {
        super("companies");

        this.name             = (name == null || name.isBlank())       ? "Restaurant" : name;
        this.description      = (description == null || description.isBlank()) ? "" : description;
        this.websiteUrl       = (websiteUrl == null || websiteUrl.isBlank())   ? "" : websiteUrl;
        this.phone            = (phone == null || phone.isBlank())     ? "" : phone;
        this.loc              = loc;
        this.priceRange       = (priceRange == null)                  ? PriceRange.MODERATE : priceRange;
        this.hasDelivery      = hasDelivery;
        this.hasOnlineBooking = hasOnlineBooking;
        this.cuisinesTypes    = (cuisinesTypes == null)               ? new HashSet<>() : cuisinesTypes;
        this.reviews          = (reviews == null)                     ? new HashMap<>() : reviews;
        this.award            = (award == null)                       ? Award.NONE : award;
        this.greenStar        = greenStar;

        this.reviewsArray      = mapper.createArrayNode();
        this.cuisinesTypesArray = mapper.createArrayNode();

        this.cuisinesTypes.forEach(c -> cuisinesTypesArray.add(c.toString()));
        this.reviews.forEach((u, r) -> reviewsArray.add(r.jsonObject));

        build();
    }

    public Restaurant(String name,
                      String description,
                      String websiteUrl,
                      String phone,
                      Location loc,
                      PriceRange priceRange,
                      boolean hasDelivery,
                      boolean hasOnlineBooking) {
        this(name, description, websiteUrl, phone, loc, priceRange,
                hasDelivery, hasOnlineBooking,
                new HashSet<>(), new HashMap<>(),
                Award.NONE, false);
    }

    public Restaurant(Restaurant other) {
        this(other.name, other.description, other.websiteUrl, other.phone,
                other.loc, other.priceRange,
                other.hasDelivery, other.hasOnlineBooking,
                other.cuisinesTypes, other.reviews,
                other.award, other.greenStar);
        this.jsonObject = other.jsonObject.deepCopy();
        this.cuisinesTypesArray.addAll(other.cuisinesTypesArray);
        this.reviewsArray.addAll(other.reviewsArray);
    }

    @Override
    protected void build() {
        jsonObject.put("name", name)
                .put("address", description)
                .put("phone", phone)
                .put("award", award.getValue())
                .put("greenStar", greenStar);

        if (loc != null) {
            jsonObject.set("location", loc.jsonObject);
        }

        cuisinesTypesArray.removeAll();
        cuisinesTypes.forEach(c -> cuisinesTypesArray.add(c.toString()));
        jsonObject.set("cuisinesTypes", cuisinesTypesArray);

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
        if (!(o instanceof Restaurant)) return false;
        if (!super.equals(o)) return false;
        Restaurant that = (Restaurant) o;
        return greenStar == that.greenStar &&
                hasDelivery == that.hasDelivery &&
                hasOnlineBooking == that.hasOnlineBooking &&
                award == that.award &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(websiteUrl, that.websiteUrl) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(loc, that.loc) &&
                priceRange == that.priceRange &&
                Objects.equals(cuisinesTypes, that.cuisinesTypes) &&
                Objects.equals(reviews, that.reviews);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, websiteUrl, phone,
                loc, priceRange, hasDelivery, hasOnlineBooking,
                award, greenStar, cuisinesTypes, reviews);
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", websiteUrl='" + websiteUrl + '\'' +
                ", phone='" + phone + '\'' +
                ", loc=" + loc +
                ", priceRange=" + priceRange +
                ", hasDelivery=" + hasDelivery +
                ", hasOnlineBooking=" + hasOnlineBooking +
                ", award=" + award +
                ", greenStar=" + greenStar +
                ", cuisinesTypes=" + cuisinesTypes +
                ", reviews=" + reviews +
                '}';
    }
}
