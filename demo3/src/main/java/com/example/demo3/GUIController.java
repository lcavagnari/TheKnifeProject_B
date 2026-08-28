package com.example.demo3;

import com.example.demo3.data.Session;
import com.example.demo3.data.SessionRepository;
import com.example.demo3.data.UserRepository;
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
import java.util.Optional;

public class GUIController {

    private static final Duration SLIDE_DURATION = Duration.millis(250);
    private static final Duration PAUSE_DURATION = Duration.millis(100);
    private static final double BLUR_RADIUS = 6;

    private final UserRepository userRepository = new UserRepository();
    private final SessionRepository sessionRepository = new SessionRepository();

    @FXML private VBox menuCard;
    @FXML private VBox titleBlock;
    @FXML private VBox menuButtons;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;
    @FXML private Button btnTrova;
    @FXML private Button btnEsci;

    private Node loginForm;
    private Node registerForm;
    private Node homeForm;
    private LoginController loginController;
    private RegisterController registerController;
    private HomeController homeController;
    private boolean animating = false;

    @FXML
    public void initialize() {
        menuCard.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            menuCard.setClip(new javafx.scene.shape.Rectangle(
                    newBounds.getWidth(), newBounds.getHeight()));
        });
        preloadForms();
        ripristinaSessioneSalvata();

        if (Session.isLoggedIn()) {
            mostraHomeSenzaAnimazione();
        }
    }

    private void preloadForms() {
        try {
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
            loginForm = loginLoader.load();
            loginController = loginLoader.getController();
            loginController.setOnCancelCallback(this::showMenu);
            loginController.setOnLoginSuccessCallback(this::showHome);

            FXMLLoader registerLoader = new FXMLLoader(getClass().getResource("Register.fxml"));
            registerForm = registerLoader.load();
            registerController = registerLoader.getController();
            registerController.setOnCancelCallback(this::showMenu);

            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("Home.fxml"));
            homeForm = homeLoader.load();
            homeController = homeLoader.getController();
            homeController.setOnDisconnettiCallback(this::showMenu);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Se nessuno è già loggato in questa sessione dell'app, prova a
     * ripristinare l'ultimo utente loggato salvato su disco.
     */
    private void ripristinaSessioneSalvata() {
        if (Session.isLoggedIn()) {
            return;
        }
        Optional<String> username = sessionRepository.loadUsername();
        username.flatMap(userRepository::findByUsername).ifPresent(Session::login);
    }

    /**
     * Mostra subito Home al posto di titleBlock/menuButtons, senza
     * animazione: usata all'avvio quando una sessione salvata è stata
     * ripristinata prima che la scena sia visibile.
     */
    private void mostraHomeSenzaAnimazione() {
        homeController.aggiorna();

        titleBlock.setVisible(false);
        titleBlock.setManaged(false);
        menuButtons.setVisible(false);
        menuButtons.setManaged(false);

        if (!menuCard.getChildren().contains(homeForm)) {
            menuCard.getChildren().add(homeForm);
        }
        homeForm.setVisible(true);
        homeForm.setManaged(true);
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

    private void showHome() {
        if (animating) return;
        homeController.aggiorna();
        showForm(homeForm);
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

            // Drop any form already showing (e.g. Login handing off to Home) so it
            // doesn't stay stacked underneath the one we're about to show.
            menuCard.getChildren().removeIf(child -> child != titleBlock && child != menuButtons && child != form);

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
    private void onTrovaClick() {
        apriRestaurants(btnTrova);
    }

    private void apriRestaurants(Button sourceButton) {
        try {
            Stage stage = (Stage) sourceButton.getScene().getWindow();

            Navigation.pushBack(() -> Navigation.navigateTo(stage, "GUI.fxml", "The Knife Menu", 650, 500));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Restaurants.fxml"));
            javafx.scene.Parent root = loader.load();

            RestaurantsController controller = loader.getController();
            controller.inizializza();

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 650, 500);
            stage.setScene(scene);
            stage.setTitle("Trova Ristoranti");
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
