package it.uninsubria.laboratoriob.client.gui;

import it.uninsubria.laboratoriob.client.gui.data.Session;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.objects.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class MyRestaurantsController {

    private Owner owner;

    @FXML
    private Label infoLabel;
    @FXML
    private Button btnIndietro;
    @FXML
    private Button btnNuovo;
    @FXML
    private ListView<Restaurant> restaurantListView;
    @FXML
    private VBox emptyState;
    @FXML
    private VBox root;

    public void inizializza() {
        User utente = Session.getCurrentUser();
        this.owner = (utente instanceof Owner o) ? o : null;

        restaurantListView.getStyleClass().add("restaurant-list");
        restaurantListView.setCellFactory(lv -> new OwnedRestaurantCell(this::apriDettagli));

        root.setOnMousePressed(e -> root.requestFocus());

        caricaEMostra();
    }

    private List<Restaurant> ristorantiDiProprieta() {
        Map<UUID, Restaurant> uniti = new LinkedHashMap<>();
        if (owner == null) {
            return List.of();
        }

        for (Restaurant r : owner.getRestaurantsById().values()) {
            uniti.put(r.getId(), r);
        }
        for (Restaurant r : GuiContext.getDataStore().getRestaurantDAO().findAll()) {
            if (r.getOwner() != null && r.getOwner().getId().equals(owner.getId())) {
                uniti.putIfAbsent(r.getId(), r);
            }
        }
        return List.copyOf(uniti.values());
    }

    private void caricaEMostra() {
        List<Restaurant> ristoranti = ristorantiDiProprieta();
        restaurantListView.getItems().setAll(ristoranti);

        boolean empty = ristoranti.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
        restaurantListView.setVisible(!empty);
        restaurantListView.setManaged(!empty);
        infoLabel.setText(ristoranti.size() + " ristorant" + (ristoranti.size() == 1 ? "e" : "i"));
    }

    private void apriDettagli(Restaurant r) {
        try {
            Stage stage = (Stage) restaurantListView.getScene().getWindow();

            Navigation.pushBack(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("MyRestaurants.fxml"));
                    Parent root = loader.load();
                    MyRestaurantsController controller = loader.getController();
                    controller.inizializza();
                    Scene scene = new Scene(root, 650, 500);
                    stage.setScene(scene);
                    stage.setTitle("I Miei Ristoranti");
                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            FXMLLoader loader = new FXMLLoader(getClass().getResource("RestaurantDetails.fxml"));
            Parent detailsRoot = loader.load();

            RestaurantDetailsController controller = loader.getController();
            controller.carica(r);

            Scene scene = new Scene(detailsRoot, 650, 550);
            stage.setScene(scene);
            stage.setTitle(r.getName() != null ? r.getName() : "Dettagli ristorante");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNuovoClick() {
        if (owner == null) {
            return;
        }
        try {
            Stage stage = (Stage) btnNuovo.getScene().getWindow();

            Navigation.pushBack(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("MyRestaurants.fxml"));
                    Parent root = loader.load();
                    MyRestaurantsController controller = loader.getController();
                    controller.inizializza();
                    Scene scene = new Scene(root, 650, 500);
                    stage.setScene(scene);
                    stage.setTitle("I Miei Ristoranti");
                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddEditRestaurant.fxml"));
            Parent formRoot = loader.load();

            AddEditRestaurantController controller = loader.getController();
            controller.configuraNuovo(owner);

            Scene scene = new Scene(formRoot, 650, 600);
            stage.setScene(scene);
            stage.setTitle("Nuovo Ristorante");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onIndietroClick() {
        Navigation.goBack();
    }

    private static class OwnedRestaurantCell extends ListCell<Restaurant> {

        private final HBox card = new HBox();
        private final VBox accent = new VBox();
        private final VBox content = new VBox();
        private final HBox topRow = new HBox();
        private final Label nameLabel = new Label();
        private final Label ratingLabel = new Label();
        private final Label metaLabel = new Label();
        private final Consumer<Restaurant> onNavigate;

        OwnedRestaurantCell(Consumer<Restaurant> onNavigate) {
            super();
            this.onNavigate = onNavigate;

            card.getStyleClass().add("restaurant-card");
            accent.getStyleClass().add("price-accent");
            nameLabel.getStyleClass().add("restaurant-name");
            ratingLabel.getStyleClass().add("restaurant-rating");
            metaLabel.getStyleClass().add("restaurant-meta");

            HBox.setHgrow(content, Priority.ALWAYS);
            content.setStyle("-fx-padding: 8 10 8 10;");
            topRow.setSpacing(4);
            topRow.getChildren().addAll(nameLabel, ratingLabel);
            content.getChildren().addAll(topRow, metaLabel);
            card.getChildren().addAll(accent, content);

            card.setOnMouseClicked(e -> {
                Restaurant r = getItem();
                if (r != null) onNavigate.accept(r);
            });
        }

        @Override
        protected void updateItem(Restaurant r, boolean empty) {
            super.updateItem(r, empty);
            if (empty || r == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            nameLabel.setText(r.getName() != null ? r.getName() : "Ristorante");

            boolean hasReviews = r.getReviews() != null && !r.getReviews().isEmpty();
            if (hasReviews) {
                double avg = r.getReviews().values().stream()
                        .mapToInt(Review::getValue)
                        .average()
                        .orElse(0.0);
                ratingLabel.setText(String.format("  ★ %.1f", avg));
            }
            ratingLabel.setVisible(hasReviews);
            ratingLabel.setManaged(hasReviews);

            String city = (r.getLocation() != null && r.getLocation().getCity() != null)
                    ? r.getLocation().getCity() : "";
            String price = r.getPriceRange() != null ? r.getPriceRange().getSymbol() : "";
            String meta = city;
            if (!price.isEmpty()) meta += meta.isEmpty() ? price : "  ·  " + price;
            metaLabel.setText(meta);

            accent.getStyleClass().removeIf(s -> s.startsWith("price-accent-"));
            String accentClass = switch (r.getPriceRange()) {
                case ECONOMY -> "price-accent-budget";
                case MODERATE -> "price-accent-moderate";
                case EXPENSIVE -> "price-accent-upscale";
                case LUXURY -> "price-accent-premium";
                default -> "price-accent-moderate";
            };
            accent.getStyleClass().add(accentClass);

            setGraphic(card);
        }
    }
}
