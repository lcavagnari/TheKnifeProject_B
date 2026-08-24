package com.example.demo3;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Set;
import java.util.stream.Collectors;

public class RestaurantDetailsController {

    @FXML
    private Label nameLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label websiteLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label priceRangeLabel;
    @FXML
    private Label deliveryLabel;
    @FXML
    private Label onlineBookingLabel;
    @FXML
    private Label awardLabel;
    @FXML
    private Label greenStarLabel;
    @FXML
    private Label ownerIdLabel;
    @FXML
    private Label cuisineTypesLabel;
    @FXML
    private Label servicesLabel;
    @FXML
    private Label reviewsCountLabel;
    @FXML
    private Label idLabel;
    @FXML
    private Button btnIndietro;

    public void carica(Restaurant r) {
        nameLabel.setText(valoreO(r.getName(), "Senza nome"));
        descriptionLabel.setText(valoreO(r.getDescription(), "?"));
        websiteLabel.setText(valoreO(r.getWebsiteUrl(), "?"));
        phoneLabel.setText(valoreO(r.getPhone(), "?"));
        locationLabel.setText(formattaLocation(r.getLocation()));
        priceRangeLabel.setText(r.getPriceRange() != null ? r.getPriceRange().toString() : "?");
        deliveryLabel.setText(formattaBooleano(r.isHasDelivery()));
        onlineBookingLabel.setText(formattaBooleano(r.isHasOnlineBooking()));
        awardLabel.setText(r.getAward() != null ? r.getAward().toString() : "?");
        greenStarLabel.setText(formattaBooleano(r.isGreenStar()));
        ownerIdLabel.setText(r.getOwner() != null ? r.getOwner().getId().toString() : "N/A");
        cuisineTypesLabel.setText(formattaCucine(r.getCuisinesTypes()));
        servicesLabel.setText(formattaLista(r.getServices()));
        int numRecensioni = r.getReviews() != null ? r.getReviews().size() : 0;
        reviewsCountLabel.setText(numRecensioni + (numRecensioni == 0
                ? " - Nessuna recensione disponibile" : ""));
        idLabel.setText(r.getId() != null ? r.getId().toString() : "?");
    }

    @FXML
    private void onIndietroClick() {
        Stage stage = (Stage) btnIndietro.getScene().getWindow();
        Navigation.navigateTo(stage, "GUI.fxml", "The Knife Menu", 600, 400);
    }

    private String valoreO(String valore, String fallback) {
        return (valore == null || valore.isBlank()) ? fallback : valore;
    }

    private String formattaBooleano(boolean valore) {
        return valore ? "Sì" : "No";
    }

    private String formattaLista(Set<String> insieme) {
        if (insieme == null || insieme.isEmpty()) return "?";
        return String.join(", ", insieme);
    }

    private String formattaCucine(Set<CuisineType> insieme) {
        if (insieme == null || insieme.isEmpty()) return "?";
        return insieme.stream().map(CuisineType::toString).collect(Collectors.joining(", "));
    }

    private String formattaLocation(Location loc) {
        if (loc == null) return "?";
        StringBuilder sb = new StringBuilder();
        if (loc.getAddress() != null) sb.append(loc.getAddress());
        if (loc.getCity() != null) sb.append(sb.isEmpty() ? "" : ", ").append(loc.getCity());
        if (loc.getNation() != null) sb.append(" (").append(loc.getNation()).append(")");
        sb.append(" [lat=").append(loc.getLatitude()).append(", lon=").append(loc.getLongitude()).append("]");
        return sb.isEmpty() ? "?" : sb.toString();
    }
}