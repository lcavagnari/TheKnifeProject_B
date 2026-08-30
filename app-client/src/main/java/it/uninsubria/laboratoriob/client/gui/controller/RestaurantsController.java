package it.uninsubria.laboratoriob.client.gui.controller;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.client.gui.GuiContext;
import it.uninsubria.laboratoriob.client.gui.Navigation;
import it.uninsubria.laboratoriob.client.gui.session.Session;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controller della schermata di elenco ristoranti.
 * Supporta due modalità:browse pubblico (tutti i ristoranti) e gestione proprietario
 * (solo i ristoranti di proprietà). Include ricerca per parola chiave, paginazione
 * e creazione di nuovi ristoranti.
 */
public class RestaurantsController {

    private static final int PAGE_SIZE = 20;

    private Owner owner;
    private List<Restaurant> risultatiCompleti = List.of();
    private int visibleCount = PAGE_SIZE;

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
    private Button btnNuovo;
    @FXML
    private VBox emptyState;
    @FXML
    private Label emptyStateTitle;
    @FXML
    private Label emptyStateSub;

    @FXML
    private VBox root;

    /**
     * Inizializza la schermata in modalità browse pubblico, caricando tutti i ristoranti.
     */
    public void inizializza() {
        this.owner = null;
        restaurantListView.getStyleClass().add("restaurant-list");
        restaurantListView.setCellFactory(lv -> new RestaurantCell(this::apriDettagli, this::caricaAltri));

        titleLabel.setText("Trova Ristoranti");
        searchBar.setVisible(true);
        searchBar.setManaged(true);
        btnNuovo.setVisible(false);
        btnNuovo.setManaged(false);
        emptyStateTitle.setText("Nessun ristorante trovato");
        emptyStateSub.setText("Prova a cercare con parole chiave diverse");

        root.setOnMousePressed(e -> root.requestFocus());

        caricaEMostra(GuiContext.getDataStore().getRestaurantDAO().findAll());
    }

    /**
     * Inizializza la schermata in modalità gestione proprietario, mostrando solo
     * i ristoranti di proprietà dell'utente loggato e abilitando il pulsante "Nuovo".
     *
     * @param owner il proprietario loggato
     */
    public void inizializzaProprietario(Owner owner) {
        this.owner = owner;
        restaurantListView.getStyleClass().add("restaurant-list");
        restaurantListView.setCellFactory(lv -> new RestaurantCell(this::apriDettagli, this::caricaAltri));

        titleLabel.setText("I Miei Ristoranti");
        searchBar.setVisible(true);
        searchBar.setManaged(true);
        btnNuovo.setVisible(true);
        btnNuovo.setManaged(true);
        emptyStateTitle.setText("Non hai ancora nessun ristorante");
        emptyStateSub.setText("Usa \"+ Nuovo Ristorante\" per aggiungere il primo");

        root.setOnMousePressed(e -> root.requestFocus());

        caricaEMostra(ristorantiDiProprieta());
    }

    private List<Restaurant> ristorantiDiProprieta() {
        Map<UUID, Restaurant> uniti = new LinkedHashMap<>();
        if (owner == null)
            return List.of();

        for (Restaurant r : owner.getRestaurantsById().values())
            uniti.put(r.getId(), r);
        for (Restaurant r : GuiContext.getDataStore().getRestaurantDAO().findAll())
            if (r.getOwner() != null && r.getOwner().getId().equals(owner.getId()))
                uniti.putIfAbsent(r.getId(), r);
        return List.copyOf(uniti.values());
    }

    private List<Restaurant> filtraPerParolaChiave(List<Restaurant> ristoranti, String keyword) {
        if (keyword == null || keyword.isBlank())
            return ristoranti;

        String kw = keyword.trim().toLowerCase(Locale.ROOT);
        List<Restaurant> risultato = new ArrayList<>();
        for (Restaurant r : ristoranti) {
            boolean matchNome = r.getName() != null && r.getName().toLowerCase(Locale.ROOT).contains(kw);
            boolean matchCitta = r.getLocation() != null && r.getLocation().getCity() != null
                    && r.getLocation().getCity().toLowerCase(Locale.ROOT).contains(kw);
            boolean matchCucina = r.getCuisinesTypes() != null && r.getCuisinesTypes().stream()
                    .anyMatch(c -> c != null && c.toString().toLowerCase(Locale.ROOT).contains(kw));

            if (matchNome || matchCitta || matchCucina)
                risultato.add(r);
        }
        return risultato;
    }

