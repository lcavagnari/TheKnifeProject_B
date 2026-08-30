package it.uninsubria.laboratoriob.client.gui.controller;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.client.gui.GuiContext;
import it.uninsubria.laboratoriob.client.gui.Navigation;
import it.uninsubria.laboratoriob.client.gui.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class FavoritesController {

    private static final int PAGE_SIZE = 20;

    private Customer customer;
    private List<Restaurant> risultatiCompleti = List.of();
    private int visibleCount = PAGE_SIZE;

    @FXML
    private Label infoLabel;
    @FXML
    private Button btnIndietro;
    @FXML
    private ListView<Restaurant> restaurantListView;
    @FXML
    private VBox emptyState;
    @FXML
    private HBox loadMoreBar;
    @FXML
    private Button btnCaricaAltri;
    @FXML
    private VBox root;

    public void inizializza() {
        User utente = Session.getCurrentUser();
        this.customer = (utente instanceof Customer c) ? c : null;

        restaurantListView.getStyleClass().add("restaurant-list");
        restaurantListView.setCellFactory(lv -> new FavoriteRestaurantCell(this::apriDettagli, this::rimuoviPreferito));

        root.setOnMousePressed(e -> root.requestFocus());

        caricaEMostra();
    }

    private List<Restaurant> ristorantiPreferiti() {
        if (customer == null || customer.getFavouriteRestourants() == null || customer.getFavouriteRestourants().isEmpty())
            return List.of();
        return GuiContext.getDataStore().getRestaurantDAO().findAll().stream()
                .filter(r -> customer.getFavouriteRestourants().contains(r.getId()))
                .toList();
    }

    private void caricaEMostra() {
        this.risultatiCompleti = ristorantiPreferiti();
        this.visibleCount = PAGE_SIZE;
        aggiornaListaVisibile();
    }

    private void aggiornaListaVisibile() {
        List<Restaurant> visibili = risultatiCompleti.subList(0, Math.min(visibleCount, risultatiCompleti.size()));
        restaurantListView.getItems().setAll(visibili);

        boolean empty = risultatiCompleti.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
        infoLabel.setText(risultatiCompleti.size() + " ristorant" + (risultatiCompleti.size() == 1 ? "e preferito" : "i preferiti"));

        boolean hasMore = visibleCount < risultatiCompleti.size();
        loadMoreBar.setVisible(hasMore);
        loadMoreBar.setManaged(hasMore);
    }

    @FXML
    private void onCaricaAltriClick() {
        visibleCount += PAGE_SIZE;
        aggiornaListaVisibile();
    }

    private void apriDettagli(Restaurant r) {
        try {
            Stage stage = (Stage) restaurantListView.getScene().getWindow();

            Navigation.pushBack(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/uninsubria/laboratoriob/client/gui/Favorites.fxml"));
                    Parent root = loader.load();
                    FavoritesController controller = loader.getController();
                    controller.inizializza();
                    Scene scene = new Scene(root, 650, 500);
                    stage.setScene(scene);
                    stage.setTitle("Preferiti");
                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/uninsubria/laboratoriob/client/gui/RestaurantDetails.fxml"));
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

    private void rimuoviPreferito(Restaurant r) {
        if (customer != null)
            GuiContext.getDataStore().getCustomerDAO().removeFavourite(customer.getId(), r.getId());
        caricaEMostra();
    }

    @FXML
    private void onIndietroClick() {
        Navigation.goBack();
    }

    private static class FavoriteRestaurantCell extends ListCell<Restaurant> {

        private final HBox card = new HBox();
        private final VBox accent = new VBox();
        private final VBox content = new VBox();
        private final HBox topRow = new HBox();
        private final Label nameLabel = new Label();
        private final Label ratingLabel = new Label();
        private final Label metaLabel = new Label();
        private final Button removeButton = new Button();
        private final Consumer<Restaurant> onNavigate;
        private final Consumer<Restaurant> onRemove;

        FavoriteRestaurantCell(Consumer<Restaurant> onNavigate, Consumer<Restaurant> onRemove) {
            super();
            this.onNavigate = onNavigate;
            this.onRemove = onRemove;

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

            removeButton.setGraphic(new ImageView(new Image(
                    getClass().getResourceAsStream("/it/uninsubria/laboratoriob/client/gui/images/heart-broken-svgrepo-com.png"), 18, 18, true, true)));
            removeButton.getStyleClass().add("button-link");
            removeButton.setStyle("-fx-padding: 0 12 0 6;");
            // Stop the click from also bubbling up to the card's own navigate-to-details handler.
            removeButton.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, javafx.event.Event::consume);
            removeButton.setOnAction(e -> {
                Restaurant r = getItem();
                if (r != null) onRemove.accept(r);
            });

            card.getChildren().addAll(accent, content, removeButton);
            card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

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
