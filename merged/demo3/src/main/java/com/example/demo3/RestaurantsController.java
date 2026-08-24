package com.example.demo3;

import com.example.demo3.data.RestaurantRepository;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
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

    /**
     * Chiamato da GUIController subito dopo aver caricato l'FXML, PRIMA di stage.show().
     * modalitaRicerca = true  -> schermata "Cerca Ristorante" (barra ricerca visibile, lista inizialmente vuota)
     * modalitaRicerca = false -> schermata "Esplora Ristoranti" (barra ricerca nascosta, lista già piena)
     */
    public void inizializza(boolean modalitaRicerca) {
        restaurantListView.setCellFactory(lv -> new RestaurantCell());

        restaurantListView.setOnMouseClicked(event -> {
            Restaurant selezionato = restaurantListView.getSelectionModel().getSelectedItem();
            if (selezionato != null) {
                apriDettagli(selezionato);
            }
        });

        if (modalitaRicerca) {
            titleLabel.setText("Cerca Ristorante");
            searchBar.setVisible(true);
            searchBar.setManaged(true);
            infoLabel.setText("Digita una parola chiave e premi Cerca. Clicca su un ristorante per i dettagli.");
        } else {
            titleLabel.setText("Esplora Ristoranti");
            searchBar.setVisible(false);
            searchBar.setManaged(false);
            caricaEMostra(restaurantRepository.caricaTutti());
        }
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GUI.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnIndietro.getScene().getWindow();
            Scene scene = new Scene(root, 600, 400);
            stage.setScene(scene);
            stage.setTitle("The Knife Menu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void caricaEMostra(List<Restaurant> risultati) {
        restaurantListView.getItems().setAll(risultati);
        infoLabel.setText(risultati.size() + " ristorant" + (risultati.size() == 1 ? "e trovato" : "i trovati"));
    }

    /**
     * Cella custom per mostrare nome, categoria, città e rating su più righe.
     */
    private static class RestaurantCell extends ListCell<Restaurant> {
        @Override
        protected void updateItem(Restaurant r, boolean empty) {
            super.updateItem(r, empty);
            if (empty || r == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            setText(r.getName());
            setStyle("-fx-padding: 10 5 10 5; -fx-font-size: 14px;");
        }
    }
}
