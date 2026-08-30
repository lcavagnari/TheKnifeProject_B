package it.uninsubria.laboratoriob.client.gui.controller;

import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.client.gui.GuiContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.*;

public class AddEditRestaurantController {

    private Owner owner;
    private Restaurant restaurantInModifica;
    private final Set<String> servizi = new LinkedHashSet<>();

    @FXML
    private Label titleLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private Button btnIndietro;

    @FXML
    private Label nomeError;
    @FXML
    private TextField nomeField;
    @FXML
    private TextArea descrizioneField;
    @FXML
    private TextField sitoWebField;
    @FXML
    private TextField telefonoField;

    @FXML
    private TextField indirizzoField;
    @FXML
    private TextField cittaField;
    @FXML
    private Label nazioneError;
    @FXML
    private TextField nazioneField;

    @FXML
    private Label prezzoError;
    @FXML
    private ComboBox<PriceRange> prezzoCombo;
    @FXML
    private ComboBox<Award> premioCombo;
    @FXML
    private CheckBox consegnaCheck;
    @FXML
    private CheckBox prenotazioneCheck;
    @FXML
    private CheckBox stellaVerdeCheck;

    @FXML
    private TextField cucinaFiltroField;
    @FXML
    private ListView<CuisineType> cucinaListView;

    @FXML
    private TextField servizioField;
    @FXML
    private FlowPane serviziFlow;

    @FXML
    private Button btnSalva;
    @FXML
    private Button btnAnnulla;

    private FilteredList<CuisineType> cucineFiltrate;

