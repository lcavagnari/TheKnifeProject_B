package it.uninsubria.laboratorioa.jsonentities;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;

import java.time.LocalDate;
import java.util.*;

public class Company extends JsonEntity {

    @Getter private String address;
    @Getter private String phone;
    @Getter private String email;
    @Getter private int foundationYear;

    private String companyName;
    private final Set<Person> employees = new HashSet<>();
    private final Map<UUID,Review> reviews = new HashMap<>();

    private final ArrayNode employeesArray;
    private final ArrayNode reviewsArray;


    public Company(String name, String address, String phone, String email, int foundationYear) {
        super("companies");

        this.companyName = (name == null || name.isBlank()) ? "Company" : name;
        this.address = (address == null || address.isBlank()) ? "" : address;
        this.phone = (phone == null || phone.isBlank()) ? "" : phone;
        this.email = (email == null || email.isBlank()) ? "" : email;
        this.foundationYear = Math.max(Math.min(foundationYear,LocalDate.now().getYear()),LocalDate.MIN.getYear());

        this.reviewsArray = mapper.createArrayNode();
        this.employeesArray = mapper.createArrayNode();

        build();
    }

    public Company(UUID id, String name, String address, String phone, String email, int foundationYear) {
        this(name,address,phone,email,foundationYear);

        if (id != null) this.id = id;
    }

    public Company(String name, String address, String phone, String email, int foundationYear, Set<Person> employees, Map<UUID,Review> reviews) {
        this(name,address,phone,email,foundationYear);

        if (employees != null && !employees.isEmpty())
            for (Person p : employees) employeesArray.add(p.jsonObject);

        if (reviews != null && !reviews.isEmpty())
            for (Review r : reviews.values()) reviewsArray.add(r.jsonObject);
    }

    public Company(Company c) {
        this(c.companyName,c.address,c.phone,c.email,c.foundationYear);
        this.jsonObject = c.jsonObject.deepCopy();

        this.employeesArray.addAll(c.employeesArray);
        this.reviewsArray.addAll(c.reviewsArray);

    }

    @Override
    protected void build() {
        this.jsonObject.put("name", companyName)
                .put("address", address)
                .put("phone", phone)
                .put("email", email)
                .put("foundationYear", foundationYear);

        jsonObject.set("employees", employeesArray);
        jsonObject.set("reviews", reviewsArray);
    }



    public boolean setCompanyName(String companyName) {
        if (companyName == null || companyName.isBlank()) return false;

        this.companyName = companyName;

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
        Company company = (Company) o;
        return foundationYear == company.foundationYear && Objects.equals(address, company.address) && Objects.equals(phone, company.phone) && Objects.equals(email, company.email) && Objects.equals(companyName, company.companyName) && Objects.equals(employees, company.employees) && Objects.equals(reviews, company.reviews);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), address, phone, email, foundationYear, companyName, employees, reviews);
    }


    // Employees

    public void addEmployee(Person p) {
        if (employees.contains(p)) return;

        employees.add(p);
        employeesArray.add(p.jsonObject);

        build();
    }

    public void addEmployee(String name, String lastName, String date) {
        addEmployee(new Person(name,lastName,date));
    }



    public void removeEmployee(Person p) {
        if (p == null && !employees.contains(p)) return;

        employeesArray.removeAll();
        employees.remove(p);

        for (Person e : employees) employeesArray.add(e.jsonObject);

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

        employeesArray.removeAll();
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
                ", companyName='" + companyName + '\'' +
                ", employees=" + employees +
                ", reviews=" + reviews +
                ", employeesArray=" + employeesArray +
                ", reviewsArray=" + reviewsArray +
                ", id=" + id +
                '}';
    }
}
