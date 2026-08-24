package it.uninsubria.laboratoriob.api.objects;

import it.uninsubria.laboratoriob.api.Entity;
import it.uninsubria.laboratoriob.api.Validators;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Rappresenta una recensione lasciata da un {@link User} su un
 * {@link Restaurant}.
 * <p>
 * Contiene valutazione numerica, testo, risposta del ristorante e timestamp.
 * <p>
 * Estende {@link Entity}; persistita nella tabella recensioni con FK verso
 * ristorante e utente.
 * <p>
 *
 * @author Luke
 * @version 2.0
 */
@Getter
public class Review extends Entity {
    /**
     * Timestamp di creazione o aggiornamento della recensione.
     */
    private final LocalDateTime timestamp;

    /**
     * Ristorante a cui è associata la recensione.
     */
    private final Restaurant restaurant;

    /**
     * Utente autore della recensione.
     */
    private final User user;

    /**
     * Valutazione numerica (da 1 a 5).
     */
    private int value;

    /**
     * Testo della recensione.
     */
    private String text;

    /**
     * Risposta del ristorante alla recensione (opzionale).
     */
    private String reply;

    /**
     * Costruttore completo con tutti i campi, incluso reply.
     * <p>
     */
    public Review(UUID id, Restaurant restaurant, User user, int value, LocalDateTime timestamp, String text,
                  String reply) {
        super(id);
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
        this.restaurant = restaurant;
        this.user = user;
        this.value = value;
        this.text = text;
        this.reply = reply;
        Validators.validateReview(this);
    }

    public Review(Restaurant restaurant, User user, int value, LocalDateTime timestamp, String text, String reply) {
        this(UUID.randomUUID(), restaurant, user, value, timestamp, text, reply);
    }

    public Review(Restaurant restaurant, int value, User user, String text) {
        this(UUID.randomUUID(), restaurant, user, value, LocalDateTime.now(), text, null);
    }

    public Review(Restaurant restaurant, User user, int value, String text) {
        this(UUID.randomUUID(), restaurant, user, value, LocalDateTime.now(), text, null);
    }

    public int getRating() {
        return value;
    }

    public String getComment() {
        return text;
    }

    public void setValue(int value) {
        if (value < 1 || value > 5)
            return;
        this.value = value;
    }

    public void setText(String text) {
        if (text == null || text.isBlank())
            return;
        this.text = text;
    }

    public void setReply(String reply) {
        if (reply == null || reply.isBlank())
            return;
        this.reply = reply;
    }

    @Override
    public String toString() {
        return "│ ID:               " + id + "\n" +
                "│ Timestamp:        " + timestamp + "\n" +
                "│ User:             " + (user != null ? user.getUsername() : "N/A") + "\n" +
                "│ Restaurant:       " + (restaurant != null ? restaurant.getName() : "N/A") + "\n" +
                "│ Rating:           " + value + " / 5" + "\n" +
                "│ Text:             " + (text != null && !text.isBlank() ? "\"" + text + "\"" : "N/A") + "\n" +
                "│ Reply:            " + (reply != null && !reply.isBlank() ? "\"" + reply + "\"" : "None") + "\n";
    }
}
