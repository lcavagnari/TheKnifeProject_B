package it.uninsubria.laboratorioa.objects.users;

import it.uninsubria.laboratorioa.objects.Location;

import java.time.LocalDate;

public class Owner extends User {

    public Owner(String username, String password, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, password, name, lastName, location, dateOfBirth);
    }

    public Owner(String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, name, lastName, location, dateOfBirth, password, salt);
    }

    @Override
    protected void build() {
        super.build();

        jsonObject.put("role", "Owner");
    }
}
