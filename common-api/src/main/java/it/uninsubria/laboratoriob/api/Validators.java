package it.uninsubria.laboratoriob.api;

import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.objects.User;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class Validators {
    /**
     * Verifica uno o più valori stringa rispetto a un pattern regex.
     *
     * @param regex pattern di validazione
     * @param value valore da controllare
     * @return true se valido
     * @throws IllegalArgumentException se nullo o non conforme
     */
    public static boolean validateString(final String regex, String value) throws IllegalArgumentException {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Il valore inserito non può essere vuoto.");
        if (!value.matches(regex))
            throw new IllegalArgumentException("Il valore inserito contiene caratteri non validi o ha un numero di caratteri non consentito (4-200).");
        return true;
    }

    /**
     * Variante di {@link #validateString(String, String)} con regex predefinita.
     */
    public static boolean validateString(String value) throws IllegalArgumentException {
        return validateString("^[\\p{L}0-9 \\-']{4,200}$", value);
    }

    /**
     * Verifica la correttezza di una Location.
     *
     * @param loc oggetto Location
     * @return true se valida
     */
    public static boolean validateLocation(Location loc) throws IllegalArgumentException {
        if (loc == null)
            throw new IllegalArgumentException("Impossibile determinare posizione, riprovare più tardi");
        if (loc.getNation() == null)
            throw new IllegalArgumentException("Impossibile determinare nazione: input non valido");
        if (loc.getCity() == null || loc.getCity().isBlank())
            throw new IllegalArgumentException("Impossibile determinare città: input non valido");
        if (loc.getAddress() == null || loc.getAddress().isBlank())
            throw new IllegalArgumentException("Impossibile determinare indirizzo: input non valido");
        if (loc.getLatitude() < -90.0 || loc.getLatitude() > 90.0)
            throw new IllegalArgumentException("Latitudine fuori intervallo (-90,+90)");
        if (loc.getLongitude() < -180.0 || loc.getLongitude() > 180.0)
            throw new IllegalArgumentException("Longitudine fuori intervallo (-180,+180)");
        return true;
    }

    /**
     * Verifica che la data sia compresa in un intervallo.
     */
    public static boolean validateDates(LocalDateTime min, LocalDateTime max, LocalDateTime date) throws IllegalArgumentException {
        if (date == null)
            throw new IllegalArgumentException("Impossibile verificare data, riprovare più tardi");
        if (date.isAfter(max) || date.isBefore(min))
            throw new IllegalArgumentException("Data fuori da intervallo accettabile");
        return true;
    }

    /**
     * Variante senza intervallo (qualsiasi data valida).
     */
    public static boolean validateDates(LocalDateTime date) throws IllegalArgumentException {
        return validateDates(LocalDateTime.MIN, LocalDateTime.MAX, date);
    }

    /**
     * Verifica la validità di un identificativo UUID.
     *
     * @param id identificatore da controllare
     * @return true se valido
     * @throws IllegalArgumentException se nullo
     */
    public static boolean validateUUID(UUID id) throws IllegalArgumentException {
        if (id == null)
            throw new IllegalArgumentException("Impossibile ottenere id utente, riprovare più tardi");
        return true;
    }

    /**
     * Verifica la correttezza di un oggetto Review.
     *
     * @param r recensione da validare
     * @return true se valida
     */
    public static boolean validateReview(Review r) throws IllegalArgumentException {
        if (r == null)
            throw new IllegalArgumentException("Recensione non può essere nulla.");
        if (r.getValue() < 1 || r.getValue() > 5)
            throw new IllegalArgumentException("Valutazione deve essere compresa tra 1 e 5.");
        if (r.getReply() != null && !r.getReply().matches("^[\\p{L}0-9 \\-']{4,200}$"))
            throw new IllegalArgumentException("Risposta non valida (4-200 caratteri).");

        validateUUID(r.getId());
        validateString("^[\\p{L}0-9 \\-']{4,200}$", r.getText());
        validateDates(r.getTimestamp());
        return true;
    }

    /**
     * Verifica la correttezza di un utente.
     *
     * @param user utente da validare
     * @return true se valido
     */
    public static boolean validateUser(User user) throws IllegalArgumentException {
        if (user == null)
            throw new IllegalArgumentException("Impossibile ottenere utente, riprovare più tardi");

        validateUUID(user.getId());
        validateString("^[\\p{L}][\\p{L}'\\- ]{1,39}$", user.getName());
        validateString("^[a-zA-Z][\\w.]{1,14}[a-zA-Z0-9]$", user.getUsername());

        if (user.getLocation() != null) validateLocation(user.getLocation());
        validateDates(LocalDateTime.MIN, LocalDateTime.now().plusDays(1), user.getDateOfBirth().atStartOfDay());
        return true;
    }

    /**
     * Verifica la correttezza di un ristorante.
     *
     * @param r ristorante da validare
     * @return true se valido
     * @throws IllegalArgumentException se uno dei campi non è conforme
     */
    public static boolean validateRestaurant(Restaurant r) throws IllegalArgumentException {
        if (r == null)
            throw new IllegalArgumentException("Impossibile ottenere dati ristorante, riprovare più tardi");

        validateUUID(r.getId());
        validateString("^[\\p{L}][\\p{L}'\\- ]{1,40}$", r.getName());
        validateString(r.getDescription());
        validateString("^(https?://)?[\\w.-]+(\\.[a-z]{2,})+.*$", r.getWebsiteUrl());
        validateString("^\\+\\d{8,15}$", r.getPhone());
        validateUser(r.getOwner());
        return true;
    }
}
