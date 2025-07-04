package it.uninsubria.laboratorioa.objects;

import it.uninsubria.laboratorioa.objects.enums.Nation;

public class Location extends JsonEntity {

    private Nation nation;
    private String city;
    private long latitude;
    private long longitude;
    private String address;

    public Location(Nation nation, String city, long latitude, long longitude, String address) {
        super("companies");

        this.nation = nation;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public Location(String address) {

    }

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
}
