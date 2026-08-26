package com.example.demo3;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

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
    private Label awardLabel;
    @FXML
    private Label greenStarLabel;
    @FXML
    private Label reviewsCountLabel;
    @FXML
    private FlowPane cuisinesFlow;
    @FXML
    private FlowPane servicesFlow;
    @FXML
    private FlowPane deliveryBookingFlow;
    @FXML
    private Button btnIndietro;
    @FXML
    private VBox root;

    @FXML
    public void initialize() {
        String css = Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm();
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && !newScene.getRoot().getStylesheets().contains(css)) {
                newScene.getRoot().getStylesheets().add(css);
            }
        });
    }

    public void carica(Restaurant r) {
        nameLabel.setText(valoreO(r.getName(), "Senza nome"));
        descriptionLabel.setText(valoreO(r.getDescription(), "No description available."));

        // Website
        String website = r.getWebsiteUrl();
        if (website != null && !website.isBlank()) {
            websiteLabel.setText(website);
        } else {
            websiteLabel.setText("N/A");
        }

        // Phone
        phoneLabel.setText(valoreO(r.getPhone(), "N/A"));

        // Location
        locationLabel.setText(formattaLocation(r.getLocation()));

        // Price range
        priceRangeLabel.setText(r.getPriceRange() != null ? r.getPriceRange().getSymbol() + " - " + r.getPriceRange().name() : "N/A");

        // Award ribbon
        if (r.getAward() != null && r.getAward() != it.uninsubria.laboratoriob.api.enums.Award.NONE) {
            awardLabel.setVisible(true);
            awardLabel.setManaged(true);
            awardLabel.setText("★ " + r.getAward().toString());
        }

        // Green star ribbon
        if (r.isGreenStar()) {
            greenStarLabel.setVisible(true);
            greenStarLabel.setManaged(true);
            greenStarLabel.setText("Green Star");
        }

        // Delivery + Online Booking badges
        deliveryBookingFlow.getChildren().clear();
        if (r.isHasDelivery()) {
            Label badge = new Label("Delivery");
            badge.getStyleClass().addAll("badge", "badge-delivery");
            deliveryBookingFlow.getChildren().add(badge);
        }
        if (r.isHasOnlineBooking()) {
            Label badge = new Label("Online Booking");
            badge.getStyleClass().addAll("badge", "badge-booking");
            deliveryBookingFlow.getChildren().add(badge);
        }

        // Cuisine badges
        cuisinesFlow.getChildren().clear();
        if (r.getCuisinesTypes() != null) {
            for (CuisineType c : r.getCuisinesTypes()) {
                Label badge = new Label(c.toString());
                badge.getStyleClass().addAll("badge", "badge-cuisine");
                cuisinesFlow.getChildren().add(badge);
            }
        }

        // Services badges
        servicesFlow.getChildren().clear();
        if (r.getServices() != null) {
            for (String s : r.getServices()) {
                Label badge = new Label(s);
                badge.getStyleClass().addAll("badge", "badge-booking");
                servicesFlow.getChildren().add(badge);
            }
        }

        // Reviews count
        int numRecensioni = r.getReviews() != null ? r.getReviews().size() : 0;
        reviewsCountLabel.setText(numRecensioni + (numRecensioni == 0
                ? " - No reviews yet" : " Reviews"));
    }

    @FXML
    private void onIndietroClick() {
        Navigation.goBack();
    }

    private String valoreO(String valore, String fallback) {
        return (valore == null || valore.isBlank()) ? fallback : valore;
    }

    private String formattaLocation(Location loc) {
        if (loc == null) return "N/A";
        StringBuilder sb = new StringBuilder();
        if (loc.getAddress() != null) sb.append(loc.getAddress());
        if (loc.getCity() != null) sb.append(sb.isEmpty() ? "" : ", ").append(loc.getCity());
        if (loc.getNation() != null) sb.append(" (").append(loc.getNation().name().replace("_", " ")).append(")");
        return sb.isEmpty() ? "N/A" : sb.toString();
    }
}
