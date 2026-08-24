package com.example.demo3;

import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class GUIController {

    private static final Duration SLIDE_DURATION = Duration.millis(250);
    private static final Duration PAUSE_DURATION = Duration.millis(100);
    private static final double BLUR_RADIUS = 6;

    @FXML private VBox menuCard;
    @FXML private VBox titleBlock;
    @FXML private VBox menuButtons;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;
    @FXML private Button btnCerca;
    @FXML private Button btnEsplora;
    @FXML private Button btnEsci;

    private Node loginForm;
    private Node registerForm;
    private LoginController loginController;
    private RegisterController registerController;
    private boolean animating = false;

    @FXML
    public void initialize() {
        menuCard.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            menuCard.setClip(new javafx.scene.shape.Rectangle(
                    newBounds.getWidth(), newBounds.getHeight()));
        });
        preloadForms();
    }

    private void preloadForms() {
        try {
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
            loginForm = loginLoader.load();
            loginController = loginLoader.getController();
            loginController.setOnCancelCallback(this::showMenu);

            FXMLLoader registerLoader = new FXMLLoader(getClass().getResource("Register.fxml"));
            registerForm = registerLoader.load();
            registerController = registerLoader.getController();
            registerController.setOnCancelCallback(this::showMenu);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double getCardWidth() {
        return menuCard.getLayoutBounds().getWidth();
    }

    private void setBlur(Node node, double radius) {
        node.setEffect(radius > 0 ? new GaussianBlur(radius) : null);
    }

    private void blurNodes(double radius, Node... nodes) {
        for (Node n : nodes) setBlur(n, radius);
    }

    @FXML
    private void onLoginClick() {
        if (animating) return;
        showForm(loginForm);
    }

    @FXML
    private void onRegisterClick() {
        if (animating) return;
        showForm(registerForm);
    }

    private void showForm(Node form) {
        animating = true;
        menuCard.setMouseTransparent(true);

        double dist = getCardWidth();
        blurNodes(BLUR_RADIUS, titleBlock, menuButtons);

        ParallelTransition slideOut = new ParallelTransition();
        for (Node node : new Node[]{titleBlock, menuButtons}) {
            TranslateTransition t = new TranslateTransition(SLIDE_DURATION, node);
            t.setFromX(0);
            t.setToX(-dist);
            slideOut.getChildren().add(t);
        }

        slideOut.setOnFinished(e -> {
            blurNodes(0, titleBlock, menuButtons);
            for (Node node : new Node[]{titleBlock, menuButtons}) {
                node.setVisible(false);
                node.setManaged(false);
                node.setTranslateX(0);
            }

            form.setTranslateX(dist);
            form.setVisible(false);
            form.setManaged(false);
            menuCard.getChildren().add(form);
            form.setVisible(true);
            form.setManaged(true);
            setBlur(form, BLUR_RADIUS);

            PauseTransition pause = new PauseTransition(PAUSE_DURATION);
            pause.setOnFinished(e2 -> {
                ParallelTransition slideIn = new ParallelTransition();
                TranslateTransition t = new TranslateTransition(SLIDE_DURATION, form);
                t.setFromX(dist);
                t.setToX(0);
                slideIn.getChildren().add(t);

                slideIn.setOnFinished(e3 -> {
                    setBlur(form, 0);
                    animating = false;
                    menuCard.setMouseTransparent(false);
                });
                slideIn.play();
            });
            pause.play();
        });

        slideOut.play();
    }

    private void showMenu() {
        if (animating) return;
        animating = true;
        menuCard.setMouseTransparent(true);

        double dist = getCardWidth();

        Node formNode = null;
        for (Node child : menuCard.getChildren()) {
            if (child != titleBlock && child != menuButtons) {
                formNode = child;
                break;
            }
        }
        if (formNode == null) {
            animating = false;
            menuCard.setMouseTransparent(false);
            return;
        }

        final Node form = formNode;
        setBlur(form, BLUR_RADIUS);

        ParallelTransition slideOut = new ParallelTransition();
        TranslateTransition t = new TranslateTransition(SLIDE_DURATION, form);
        t.setFromX(0);
        t.setToX(dist);
        slideOut.getChildren().add(t);

        slideOut.setOnFinished(e -> {
            setBlur(form, 0);
            menuCard.getChildren().remove(form);

            titleBlock.setTranslateX(-dist);
            menuButtons.setTranslateX(-dist);
            titleBlock.setVisible(false);
            titleBlock.setManaged(false);
            menuButtons.setVisible(false);
            menuButtons.setManaged(false);

            titleBlock.setVisible(true);
            titleBlock.setManaged(true);
            menuButtons.setVisible(true);
            menuButtons.setManaged(true);
            blurNodes(BLUR_RADIUS, titleBlock, menuButtons);

            PauseTransition pause = new PauseTransition(PAUSE_DURATION);
            pause.setOnFinished(e2 -> {
                ParallelTransition slideIn = new ParallelTransition();
                for (Node node : new Node[]{titleBlock, menuButtons}) {
                    TranslateTransition ti = new TranslateTransition(SLIDE_DURATION, node);
                    ti.setFromX(-dist);
                    ti.setToX(0);
                    slideIn.getChildren().add(ti);
                }

                slideIn.setOnFinished(e3 -> {
                    blurNodes(0, titleBlock, menuButtons);
                    animating = false;
                    menuCard.setMouseTransparent(false);
                });
                slideIn.play();
            });
            pause.play();
        });

        slideOut.play();
    }

    @FXML
    private void onCercaClick() {
        apriRestaurants(true, btnCerca);
    }

    @FXML
    private void onEsploraClick() {
        apriRestaurants(false, btnEsplora);
    }

    private void apriRestaurants(boolean modalitaRicerca, Button sourceButton) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("Restaurants.fxml"));
            javafx.scene.Parent root = loader.load();

            RestaurantsController controller = loader.getController();
            controller.inizializza(modalitaRicerca);

            Stage stage = (Stage) sourceButton.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 650, 500);
            stage.setScene(scene);
            stage.setTitle(modalitaRicerca ? "Cerca Ristorante" : "Esplora Ristoranti");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onEsciClick() {
        Stage stage = (Stage) btnEsci.getScene().getWindow();
        stage.close();
    }
}
