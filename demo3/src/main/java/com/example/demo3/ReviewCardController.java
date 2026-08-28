package com.example.demo3;

import it.uninsubria.laboratoriob.api.objects.Review;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private Button deleteButton;

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
    }

    public void setContext(Review review, String currentUserId, boolean isOwner) {
        setReview(review);
        this.currentUserId = currentUserId;
        this.isOwner = isOwner;

        // Mutua esclusività
        boolean isAuthor = currentUserId != null && currentUserId.equals(review.getAuthorId());

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
        System.out.println("Rispondi alla recensione " + review.getId());
        // Aggiungi qui la logica per aprire un dialogo di risposta
    }

    @FXML
    private void onDeleteAction() {
        System.out.println("Elimina la recensione " + review.getId());
        // Aggiungi qui la logica per eliminare la recensione (es. API call)
    }
}