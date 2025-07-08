package it.uninsubria.laboratorioa.objects;

import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.utils.IO;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Rappresenta una recensione lasciata da un {@link User} su un {@link Restaurant}.<p>
 * Contiene valutazione numerica, testo, risposta del ristorante e timestamp.<p>
 * Estende {@link JsonEntity} per la gestione e persistenza JSON.<p>
 *
 * @author Luke
 * @version 1.0
 */
@Getter
public class Review extends JsonEntity {
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
     * Costruttore completo con tutti i campi, incluso reply.<p>
     * Genera un nuovo UUID e costruisce l'oggetto JSON.
     *
     * @param restaurant ristorante associato
     * @param user       utente autore
     * @param value      valutazione numerica
     * @param timestamp  data e ora recensione
     * @param text       testo recensione
     * @param reply      risposta ristorante (opzionale)
     */
    public Review(Restaurant restaurant, User user, int value, LocalDateTime timestamp, String text, String reply) {
        super(UUID.randomUUID());
        this.timestamp = timestamp;
        this.restaurant = restaurant;
        this.user = user;

        this.value = value;
        this.text = text;
        this.reply = reply;

        build();
    }

    /**
     * Costruttore senza risposta.<p>
     * Genera un nuovo UUID e costruisce l'oggetto JSON.
     *
     * @param restaurant ristorante associato
     * @param user       utente autore
     * @param value      valutazione numerica
     * @param timestamp  data e ora recensione
     * @param text       testo recensione
     */
    public Review(Restaurant restaurant, User user, int value, LocalDateTime timestamp, String text) {
        super(UUID.randomUUID());
        this.timestamp = timestamp;
        this.restaurant = restaurant;
        this.user = user;

        this.value = value;
        this.text = text;
        this.reply = null;

        IO.validateReview(this);
        build();
    }

    /**
     * Costruttore semplificato con timestamp corrente.<p>
     * Non costruisce automaticamente JSON.
     *
     * @param restaurant ristorante associato
     * @param user       utente autore
     * @param value      valutazione numerica
     * @param text       testo recensione
     */
    public Review(Restaurant restaurant, User user, int value, String text) {
        this.restaurant = restaurant;
        this.user = user;
        this.value = value;
        this.text = text;

        this.timestamp = LocalDateTime.now();
    }

    /**
     * Imposta il valore di valutazione se compreso tra 1 e 5.<p>
     * Ricostruisce l'oggetto JSON.
     *
     * @param value nuovo valore valutazione
     */
    public void setValue(int value) {
        if (value < 1 || value > 5) return;

        this.value = value;
        rebuild();
    }

    /**
     * Imposta il testo della recensione se non nullo o vuoto.<p>
     * Ricostruisce l'oggetto JSON.
     *
     * @param text nuovo testo recensione
     */
    public void setText(String text) {
        if (text == null || text.isBlank()) return;

        this.text = text;
        rebuild();
    }

    /**
     * Imposta la risposta del ristorante se il testo della recensione è valido.<p>
     * Ricostruisce l'oggetto JSON.
     *
     * @param reply nuova risposta ristorante
     */
    public void setReply(String reply) {
        if (text == null || text.isBlank()) return;

        this.reply = reply;
        rebuild();
    }

    /**
     * Costruisce la rappresentazione JSON della recensione.<p>
     * Imposta timestamp, utente, valore, testo e risposta.
     */
    @Override
    protected void build() {
        this.jsonObject.put("timestamp", timestamp.toString())
                .put("user", user.getId().toString())
                .put("value", value)
                .put("text", text)
                .put("reply", reply);
    }

    /**
     * Rappresentazione testuale dettagliata della recensione.
     *
     * @return stringa descrittiva della recensione
     */
    @Override
    public String toString() {
        return "Review{" +
                "timestamp=" + timestamp +
                ", company=" + restaurant +
                ", person=" + user +
                ", value=" + value +
                ", text='" + text + '\'' +
                ", reply='" + reply + '\'' +
                ", id=" + id +
                '}';
    }

    /**
     * Sovrascrive il metodo save.<p>
     * Costruisce l'oggetto JSON ma non salva fisicamente.<p>
     * Ritorna sempre false.
     *
     * @return false
     */
    @Override
    public boolean save() {
        build();
        return false;
    }
}