    private void apriDettagli(Restaurant r) {
        try {
            Stage stage = (Stage) restaurantListView.getScene().getWindow();
            Owner ownerCorrente = owner;

            Navigation.pushBack(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/uninsubria/laboratoriob/client/gui/Restaurants.fxml"));
                    Parent root = loader.load();
                    RestaurantsController controller = loader.getController();
                    Scene scene = new Scene(root, 650, 500);
                    stage.setScene(scene);
                    if (ownerCorrente != null) {
                        controller.inizializzaProprietario(ownerCorrente);
                        stage.setTitle("I Miei Ristoranti");
                    } else {
                        controller.inizializza();
                        stage.setTitle("Trova Ristoranti");
                    }
                    stage.show();
                } catch (IOException ex) {
                    // TODO: MORE ROBUST LOGGING
                    ex.printStackTrace();
                }
            });

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/uninsubria/laboratoriob/client/gui/RestaurantDetails.fxml"));
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
        List<Restaurant> base = owner != null
                ? ristorantiDiProprieta()
                : GuiContext.getDataStore().getRestaurantDAO().findAll();
        caricaEMostra(filtraPerParolaChiave(base, keyword));
    }

    @FXML
    private void onNuovoClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/uninsubria/laboratoriob/client/gui/AddEditRestaurant.fxml"));
            Parent formRoot = loader.load();

            AddEditRestaurantController controller = loader.getController();
            controller.configuraNuovo(owner);

            Stage dialog = new Stage();
            dialog.initOwner(restaurantListView.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Nuovo Ristorante");
            dialog.setScene(new Scene(formRoot, 950, 700));
            dialog.showAndWait();

            caricaEMostra(ristorantiDiProprieta());
        } catch (IOException e) {
            // TODO: MORE ROBUST LOGGING
            e.printStackTrace();
        }
    }

    @FXML
    private void onIndietroClick() {
        Navigation.goBack();
    }

    private void caricaEMostra(List<Restaurant> risultati) {
        this.risultatiCompleti = risultati;
        this.visibleCount = PAGE_SIZE;
        aggiornaListaVisibile();
    }

    private void aggiornaListaVisibile() {
        List<Restaurant> visibili = risultatiCompleti.subList(0, Math.min(visibleCount, risultatiCompleti.size()));
        boolean hasMore = visibleCount < risultatiCompleti.size();

        // "Carica altri" e' l'ultima riga della lista stessa (item null = riga
        // segnaposto), cosi' scorre insieme ai ristoranti invece di stare fissa
        // sotto la ListView.
        List<Restaurant> itemsConFooter = new ArrayList<>(visibili);
        if (hasMore) itemsConFooter.add(null);
        restaurantListView.getItems().setAll(itemsConFooter);

        boolean empty = risultatiCompleti.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
        statsBar.setVisible(!empty);
        statsBar.setManaged(!empty);

        int totale = risultatiCompleti.size();
        String suffisso = "ristorant" + (totale == 1 ? "e trovato" : "i trovati");
        infoLabel.setText(hasMore ? (visibili.size() + " di " + totale + " " + suffisso) : (totale + " " + suffisso));
    }

    private void caricaAltri() {
        visibleCount += PAGE_SIZE;
        aggiornaListaVisibile();
    }

    private static class RestaurantCell extends ListCell<Restaurant> {

        private static final Image HEART_EMPTY = new Image(Objects.requireNonNull(
                RestaurantCell.class.getResourceAsStream("/it/uninsubria/laboratoriob/client/gui/images/empty-heart-svgrepo-com.png")));
        private static final Image HEART_FILLED = new Image(Objects.requireNonNull(
                RestaurantCell.class.getResourceAsStream("/it/uninsubria/laboratoriob/client/gui/images/heart-svgrepo-com.png")));
        private static final Image HEART_BROKEN = new Image(Objects.requireNonNull(
                RestaurantCell.class.getResourceAsStream("/it/uninsubria/laboratoriob/client/gui/images/heart-broken-svgrepo-com.png")));
        private static final Color HEART_HOVER_TINT = Color.web("#e91e63");
        private static final Color HEART_FAVORITED_TINT = Color.web("#d32f2f");

        private final HBox card = new HBox();
        private final VBox accent = new VBox();
        private final VBox content = new VBox();
        private final HBox topRow = new HBox();
        private final Label nameLabel = new Label();
        private final HBox awardBox = new HBox();
        private final Label ratingLabel = new Label();
        private final Label metaLabel = new Label();
        private final HBox badges = new HBox();
        private final Button heartButton = new Button();
        private final ImageView heartIcon = new ImageView();
        private final Consumer<Restaurant> onNavigate;
        private final Runnable onLoadMore;
        private final VBox loadMoreRow = new VBox();
        private Restaurant currentRestaurant;
        private Timeline runningAnimation;
        private boolean isFavorited;
        private Timeline heartAnimation;

        RestaurantCell(Consumer<Restaurant> onNavigate, Runnable onLoadMore) {
            super();
            this.onNavigate = onNavigate;
            this.onLoadMore = onLoadMore;

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            spacer.setPrefHeight(16);
            Button loadMoreButton = new Button("Carica altri");
            loadMoreButton.getStyleClass().add("button-secondary");
            loadMoreButton.setOnAction(e -> {
                if (this.onLoadMore != null) this.onLoadMore.run();
            });
            HBox loadMoreWrap = new HBox(loadMoreButton);
            loadMoreWrap.setAlignment(javafx.geometry.Pos.CENTER);
            loadMoreRow.getChildren().addAll(spacer, loadMoreWrap);
            loadMoreRow.setAlignment(javafx.geometry.Pos.CENTER);

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

            heartIcon.setFitWidth(28);
            heartIcon.setFitHeight(28);
            heartIcon.setPreserveRatio(true);
            heartIcon.setImage(HEART_EMPTY);
            heartButton.setGraphic(heartIcon);
            heartButton.getStyleClass().add("icon-button");
            heartButton.setOnMouseEntered(e -> repaintHeart());
            heartButton.setOnMouseExited(e -> repaintHeart());
            heartButton.setOnAction(e -> onHeartClick());

            card.getChildren().addAll(accent, content, heartButton);
            card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            card.setOnMousePressed(e -> {
                if (currentRestaurant == null) return;
                if (isWithinHeartButton(e.getTarget())) return;
                if (runningAnimation != null && runningAnimation.getStatus() == Animation.Status.RUNNING) return;
                bounceForward();
            });
        }

        private boolean isWithinHeartButton(javafx.event.EventTarget target) {
            for (Object walker = target; walker instanceof javafx.scene.Node node; walker = node.getParent())
                if (node == heartButton) return true;
            return false;
        }

        private void applyTint(ImageView iv, Color color) {
            if (color == null) {
                iv.setEffect(null);
                return;
            }
            ColorInput colorInput = new ColorInput(0, 0, iv.getFitWidth(), iv.getFitHeight(), color);
            iv.setEffect(new Blend(BlendMode.SRC_ATOP, null, colorInput));
        }

        private void repaintHeart() {
            boolean hovered = heartButton.isHover();
            if (isFavorited) {
                heartIcon.setImage(hovered ? HEART_BROKEN : HEART_FILLED);
                applyTint(heartIcon, HEART_FAVORITED_TINT);
            } else if (hovered) {
                heartIcon.setImage(HEART_FILLED);
                applyTint(heartIcon, HEART_HOVER_TINT);
            } else {
                heartIcon.setImage(HEART_EMPTY);
                applyTint(heartIcon, null);
            }
        }

        private void onHeartClick() {
            if (heartAnimation != null && heartAnimation.getStatus() == Animation.Status.RUNNING) return;
            if (!(Session.getCurrentUser() instanceof Customer customer) || currentRestaurant == null) return;

            if (!isFavorited) {
                GuiContext.getDataStore().getCustomerDAO().addFavourite(customer.getId(), currentRestaurant.getId());
                isFavorited = true;
                heartIcon.setImage(HEART_FILLED);
                applyTint(heartIcon, HEART_FAVORITED_TINT);
                playHeartAnimation(1.3);
            } else {
                GuiContext.getDataStore().getCustomerDAO().removeFavourite(customer.getId(), currentRestaurant.getId());
                isFavorited = false;
                heartIcon.setImage(HEART_EMPTY);
                applyTint(heartIcon, null);
                playHeartAnimation(0.75);
            }
        }

        private void playHeartAnimation(double peakScale) {
            heartButton.setDisable(true);
            heartAnimation = new Timeline(
                    new KeyFrame(Duration.millis(140),
                            new KeyValue(heartButton.scaleXProperty(), peakScale, Interpolator.EASE_OUT),
                            new KeyValue(heartButton.scaleYProperty(), peakScale, Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(260),
                            new KeyValue(heartButton.scaleXProperty(), 1.0, Interpolator.EASE_IN),
                            new KeyValue(heartButton.scaleYProperty(), 1.0, Interpolator.EASE_IN))
            );
            heartAnimation.setOnFinished(e -> {
                heartAnimation = null;
                heartButton.setDisable(false);
                repaintHeart();
            });
            heartAnimation.play();
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

            // Cells are recycled while scrolling; a heart animation left running from a
            // previous restaurant must not finish later and toggle the wrong one's favorite.
            if (heartAnimation != null) {
                heartAnimation.stop();
                heartAnimation = null;
                heartButton.setDisable(false);
                heartButton.setScaleX(1.0);
                heartButton.setScaleY(1.0);
            }

            if (empty) {
                setText(null);
                setGraphic(null);
                currentRestaurant = null;
                return;
            }
            if (r == null) {
                // Riga segnaposto: spazio vuoto + pulsante "Carica altri", ultima della lista.
                currentRestaurant = null;
                setText(null);
                setGraphic(loadMoreRow);
                return;
            }
            currentRestaurant = r;

            boolean isCustomer = Session.getCurrentUser() instanceof Customer;
            isFavorited = isCustomer
                    && ((Customer) Session.getCurrentUser()).getFavouriteRestourants().contains(r.getId());
            heartButton.setVisible(isCustomer);
            heartButton.setManaged(isCustomer);
            repaintHeart();

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
            for (String c : cuisineBadges)
                badges.getChildren().add(makeBadge(c, "badge-cuisine", null));
            if (r.isHasDelivery())
                badges.getChildren().add(makeBadge("Delivery", "badge-delivery", "/it/uninsubria/laboratoriob/client/gui/images/ic_geomarker.png"));
            if (r.isHasOnlineBooking())
                badges.getChildren().add(makeBadge("Booking", "badge-booking", "/it/uninsubria/laboratoriob/client/gui/images/ic_globe.png"));

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
                } catch (Exception ignored) {
                }
            }
            return l;
        }

        private Label makeStarLabel(int count) {
            Label l = new Label("★".repeat(count));
            l.setStyle("-fx-text-fill: #f9a825; -fx-font-size: 18px;");
            return l;
        }

        private ImageView makeGreenStarIcon() {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/it/uninsubria/laboratoriob/client/gui/images/ic_award.png")));
            ImageView iv = new ImageView(img);
            iv.setFitHeight(20);
            iv.setFitWidth(20);
            iv.setPreserveRatio(true);
            iv.setEffect(new javafx.scene.effect.DropShadow(3, 0, 1, Color.web("rgba(46,125,50,0.4)")));
            return iv;
        }
    }
}
