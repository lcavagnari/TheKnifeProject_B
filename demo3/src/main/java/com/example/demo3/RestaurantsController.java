package com.example.demo3;

import com.example.demo3.data.RestaurantRepository;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class RestaurantsController {

    private final RestaurantRepository restaurantRepository = new RestaurantRepository();

    @FXML
    private Label titleLabel;
    @FXML
    private Label infoLabel;
    @FXML
    private HBox searchBar;
    @FXML
    private TextField searchField;
    @FXML
    private ListView<Restaurant> restaurantListView;
    @FXML
    private Button btnIndietro;

    public void inizializza() {
        restaurantListView.setCellFactory(lv -> new RestaurantCell());

        restaurantListView.setOnMouseClicked(event -> {
            Restaurant selezionato = restaurantListView.getSelectionModel().getSelectedItem();
            if (selezionato != null) {
                apriDettagli(selezionato);
            }
        });

        searchBar.setVisible(true);
        searchBar.setManaged(true);
        caricaEMostra(restaurantRepository.caricaTutti());
        infoLabel.setText("Cerca per nome, cucina o citta. Clicca su un ristorante per i dettagli.");
    }

    private void apriDettagli(Restaurant r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RestaurantDetails.fxml"));
            Parent root = loader.load();

            RestaurantDetailsController controller = loader.getController();
            controller.carica(r);

            Stage stage = (Stage) restaurantListView.getScene().getWindow();
            Scene scene = new Scene(root, 650, 550);
            stage.setScene(scene);
            stage.setTitle(r.getName() != null ? r.getName() : "Dettagli ristorante");
            stage.show();
        } catch (IOException e) {
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
        Stage stage = (Stage) btnIndietro.getScene().getWindow();
        Navigation.navigateTo(stage, "GUI.fxml", "The Knife Menu", 600, 400);
    }

    private void caricaEMostra(List<Restaurant> risultati) {
        restaurantListView.getItems().setAll(risultati);
        infoLabel.setText(risultati.size() + " ristorant" + (risultati.size() == 1 ? "e trovato" : "i trovati"));
    }

    private static class RestaurantCell extends ListCell<Restaurant> {
        @Override
        protected void updateItem(Restaurant r, boolean empty) {
            super.updateItem(r, empty);
            if (empty || r == null) {
                setText(null);
                return;
            }
            // Mostra nome, città e una valutazione media se disponibile
            String city = r.getLocation() != null ? r.getLocation().getCity() : "";
            String rating = "⭐ Nuovo";
            if (r.getReviews() != null && !r.getReviews().isEmpty()) {
                double avg = r.getReviews().values().stream()
                        .mapToInt(Review::getValue)
                        .average()
                        .orElse(0.0);
                rating = String.format("★ %.1f", avg);
            }
            setText(r.getName() + " — " + city + "   " + rating);
        }
    }
}