package com.example.demo3;

import com.example.demo3.data.RestaurantRepository;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.control.ContentDisplay;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RestaurantsController {

    private final RestaurantRepository restaurantRepository = new RestaurantRepository();

    @FXML
    private Label titleLabel;
    @FXML
    private Label infoLabel;
    @FXML
    private HBox searchBar;
    @FXML
    private HBox statsBar;
    @FXML
    private TextField searchField;
    @FXML
    private ListView<Restaurant> restaurantListView;
    @FXML
    private Button btnIndietro;
    @FXML
    private VBox emptyState;

    @FXML
    private VBox root;

    public void inizializza() {
        restaurantListView.getStyleClass().add("restaurant-list");
        restaurantListView.setCellFactory(lv -> new RestaurantCell(this::apriDettagli));

        searchBar.setVisible(true);
        searchBar.setManaged(true);

        root.setOnMousePressed(e -> root.requestFocus());

        caricaEMostra(restaurantRepository.caricaTutti());
    }

    private void apriDettagli(Restaurant r) {
        try {
            Stage stage = (Stage) restaurantListView.getScene().getWindow();

            Navigation.pushBack(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Restaurants.fxml"));
                    Parent root = loader.load();
                    RestaurantsController controller = loader.getController();
                    controller.inizializza();
                    Scene scene = new Scene(root, 650, 500);
                    stage.setScene(scene);
                    stage.setTitle("Trova Ristoranti");
                    stage.show();
                } catch (IOException ex) {
                    // TODO: MORE ROBUST LOGGING
                    ex.printStackTrace();
                }
            });

            FXMLLoader loader = new FXMLLoader(getClass().getResource("RestaurantDetails.fxml"));
            Parent root = loader.load();

            RestaurantDetailsController controller = loader.getController();
            controller.carica(r);

            Scene scene = new Scene(root, 650, 550);
            stage.setScene(scene);
            stage.setTitle(r.getName() != null ? r.getName() : "Dettagli ristorante");
            stage.show();
        } catch (IOException e) {
            // TODO: MORE ROBUST LOGGING
            e.printStackTrace();
        }
    }

    @FXML
    private void onCercaClick() {
        String keyword = searchField.getText();
        caricaEMostra(restaurantRepository.cerca(keyword));
    }

    @FXML
    private void onIndietroClick() {
        Navigation.goBack();
    }

    private void caricaEMostra(List<Restaurant> risultati) {
        restaurantListView.getItems().setAll(risultati);
        boolean empty = risultati.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
        statsBar.setVisible(!empty);
        statsBar.setManaged(!empty);
        infoLabel.setText(risultati.size() + " ristorant" + (risultati.size() == 1 ? "e trovato" : "i trovati"));
    }

    private static class RestaurantCell extends ListCell<Restaurant> {

        private final HBox card = new HBox();
        private final VBox accent = new VBox();
        private final VBox content = new VBox();
        private final HBox topRow = new HBox();
        private final Label nameLabel = new Label();
        private final HBox awardBox = new HBox();
        private final Label ratingLabel = new Label();
        private final Label metaLabel = new Label();
        private final HBox badges = new HBox();
        private final Consumer<Restaurant> onNavigate;
        private Restaurant currentRestaurant;
        private Timeline runningAnimation;

        RestaurantCell(Consumer<Restaurant> onNavigate) {
            super();
            this.onNavigate = onNavigate;

            card.getStyleClass().add("restaurant-card");
            card.setSpacing(0);
            accent.getStyleClass().add("price-accent");

            nameLabel.getStyleClass().add("restaurant-name");
            ratingLabel.getStyleClass().add("restaurant-rating");
            metaLabel.getStyleClass().add("restaurant-meta");
            badges.getStyleClass().add("restaurant-badges");

            HBox.setHgrow(content, Priority.ALWAYS);
            content.setStyle("-fx-padding: 6 10 6 10;");
            awardBox.setSpacing(3);
            awardBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            topRow.setSpacing(4);
            topRow.getChildren().addAll(nameLabel, awardBox, ratingLabel);
            content.getChildren().addAll(topRow, metaLabel, badges);
            card.getChildren().addAll(accent, content);

            card.setOnMousePressed(e -> {
                if (currentRestaurant == null) return;
                if (runningAnimation != null && runningAnimation.getStatus() == Animation.Status.RUNNING) return;
                bounceForward();
            });
        }

        private void bounceForward() {
            Restaurant r = currentRestaurant;
            runningAnimation = new Timeline(
                    new KeyFrame(Duration.millis(180),
                            new KeyValue(card.scaleXProperty(), 0.92, Interpolator.EASE_OUT),
                            new KeyValue(card.scaleYProperty(), 0.95, Interpolator.EASE_OUT),
                            new KeyValue(card.translateYProperty(), 4, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(card.scaleXProperty(), 1.0, Interpolator.EASE_IN),
                            new KeyValue(card.scaleYProperty(), 1.0, Interpolator.EASE_IN),
                            new KeyValue(card.translateYProperty(), 0, Interpolator.EASE_IN))
            );
            runningAnimation.setOnFinished(e -> {
                runningAnimation = null;
                if (r != null) onNavigate.accept(r);
            });
            runningAnimation.play();
        }

        @Override
        protected void updateItem(Restaurant r, boolean empty) {
            super.updateItem(r, empty);
            if (empty || r == null) {
                setText(null);
                setGraphic(null);
                currentRestaurant = null;
                return;
            }
            currentRestaurant = r;

            nameLabel.setText(r.getName() != null ? r.getName() : "Ristorante");

            // Awards: Michelin stars + green star icon next to name
            awardBox.getChildren().clear();
            if (r.getAward() != null) awardBox.getChildren().add(makeStarLabel(r.getAward().getValue()));
            if (r.isGreenStar()) awardBox.getChildren().add(makeGreenStarIcon());


            // Rating
            boolean isPresent = r.getReviews() != null && !r.getReviews().isEmpty();
            if (isPresent) {
                double avg = r.getReviews().values().stream()
                        .mapToInt(Review::getValue)
                        .average()
                        .orElse(0.0);

                ratingLabel.setText(String.format("  ★ %.1f", avg));
            }

            ratingLabel.setVisible(isPresent);
            ratingLabel.setManaged(isPresent);




            // Meta: city + price range
            String city = (r.getLocation() != null && r.getLocation().getCity() != null)
                    ? r.getLocation().getCity() : "";
            String price = r.getPriceRange() != null ? r.getPriceRange().getSymbol() : "";
            String meta = city;
            if (!price.isEmpty()) meta += meta.isEmpty() ? price : "  ·  " + price;
            metaLabel.setText(meta);

            // Price accent color
            accent.getStyleClass().removeIf(s -> s.startsWith("price-accent-"));
            String accentClass = switch (r.getPriceRange()) {
                case ECONOMY -> "price-accent-budget";
                case MODERATE -> "price-accent-moderate";
                case EXPENSIVE -> "price-accent-upscale";
                case LUXURY -> "price-accent-premium";
                default -> "price-accent-moderate";
            };
            accent.getStyleClass().add(accentClass);

            // Badges
            badges.getChildren().clear();
            Set<String> cuisineBadges = (r.getCuisinesTypes() != null)
                    ? r.getCuisinesTypes().stream().limit(3).map(c -> c.toString()).collect(Collectors.toSet())
                    : Set.of();
            for (String c : cuisineBadges) {
                badges.getChildren().add(makeBadge(c, "badge-cuisine", null));
            }
            if (r.isHasDelivery()) badges.getChildren().add(makeBadge("Delivery", "badge-delivery", "images/ic_geomarker.png"));
            if (r.isHasOnlineBooking()) badges.getChildren().add(makeBadge("Booking", "badge-booking", "images/ic_globe.png"));

            setGraphic(card);
        }

        private Label makeBadge(String text, String styleClass, String iconPath) {
            Label l = new Label(text);
            l.getStyleClass().addAll("badge", styleClass);
            if (iconPath != null && !iconPath.isBlank()) {
                try {
                    Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath)));
                    ImageView iv = new ImageView(img);
                    iv.setFitHeight(14);
                    iv.setFitWidth(14);
                    iv.setPreserveRatio(true);
                    // Black tint for icon (like the text)
                    ColorAdjust blackTint = new ColorAdjust();
                    blackTint.setSaturation(-1.0);
                    blackTint.setBrightness(-1.0);
                    iv.setEffect(blackTint);
                    l.setGraphic(iv);
                    l.setContentDisplay(ContentDisplay.LEFT);
                    l.setGraphicTextGap(4);
                } catch (Exception ignored) {}
            }
            return l;
        }

        private Label makeStarLabel(int count) {
            Label l = new Label("★".repeat(count));
            l.setStyle("-fx-text-fill: #f9a825; -fx-font-size: 18px;");
            return l;
        }

        private ImageView makeGreenStarIcon() {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/ic_award.png")));
            ImageView iv = new ImageView(img);
            iv.setFitHeight(20);
            iv.setFitWidth(20);
            iv.setPreserveRatio(true);
            iv.setEffect(new javafx.scene.effect.DropShadow(3, 0, 1, Color.web("rgba(46,125,50,0.4)")));
            return iv;
        }
    }
}