    @FXML
    public void initialize() {
        prezzoCombo.setItems(FXCollections.observableArrayList(PriceRange.values()));
        prezzoCombo.setCellFactory(lv -> priceRangeCell());
        prezzoCombo.setButtonCell(priceRangeCell());

        premioCombo.setItems(FXCollections.observableArrayList(Award.values()));
        premioCombo.setCellFactory(lv -> awardCell());
        premioCombo.setButtonCell(awardCell());
        premioCombo.getSelectionModel().select(Award.NONE);

        ObservableList<CuisineType> tutteLeCucine = FXCollections.observableArrayList(CuisineType.values());
        cucineFiltrate = new FilteredList<>(tutteLeCucine, c -> true);
        cucinaListView.setItems(cucineFiltrate);
        cucinaListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        cucinaListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CuisineType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formattaCucina(item));
            }
        });

        cucinaFiltroField.textProperty().addListener((obs, old, val) -> {
            String kw = val == null ? "" : val.trim().toLowerCase(Locale.ROOT);
            cucineFiltrate.setPredicate(c -> kw.isEmpty() || c.toString().contains(kw));
        });

        nomeField.textProperty().addListener((obs, old, n) -> clearFieldError(nomeError, nomeField));
        nazioneField.textProperty().addListener((obs, old, n) -> clearFieldError(nazioneError, nazioneField));
        prezzoCombo.valueProperty().addListener((obs, old, n) -> clearFieldError(prezzoError, prezzoCombo));

        renderServizi();
    }

    public void configuraNuovo(Owner owner) {
        this.owner = owner;
        this.restaurantInModifica = null;
        titleLabel.setText("Nuovo Ristorante");
        btnSalva.setText("Crea Ristorante");
        prezzoCombo.getSelectionModel().select(PriceRange.MODERATE);
    }

    public void configuraModifica(Owner owner, Restaurant restaurant) {
        this.owner = owner;
        this.restaurantInModifica = restaurant;
        titleLabel.setText("Modifica Ristorante");
        btnSalva.setText("Salva Modifiche");

        nomeField.setText(restaurant.getName());
        descrizioneField.setText(restaurant.getDescription());
        sitoWebField.setText(restaurant.getWebsiteUrl());
        telefonoField.setText(restaurant.getPhone());

        if (restaurant.getLocation() != null) {
            indirizzoField.setText(restaurant.getLocation().getAddress());
            cittaField.setText(restaurant.getLocation().getCity());
            nazioneField.setText(restaurant.getLocation().getNation() != null
                    ? restaurant.getLocation().getNation().name() : "");
        }

        prezzoCombo.getSelectionModel().select(restaurant.getPriceRange());
        premioCombo.getSelectionModel().select(restaurant.getAward() != null ? restaurant.getAward() : Award.NONE);
        consegnaCheck.setSelected(restaurant.isHasDelivery());
        prenotazioneCheck.setSelected(restaurant.isHasOnlineBooking());
        stellaVerdeCheck.setSelected(restaurant.isGreenStar());

        if (restaurant.getCuisinesTypes() != null) {
            for (CuisineType c : restaurant.getCuisinesTypes())
                cucinaListView.getSelectionModel().select(c);
        }

        if (restaurant.getServices() != null) {
            servizi.addAll(restaurant.getServices());
            renderServizi();
        }
    }

    @FXML
    private void onAggiungiServizioClick() {
        String s = servizioField.getText() != null ? servizioField.getText().trim() : "";
        if (!s.isEmpty() && servizi.add(s))
            renderServizi();
        servizioField.clear();
    }

    private void renderServizi() {
        serviziFlow.getChildren().clear();
        for (String s : servizi) {
            HBox chip = new HBox(4);
            chip.setAlignment(Pos.CENTER_LEFT);
            chip.getStyleClass().addAll("badge", "badge-booking");

            Label label = new Label(s);
            Button remove = new Button("✕");
            remove.setStyle("-fx-background-color: transparent; -fx-padding: 0 0 0 2; "
                    + "-fx-font-size: 10px; -fx-text-fill: inherit; -fx-cursor: hand;");
            remove.setOnAction(e -> {
                servizi.remove(s);
                renderServizi();
            });

            chip.getChildren().addAll(label, remove);
            serviziFlow.getChildren().add(chip);
        }
    }

    @FXML
    private void onSalvaClick() {
        clearAllErrors();

        String nome = testoDi(nomeField);
        String descrizione = testoDi(descrizioneField);
        String sitoWeb = testoDi(sitoWebField);
        String telefono = testoDi(telefonoField);
        String indirizzo = testoDi(indirizzoField);
        String citta = testoDi(cittaField);
        String nazioneTesto = testoDi(nazioneField);
        PriceRange prezzo = prezzoCombo.getValue();
        Award premio = premioCombo.getValue() != null ? premioCombo.getValue() : Award.NONE;
        Set<CuisineType> cucineSelezionate = new HashSet<>(cucinaListView.getSelectionModel().getSelectedItems());

        boolean valid = true;

        if (!nome.matches("[\\p{L}0-9 \\-']{4,30}")) {
            showFieldError(nomeError, "4-30 caratteri: lettere, numeri, spazi, - o '.", nomeField);
            valid = false;
        }

        Nation nazione = null;
        if (!nazioneTesto.isEmpty()) {
            nazione = Nation.fromString(nazioneTesto);
            if (nazione == null) {
                showFieldError(nazioneError, "Nazione non riconosciuta.", nazioneField);
                valid = false;
            }
        }

        if (prezzo == null) {
            showFieldError(prezzoError, "Seleziona una fascia di prezzo.", prezzoCombo);
            valid = false;
        }

        if (!valid) {
            errorLabel.getStyleClass().removeAll("label-success");
            errorLabel.getStyleClass().add("label-error");
            errorLabel.setText("Controlla i campi evidenziati.");
            return;
        }

        Location location = new Location(nazione, citta.isEmpty() ? null : citta, 0.0, 0.0,
                indirizzo.isEmpty() ? null : indirizzo);

        if (restaurantInModifica == null) {
            Restaurant nuovo = new Restaurant(nome, descrizione, sitoWeb, owner, telefono, location, prezzo,
                    consegnaCheck.isSelected(), prenotazioneCheck.isSelected(), premio, stellaVerdeCheck.isSelected(),
                    cucineSelezionate, new HashSet<>(servizi), new HashMap<>());
            GuiContext.getDataStore().getOwnerDAO().addOwnedRestaurant(owner.getId(), nuovo);
        } else {
            aggiornaRistorante(location, nome, descrizione, sitoWeb, telefono, prezzo, premio, cucineSelezionate);
            GuiContext.getDataStore().getRestaurantDAO().update(restaurantInModifica);
        }

        chiudiFinestra();
    }

    private void aggiornaRistorante(Location location, String nome, String descrizione, String sitoWeb,
                                    String telefono, PriceRange prezzo, Award premio,
                                    Set<CuisineType> cucineSelezionate) {
        Restaurant r = restaurantInModifica;

        if (!nome.equals(r.getName()))
            owner.renameRestaurant(r.getId(), nome);
        owner.modifyRestaurantDescription(r, descrizione);
        owner.modifyRestaurantWebsite(r, sitoWeb);
        owner.modifyRestaurantPhone(r, telefono);
        owner.modifyRestaurantLocation(r, location);
        owner.modifyRestaurantPriceRange(r, prezzo);
        owner.modifyRestaurantAward(r, premio);
        owner.modifyRestaurantGreenStar(r, stellaVerdeCheck.isSelected());
        owner.modifyRestaurantHasDelivery(r, consegnaCheck.isSelected());
        owner.modifyRestaurantHasBooking(r, prenotazioneCheck.isSelected());

        Set<CuisineType> cucineAttuali = new HashSet<>(r.getCuisinesTypes());
        for (CuisineType c : cucineAttuali)
            if (!cucineSelezionate.contains(c)) r.removeCuisineType(c);
        for (CuisineType c : cucineSelezionate)
            if (!cucineAttuali.contains(c)) r.addCuisineType(c);

        Set<String> serviziAttuali = new HashSet<>(r.getServices());
        for (String s : serviziAttuali)
            if (!servizi.contains(s)) r.removeService(s);
        for (String s : servizi)
            if (!serviziAttuali.contains(s)) r.addService(s);
    }

    @FXML
    private void onAnnullaClick() {
        chiudiFinestra();
    }

    @FXML
    private void onIndietroClick() {
        chiudiFinestra();
    }

    private void chiudiFinestra() {
        Stage stage = (Stage) btnAnnulla.getScene().getWindow();
        stage.close();
    }

    private String formattaCucina(CuisineType c) {
        String[] parole = c.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parole) {
            if (p.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    private ListCell<PriceRange> priceRangeCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(PriceRange item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getSymbol() + "  ·  " + capitalize(item.name()));
            }
        };
    }

    private ListCell<Award> awardCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Award item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item == Award.NONE ? "Nessuno" : capitalize(item.toString())));
            }
        };
    }

    private String capitalize(String s) {
        String lower = s.toLowerCase(Locale.ROOT).replace('_', ' ');
        return lower.isEmpty() ? lower : Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String testoDi(TextInputControl field) {
        return field.getText() != null ? field.getText().trim() : "";
    }

    private void showFieldError(Label errorLabel, String message, Control field) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (!field.getStyleClass().contains("error"))
            field.getStyleClass().add("error");
    }

    private void clearFieldError(Label errorLabel, Control field) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        field.getStyleClass().remove("error");
    }

    private void clearAllErrors() {
        errorLabel.setText("");
        Label[] errors = {nomeError, nazioneError, prezzoError};
        Control[] fields = {nomeField, nazioneField, prezzoCombo};
        for (int i = 0; i < errors.length; i++)
            clearFieldError(errors[i], fields[i]);
    }
}
