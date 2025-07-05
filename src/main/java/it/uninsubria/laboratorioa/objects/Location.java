package it.uninsubria.laboratorioa.objects;

import it.uninsubria.laboratorioa.objects.enums.Nation;
import lombok.Getter;

@Getter
public class Location extends JsonEntity {

    private Nation nation;
    private String city;
    private double latitude;
    private double longitude;
    private String address;

    public Location(Nation nation, String city, double latitude, double longitude, String address) {
        super("companies");

        this.nation = nation;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;

        this.jsonObject.remove("id");
        build();
    }

    /*
    public Location(String address) {

    }


     */
    @Override
    protected void build() {
        this.jsonObject.put("nation", ""+nation)
                .put("city", city)
                .put("address", address)
                .put("latitude", latitude)
                .put("longitude", longitude);
    }

    @Override
    public boolean save() {
        return false;
    }

    @Override
    public String toString() {
        return "Location{" +
                "nation=" + nation +
                ", city='" + city + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                '}';
    }
}
