package it.uninsubria.laboratoriob.client.gui.controller;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.UserRole;
import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.client.gui.GuiContext;
import it.uninsubria.laboratoriob.client.gui.Navigation;
import it.uninsubria.laboratoriob.client.gui.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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
    private javafx.scene.layout.HBox greenStarRibbon;
    @FXML
    private Label reviewsCountLabel;
    @FXML
    private VBox bestWorstReviewSeparator;
    @FXML
    private GridPane bestWorstReviewRow;
    @FXML
    private Label bestReviewLabel;
    @FXML
    private Label worstReviewLabel;
    @FXML
    private FlowPane cuisinesFlow;
    @FXML
    private FlowPane servicesFlow;
    @FXML
    private FlowPane deliveryBookingFlow;
    @FXML
    private FlowPane starsFlow;
    @FXML
    private Button btnIndietro;
    @FXML
    private Button btnModifica;
    @FXML
    private Button btnPreferito;
    @FXML
    private VBox root;
    @FXML
    private ScrollPane reviewsScrollPane;
    @FXML
    private VBox reviewsList;
    @FXML
    private VBox writeReviewSection;
    @FXML
    private ReviewInputBoxController writeReviewBoxController;

    private Restaurant restaurant;

    @FXML
    public void initialize() {
        String css = Objects.requireNonNull(getClass().getResource("/it/uninsubria/laboratoriob/client/gui/style.css")).toExternalForm();
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && !newScene.getRoot().getStylesheets().contains(css))
                newScene.getRoot().getStylesheets().add(css);
        });

        writeReviewBoxController.setReviewMode();
        writeReviewBoxController.setOnSubmit(this::onWriteReviewSubmit);
        writeReviewBoxController.setOnCancel(() -> carica(restaurant));
    }

    private void onWriteReviewSubmit(ReviewInputBoxController.Result result) {
        if (!(Session.getCurrentUser() instanceof Customer customer) || restaurant == null) return;
        if (result.rating() < 1 || result.rating() > 5 || result.text() == null || result.text().isBlank()) return;

        Review existing = restaurant.getReviews().get(customer.getId());
        if (existing != null) {
            existing.setValue(result.rating());
            existing.setText(result.text());
            GuiContext.getDataStore().getReviewDAO().update(existing);
        } else {
            Review review = new Review(restaurant, customer, result.rating(), result.text());
            restaurant.addReview(review);
            GuiContext.getDataStore().getReviewDAO().save(review);
        }

        carica(restaurant);
    }

    public void carica(Restaurant r) {
        this.restaurant = r;

        User currentUser = Session.getCurrentUser();
        boolean isOwner = currentUser != null
                && currentUser.getRole() == UserRole.OWNER
                && r.getOwner() != null
                && currentUser.getId().toString().equals(r.getOwner().getId().toString());
        btnModifica.setVisible(isOwner);
        btnModifica.setManaged(isOwner);

        boolean isCustomer = currentUser instanceof Customer;
        btnPreferito.setVisible(isCustomer);
        btnPreferito.setManaged(isCustomer);
        if (isCustomer)
            aggiornaTestoPreferito((Customer) currentUser, r);

        writeReviewSection.setVisible(isCustomer);
        writeReviewSection.setManaged(isCustomer);
        if (isCustomer) {
            Review ownReview = r.getReviews().get(currentUser.getId());
            if (ownReview != null) {
                writeReviewBoxController.setRating(ownReview.getValue());
                writeReviewBoxController.setText(ownReview.getText());
            } else {
                writeReviewBoxController.clear();
            }
        }

        nameLabel.setText(valoreO(r.getName(), "Senza nome"));
        descriptionLabel.setText(valoreO(r.getDescription(), "No description available."));

        // Website
        String website = r.getWebsiteUrl();
        if (website != null && !website.isBlank())
            websiteLabel.setText(website);
        else
            websiteLabel.setText("N/A");

        // Phone
        phoneLabel.setText(valoreO(r.getPhone(), "N/A"));

        // Location
        locationLabel.setText(formattaLocation(r.getLocation()));

        // Price range
        priceRangeLabel.setText(r.getPriceRange() != null ? r.getPriceRange().getSymbol() + " - " + r.getPriceRange().name() : "N/A");

        // Michelin stars
        if (r.getAward() != null && r.getAward() != it.uninsubria.laboratoriob.api.enums.Award.NONE) {
            awardLabel.setVisible(true);
            awardLabel.setManaged(true);
            awardLabel.setText("★".repeat(r.getAward().getValue()));
        }

        // Green star ribbon
        if (r.isGreenStar()) {
            greenStarRibbon.setVisible(true);
            greenStarRibbon.setManaged(true);
        }

        // Delivery + Online Booking badges
        deliveryBookingFlow.getChildren().clear();
        if (r.isHasDelivery())
            deliveryBookingFlow.getChildren().add(makeIconBadge("Delivery", "badge-delivery", "/it/uninsubria/laboratoriob/client/gui/images/ic_geomarker.png"));
        if (r.isHasOnlineBooking())
            deliveryBookingFlow.getChildren().add(makeIconBadge("Online Booking", "badge-booking", "/it/uninsubria/laboratoriob/client/gui/images/ic_globe.png"));

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

        // Reviews count + stars
        int numRecensioni = r.getReviews() != null ? r.getReviews().size() : 0;
        double avg = 0;
        if (numRecensioni > 0) {
            avg = r.getReviews().values().stream()
                    .mapToInt(Review::getValue)
                    .average()
                    .orElse(0.0);
        }
        int filledStars = (int) Math.round(avg);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++)
            sb.append(i <= filledStars ? "★" : "☆");
        Label starLabel = new Label(sb.toString());
        starLabel.getStyleClass().add("reviews-stars");
        starLabel.setPadding(Insets.EMPTY);
        starsFlow.getChildren().setAll(starLabel);

        reviewsCountLabel.setText(numRecensioni + (numRecensioni == 1 ? " review" : " reviews"));

        // Best + worst review values
        boolean showBestWorst = numRecensioni > 0;
        bestWorstReviewSeparator.setVisible(showBestWorst);
        bestWorstReviewSeparator.setManaged(showBestWorst);
        bestWorstReviewRow.setVisible(showBestWorst);
        bestWorstReviewRow.setManaged(showBestWorst);
        if (showBestWorst) {
            Review best = r.getReviews().values().stream().max(Comparator.comparingInt(Review::getValue)).orElseThrow();
            Review worst = r.getReviews().values().stream().min(Comparator.comparingInt(Review::getValue)).orElseThrow();
            bestReviewLabel.setText(formattaRecensione(best));
            worstReviewLabel.setText(formattaRecensione(worst));
        }

        // ------------------ LISTA COMPLETA RECENSIONI ------------------

        if (reviewsList != null) reviewsList.getChildren().clear();

        if (r.getReviews() != null && !r.getReviews().isEmpty()) {
            final String currentUserId = currentUser != null ? currentUser.getId().toString() : null;

            // Ordina per data decrescente e crea le card
            r.getReviews().values().stream()
                    .sorted(Comparator.comparing(Review::getTimestamp).reversed())
                    .forEach(review -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/uninsubria/laboratoriob/client/gui/ReviewCard.fxml"));
                            VBox card = loader.load();
                            ReviewCardController controller = loader.getController();
                            controller.setContext(review, currentUserId, isOwner);
                            reviewsList.getChildren().add(card);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } else {
            Label noReviews = new Label("No reviews yet.");
            noReviews.setStyle("-fx-text-fill: #757575; -fx-font-size: 15px; -fx-padding: 10;");
            reviewsList.getChildren().add(noReviews);
        }
    }

    private static final DateTimeFormatter REVIEW_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String formattaRecensione(Review review) {
        return review.getValue() + " . " + review.getTimestamp().format(REVIEW_TIMESTAMP_FORMAT);
    }

    @FXML
    private void onIndietroClick() {
        Navigation.goBack();
    }

    @FXML
    private void onModificaClick() {
        if (!(Session.getCurrentUser() instanceof Owner owner) || restaurant == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/uninsubria/laboratoriob/client/gui/AddEditRestaurant.fxml"));
            Parent formRoot = loader.load();
            AddEditRestaurantController controller = loader.getController();
            controller.configuraModifica(owner, restaurant);

            Stage dialog = new Stage();
            dialog.initOwner(btnModifica.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Modifica Ristorante");
            dialog.setScene(new Scene(formRoot, 950, 700));
            dialog.showAndWait();

            carica(restaurant);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onPreferitoClick() {
        if (!(Session.getCurrentUser() instanceof Customer customer) || restaurant == null)
            return;
        var customerDAO = GuiContext.getDataStore().getCustomerDAO();
        if (customer.getFavouriteRestourants().contains(restaurant.getId()))
            customerDAO.removeFavourite(customer.getId(), restaurant.getId());
        else
            customerDAO.addFavourite(customer.getId(), restaurant.getId());
        aggiornaTestoPreferito(customer, restaurant);
    }

    private void aggiornaTestoPreferito(Customer customer, Restaurant r) {
        boolean isPreferito = customer.getFavouriteRestourants().contains(r.getId());
        btnPreferito.setText(isPreferito ? "Nei Preferiti" : "Aggiungi ai Preferiti");
        btnPreferito.getStyleClass().removeAll("button-secondary");
        if (!isPreferito)
            btnPreferito.getStyleClass().add("button-secondary");
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

    private Label makeIconBadge(String text, String styleClass, String iconPath) {
        Label l = new Label(text);
        l.getStyleClass().addAll("badge", styleClass);
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
        } catch (Exception ignored) {
        }
        return l;
    }
}