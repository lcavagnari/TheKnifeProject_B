package it.uninsubria.laboratoriob.client.gui.controller;
import it.uninsubria.laboratoriob.client.gui.GuiContext;

import it.uninsubria.laboratoriob.api.objects.Review;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.time.format.DateTimeFormatter;

public class ReviewCardController {

    @FXML
    private Label ratingLabel;
    @FXML
    private Label starsLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label commentLabel;
    @FXML
    private Button replyButton;
    @FXML
    private Button replyDeleteCancelButton;
    @FXML
    private Button deleteButton;
    @FXML
    private VBox replyDisplay;
    @FXML
    private Label replyTextLabel;
    @FXML
    private VBox replyInputBox;
    @FXML
    private TextArea replyField;

    private enum StatoBottoneRisposta { NESSUNA_RISPOSTA, RISPOSTA_PRESENTE, CONFERMA_CANCELLAZIONE }

    private StatoBottoneRisposta statoBottoneRisposta = StatoBottoneRisposta.NESSUNA_RISPOSTA;

    private Review review;
    private String currentUserId;
    private boolean isOwner;



    private static final DateTimeFormatter REVIEW_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setReview(Review review) {
        this.review = review;
        ratingLabel.setText(String.valueOf(review.getValue()));

        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= review.getValue() ? "★" : "☆");
        }
        starsLabel.setText(stars.toString());
        timeLabel.setText(review.getTimestamp().format(REVIEW_TIMESTAMP_FORMAT));

        if (review.getComment() != null && !review.getComment().isBlank()) {
            commentLabel.setText(review.getComment());
        } else {
            commentLabel.setText("No review text provided.");
        }

        aggiornaRispostaVisibile();
    }

    private void aggiornaRispostaVisibile() {
        boolean hasReply = review.getReply() != null && !review.getReply().isBlank();
        replyDisplay.setVisible(hasReply);
        replyDisplay.setManaged(hasReply);
        if (hasReply) {
            replyTextLabel.setText(review.getReply());
        }
        impostaStatoBottoneRisposta(hasReply ? StatoBottoneRisposta.RISPOSTA_PRESENTE : StatoBottoneRisposta.NESSUNA_RISPOSTA);
    }

    private void impostaStatoBottoneRisposta(StatoBottoneRisposta stato) {
        statoBottoneRisposta = stato;

        boolean confermaAttiva = stato == StatoBottoneRisposta.CONFERMA_CANCELLAZIONE;
        replyDeleteCancelButton.setVisible(confermaAttiva);
        replyDeleteCancelButton.setManaged(confermaAttiva);

        replyButton.getStyleClass().removeAll("button-secondary", "button-danger");
        switch (stato) {
            case NESSUNA_RISPOSTA -> {
                replyButton.setText("Reply");
                replyButton.getStyleClass().add("button-secondary");
            }
            case RISPOSTA_PRESENTE -> {
                replyButton.setText("Cancella risposta");
                replyButton.getStyleClass().add("button-secondary");
            }
            case CONFERMA_CANCELLAZIONE -> {
                replyButton.setText("Conferma");
                replyButton.getStyleClass().add("button-danger");
            }
        }
    }

    public void setContext(Review review, String currentUserId, boolean isOwner) {
        setReview(review);
        this.currentUserId = currentUserId;
        this.isOwner = isOwner;

        // Mutua esclusività
        boolean isAuthor = currentUserId != null && review.getUser() != null
                && currentUserId.equals(review.getUser().getId().toString());

        if (isAuthor) {
            // L'autore può solo eliminare
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
            replyButton.setVisible(false);
            replyButton.setManaged(false);
        } else if (isOwner) {
            // Il proprietario può solo rispondere
            replyButton.setVisible(true);
            replyButton.setManaged(true);
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        } else {
            // Nessun bottone
            replyButton.setVisible(false);
            replyButton.setManaged(false);
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }
    }

    @FXML
    private void onReplyAction() {
        switch (statoBottoneRisposta) {
            case NESSUNA_RISPOSTA -> apriCasellaRisposta();
            case RISPOSTA_PRESENTE -> impostaStatoBottoneRisposta(StatoBottoneRisposta.CONFERMA_CANCELLAZIONE);
            case CONFERMA_CANCELLAZIONE -> confermaCancellazioneRisposta();
        }
    }

    private void apriCasellaRisposta() {
        replyField.setText(review.getReply() != null ? review.getReply() : "");
        replyButton.setVisible(false);
        replyButton.setManaged(false);
        replyInputBox.setVisible(true);
        replyInputBox.setManaged(true);
    }

    @FXML
    private void onReplyCancel() {
        replyInputBox.setVisible(false);
        replyInputBox.setManaged(false);
        replyButton.setVisible(true);
        replyButton.setManaged(true);
    }

    @FXML
    private void onReplySubmit() {
        GuiContext.getDataStore().getReviewDAO().replyToReview(review, replyField.getText());
        aggiornaRispostaVisibile();
        onReplyCancel();
    }

    @FXML
    private void onReplyDeleteCancel() {
        impostaStatoBottoneRisposta(StatoBottoneRisposta.RISPOSTA_PRESENTE);
    }

    private void confermaCancellazioneRisposta() {
        review.clearReply();
        GuiContext.getDataStore().getReviewDAO().update(review);

        double larghezzaAttuale = replyButton.getWidth();
        replyButton.setMinWidth(larghezzaAttuale);
        replyButton.setPrefWidth(larghezzaAttuale);
        replyButton.setMaxWidth(larghezzaAttuale);
        replyButton.setDisable(true);
        replyButton.setText("✓");

        replyDeleteCancelButton.setVisible(false);
        replyDeleteCancelButton.setManaged(false);

        PauseTransition pausa = new PauseTransition(Duration.millis(700));
        pausa.setOnFinished(e -> {
            replyButton.setDisable(false);
            replyButton.setMinWidth(Region.USE_COMPUTED_SIZE);
            replyButton.setPrefWidth(Region.USE_COMPUTED_SIZE);
            replyButton.setMaxWidth(Region.USE_COMPUTED_SIZE);
            aggiornaRispostaVisibile();
        });
        pausa.play();
    }

    @FXML
    private void onDeleteAction() {
        System.out.println("Elimina la recensione " + review.getId());
        // Aggiungi qui la logica per eliminare la recensione (es. API call)
    }
}