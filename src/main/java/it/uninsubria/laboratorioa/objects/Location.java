package it.uninsubria.laboratorioa.objects;

import it.uninsubria.laboratorioa.objects.enums.Nation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Location extends JsonEntity {


    private Nation nation;
    private String city;
    private String address;

    private double latitude;
    private double longitude;

    public Location(Nation nation, String city, double latitude, double longitude, String address) {
        this.nation = nation;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;

        this.jsonObject.remove("id");
        build();
    }

    @Override
    protected void build() {
        this.jsonObject.put("nation", String.valueOf(nation))
                .put("city", city)
                .put("address", address)
                .put("latitude", latitude)
                .put("longitude", longitude);
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


    @Override
    public boolean save() {
        build();
        return true;
    }
}
