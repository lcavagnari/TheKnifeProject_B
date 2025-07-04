package it.uninsubria.laboratorioa.objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
public class Person extends JsonEntity {
    private final String name;
    private final String lastName;
    private final String dateOfBirth;

    private final Set<Restaurant> favourites;
    private final ArrayNode favouritesArray;

    public Person(String name, String lastName, String dateOfBirth) {
        super("users");
        this.name = name;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;

        this.favourites = new HashSet<>();
        this.favouritesArray = mapper.createArrayNode();

        build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(name, person.name) && Objects.equals(lastName, person.lastName) && Objects.equals(dateOfBirth, person.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, lastName, dateOfBirth);
    }

    @Override
    protected void build() {
        this.jsonObject.put("name",name)
                .put("lastName",lastName)
                .put("dateOfBirth",dateOfBirth);
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                '}';
    }
}
