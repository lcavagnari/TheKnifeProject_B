package it.uninsubria.laboratoriob.client.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Shared input box: star rating + text + Invia/Annulla. Used both to write a
 * new review (stars shown) and to reply to one (stars hidden via {@link #setReplyMode()}).
 */
public class ReviewInputBoxController {

    public record Result(int rating, String text) {
    }

    @FXML
    private VBox starsSection;
    @FXML
    private Label star1;
    @FXML
    private Label star2;
    @FXML
    private Label star3;
    @FXML
    private Label star4;
    @FXML
    private Label star5;
    @FXML
    private TextArea textField;
    @FXML
    private Label errorLabel;

    private Label[] stars;
    private int selectedRating = 0;
    private Consumer<Result> onSubmit;
    private Runnable onCancel;

    @FXML
    private void initialize() {
        stars = new Label[]{star1, star2, star3, star4, star5};
    }

    public void setReplyMode() {
        starsSection.setVisible(false);
        starsSection.setManaged(false);
    }

    public void setReviewMode() {
        starsSection.setVisible(true);
        starsSection.setManaged(true);
    }

    public void setText(String text) {
        textField.setText(text != null ? text : "");
    }

    public void setRating(int rating) {
        selectRating(rating);
    }

    public void setOnSubmit(Consumer<Result> handler) {
        this.onSubmit = handler;
    }

    public void setOnCancel(Runnable handler) {
        this.onCancel = handler;
    }

    public void clear() {
        textField.clear();
        selectRating(0);
        clearError();
    }

    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void selectRating(int rating) {
        selectedRating = rating;
        for (int i = 0; i < stars.length; i++) {
            stars[i].setText(i < rating ? "★" : "☆");
        }
    }

    @FXML
    private void onStar1Clicked() {
        selectRating(1);
    }

    @FXML
    private void onStar2Clicked() {
        selectRating(2);
    }

    @FXML
    private void onStar3Clicked() {
        selectRating(3);
    }

    @FXML
    private void onStar4Clicked() {
        selectRating(4);
    }

    @FXML
    private void onStar5Clicked() {
        selectRating(5);
    }

    @FXML
    private void onSubmitAction() {
        clearError();
        if (onSubmit != null) onSubmit.accept(new Result(selectedRating, textField.getText()));
    }

    @FXML
    private void onCancelAction() {
        clearError();
        if (onCancel != null) onCancel.run();
    }
}
