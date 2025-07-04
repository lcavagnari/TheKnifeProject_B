package it.uninsubria.laboratorioa.objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;

import java.time.LocalDate;
import java.util.*;

public class Restaurant extends JsonEntity {

    @Getter private String address;
    @Getter private String phone;

    @Getter private String email;
    @Getter private int foundationYear;


    private final Location loc;
    private final

    private String name;
    private final Set<Person> cuisinesTypes = new HashSet<>();
    private final Map<UUID,Review> reviews = new HashMap<>();

    private final ArrayNode cuisinesTypesArray;
    private final ArrayNode reviewsArray;


    public Restaurant(String name, String address, String phone, String email, int foundationYear) {
        super("companies");

        this.name = (name == null || name.isBlank()) ? "Company" : name;
        this.address = (address == null || address.isBlank()) ? "" : address;
        this.phone = (phone == null || phone.isBlank()) ? "" : phone;
        this.email = (email == null || email.isBlank()) ? "" : email;
        this.foundationYear = Math.max(Math.min(foundationYear,LocalDate.now().getYear()),LocalDate.MIN.getYear());

        this.reviewsArray = mapper.createArrayNode();
        this.cuisinesTypesArray = mapper.createArrayNode();

        build();
    }

    public Restaurant(UUID id, String name, String address, String phone, String email, int foundationYear) {
        this(name,address,phone,email,foundationYear);

        if (id != null) this.id = id;
    }

    public Restaurant(String name, String address, String phone, String email, int foundationYear, Set<Person> cuisinesTypes, Map<UUID,Review> reviews) {
        this(name,address,phone,email,foundationYear);

        if (cuisinesTypes != null && !cuisinesTypes.isEmpty())
            for (Person p : cuisinesTypes) cuisinesTypesArray.add(p.jsonObject);

        if (reviews != null && !reviews.isEmpty())
            for (Review r : reviews.values()) reviewsArray.add(r.jsonObject);
    }

    public Restaurant(Restaurant c) {
        this(c.name,c.address,c.phone,c.email,c.foundationYear);
        this.jsonObject = c.jsonObject.deepCopy();

        this.cuisinesTypesArray.addAll(c.cuisinesTypesArray);
        this.reviewsArray.addAll(c.reviewsArray);

    }

    @Override
    protected void build() {
        this.jsonObject.put("name", name)
                .put("address", address)
                .put("phone", phone)
                .put("email", email)
                .put("foundationYear", foundationYear);

        jsonObject.set("cuisinesTypes", cuisinesTypesArray);
        jsonObject.set("reviews", reviewsArray);
    }



    public boolean setName(String companyName) {
        if (companyName == null || companyName.isBlank()) return false;

        this.name = companyName;

        rebuild();

        return true;
    }

    public boolean setFoundationYear(int foundationYear) {
        if (foundationYear > LocalDate.now().getYear() || foundationYear < LocalDate.MIN.getYear()) return false;
        this.foundationYear = foundationYear;

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Restaurant restaurant = (Restaurant) o;
        return foundationYear == restaurant.foundationYear && Objects.equals(address, restaurant.address) && Objects.equals(phone, restaurant.phone) && Objects.equals(email, restaurant.email) && Objects.equals(name, restaurant.name) && Objects.equals(cuisinesTypes, restaurant.cuisinesTypes) && Objects.equals(reviews, restaurant.reviews);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), address, phone, email, foundationYear, name, cuisinesTypes, reviews);
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
                "address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", foundationYear=" + foundationYear +
                ", companyName='" + name + '\'' +
                ", employees=" + cuisinesTypes +
                ", reviews=" + reviews +
                ", employeesArray=" + cuisinesTypesArray +
                ", reviewsArray=" + reviewsArray +
                ", id=" + id +
                '}';
    }
}
