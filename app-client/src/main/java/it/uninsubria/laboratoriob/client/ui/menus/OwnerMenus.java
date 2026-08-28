package it.uninsubria.laboratoriob.client.ui.menus;

import it.uninsubria.laboratoriob.api.Validators;
import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.client.data.ClientDataStore;
import it.uninsubria.laboratoriob.client.ui.IO;
import it.uninsubria.laboratoriob.client.ui.Menus;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Menu per utenti di tipo Owner.
 */
public class OwnerMenus extends Menus {

    private final Owner owner;

    public OwnerMenus(Owner owner, ClientDataStore dataStore) {
        super(owner, dataStore);
        this.owner = owner;
    }

    @Override
    public void openMenu() {
        while (true) {
            IO.clearScreen();

            IO.printMenu(
                    "\n┌───────────── The Knife Main menu ─────────────┐",
                    "└───────────────────────────────────────────────────┘",
                    "Esplora Ristoranti", "Cerca un Ristorante", "Visualizza ristoranti posseduti",
                    "Visualizza ultime recensioni", "Disconnetti", "Esci"
            );
            int choice = IO.getInt("Enter choice:");

            switch (choice) {
                case 1 -> browseRestaurants();
                case 2 -> searchRestaurant();
                case 3 -> viewOwnedRestaurants();
                case 4 -> viewOwnerLatestReviews();
                case 5 -> {
                    IO.getUserInput("Procedura di logout completata. Premere 'Enter' per tornare al menu principale.");
                    return;
                }
                case 6 -> exit();
                default -> IO.printErrorMessage("Opzione non valida");
            }
        }
    }

    @Override
    protected void viewRestaurantDetails(Restaurant restaurant) {
        String originalName = restaurant.getName();

        while (true) {
            IO.clearScreen();

            Collection<Review> reviews = restaurant.getReviews().values();
            if (reviews.isEmpty()) {
                System.out.println("No reviews available for this restaurant.\n");
            } else {
                Review best = reviews.stream().max(Comparator.comparingInt(Review::getValue)).orElse(null);
                Review worst = reviews.stream().min(Comparator.comparingInt(Review::getValue)).orElse(null);

                System.out.println("\n★ Best Review:\n" + (best != null ? best : "N/A"));
                System.out.println("\n☆ Worst Review:\n" + (worst != null ? worst : "N/A"));
            }

            IO.printMenu(
                    "┌─────Operazioni disponibili:─────┐",
                    "└──────────────────────────────────┘",
                    "Modifica Nome",
                    "Modifica Descrizione",
                    "Modifica Website",
                    "Modifica Telefono",
                    "Modifica Location",
                    "Modifica Fascia di Prezzo",
                    "Modifica Premi",
                    "Modifica Stella Verde",
                    "Modifica Delivery",
                    "Modifica Prenotazione Online",
                    "Modifica Tipi di Cucina",
                    "Modifica Servizi",
                    "Salva e torna indietro",
                    "Rispondi a una recensione"
            );

            int choice = IO.getInt("Seleziona un'opzione:");

            try {
                switch (choice) {
                    case 1 -> {
                        restaurant.setName(IO.getUserInput("Nuovo nome:"));
                        IO.printSuccessMessage("Nome aggiornato.");
                    }
                    case 2 -> {
                        restaurant.setDescription(IO.getUserInput("Nuova descrizione:"));
                        IO.printSuccessMessage("Descrizione aggiornata.");
                    }
                    case 3 -> {
                        restaurant.setWebsiteUrl(IO.getUserInput("Nuovo sito web:"));
                        IO.printSuccessMessage("Website aggiornato.");
                    }
                    case 4 -> {
                        restaurant.setPhone(IO.getUserInput("Nuovo numero di telefono:"));
                        IO.printSuccessMessage("Telefono aggiornato.");
                    }
                    case 5 -> {
                        Location location = IO.getLocationInput(false);
                        Validators.validateLocation(location);
                        restaurant.setLocation(location);
                        IO.printSuccessMessage("Location aggiornata.");
                    }
                    case 6 -> {
                        PriceRange range = IO.getEnumInput(PriceRange.class, "Inserisci nuova fascia di prezzo");
                        restaurant.setPriceRange(range);
                        IO.printSuccessMessage("Fascia di prezzo aggiornata.");
                    }
                    case 7 -> {
                        Award award = IO.getEnumInput(Award.class, "Nuovo premio:");
                        restaurant.setAward(award);
                        IO.printSuccessMessage("Premio aggiornato.");
                    }
                    case 8 -> {
                        restaurant.setGreenStar(IO.getBooleanInput("Ha la Stella Verde?"));
                        IO.printSuccessMessage("Stella verde aggiornata.");
                    }
                    case 9 -> {
                        restaurant.setHasDelivery(IO.getBooleanInput("Offre consegna a domicilio?"));
                        IO.printSuccessMessage("Opzione delivery aggiornata.");
                    }
                    case 10 -> {
                        restaurant.setHasOnlineBooking(IO.getBooleanInput("Offre prenotazione online?"));
                        IO.printSuccessMessage("Prenotazione online aggiornata.");
                    }
                    case 11 -> {
                        Set<CuisineType> cuisines = IO.getEnumSetInput(CuisineType.class, "Inserisci tipi di cucina:");
                        restaurant.getCuisinesTypes().clear();
                        restaurant.getCuisinesTypes().addAll(cuisines);
                        IO.printSuccessMessage("Tipi di cucina aggiornati.");
                    }
                    case 12 -> {
                        Set<String> services = IO.parseValidatedStrings("Inserisci servizi:");
                        restaurant.getServices().clear();
                        restaurant.getServices().addAll(services);
                        IO.printSuccessMessage("Servizi aggiornati.");
                    }
                    case 13 -> {
                        dataStore.getRestaurantDAO().update(restaurant);

                        if (!originalName.equals(restaurant.getName())) {
                            owner.getRestaurantsByName().remove(originalName);
                        }
                        owner.getRestaurantsByName().put(restaurant.getName(), restaurant);

                        IO.printSuccessMessage("Modifiche salvate.");
                        return;
                    }
                    case 14 -> respondToReview(restaurant);
                    default -> IO.printErrorMessage("Opzione non valida.");
                }
            } catch (IllegalArgumentException e) {
                IO.printErrorMessage(e.getMessage());
            } catch (AbortOperationException e) {
                IO.printErrorMessage("Abort: " + e.getMessage());
                break;
            }

            IO.getUserInput("Premi invio per continuare.");
        }
    }

    protected void viewOwnerLatestReviews() {
        Map<UUID, Restaurant> restaurants = owner.getRestaurantsById();

        List<Review> recent = restaurants.values().stream()
                .flatMap(r -> r.getReviews().values().stream())
                .filter(r -> r.getTimestamp() != null && r.getTimestamp().isAfter(LocalDateTime.now().minusDays(7)))
                .sorted(Comparator.comparing(Review::getTimestamp))
                .toList();

        if (recent.isEmpty()) {
            IO.printErrorMessage("Nessuna recensione negli ultimi 7 giorni.");
            IO.getUserInput("Premere 'Enter' per tornare al menu principale.");
            return;
        }

        IO.clearScreen();
        System.out.println("┌─── Recensioni ricevute negli ultimi 7 giorni ───┐\n");
        for (Review r : recent) {
            System.out.println(r);
            System.out.println("-".repeat(60));
        }
        IO.getUserInput("\nPremi invio per tornare.");
    }

    private void viewOwnedRestaurants() {
        List<Restaurant> list = new ArrayList<>(owner.getRestaurantsById().values());
        if (list.isEmpty()) {
            IO.clearScreen();
            IO.printErrorMessage("Non hai ristoranti registrati.");
            IO.getUserInput("Premi invio per tornare.");
            return;
        }

        while (true) {
            IO.clearScreen();
            IO.printMenu(
                    "\n┌───────────── Ristoranti di " + owner.getUsername() + " ─────────────┐",
                    "",
                    list.stream().map(Restaurant::getName).toArray(String[]::new)
            );
            System.out.println("│ 0 - Torna indietro\n");
            System.out.println("└───────────────────────────────────────────────────┘");

            try {
                int sel = IO.getInt("Seleziona un ristorante da visualizzare:");
                if (sel == 0) return;
                viewRestaurantDetails(list.get(sel));
            } catch (AbortOperationException e) {
                IO.printErrorMessage(e.getMessage() + ((e.getReason() != null) ? " Reason: " + e.getReason() : ""));
                return;
            }
        }
    }

    private void respondToReview(Restaurant restaurant) {
        List<Review> allReviews = new ArrayList<>(restaurant.getReviews().values());

        if (allReviews.isEmpty()) {
            IO.printErrorMessage("Nessuna recensione disponibile per questo ristorante.");
            IO.getUserInput("Premi invio per tornare.");
            return;
        }

        while (true) {
            IO.clearScreen();
            System.out.println("┌───── Recensioni ricevute ─────┐");
            for (int i = 0; i < allReviews.size(); i++) {
                Review r = allReviews.get(i);
                System.out.printf("[%d] Da: %s | Voto: %d\n", i + 1, r.getUser().getUsername(), r.getValue());
                System.out.println(r.getText());
                if (r.getReply() != null) {
                    System.out.println("✎ Risposta attuale: " + r.getReply());
                }
                System.out.println("─".repeat(50));
            }
            System.out.println("[0] Torna indietro");

            int sel = IO.getInt("Seleziona una recensione da rispondere:");
            if (sel == 0) return;
            if (sel < 1 || sel > allReviews.size()) {
                IO.printErrorMessage("Indice non valido.");
                continue;
            }

            Review selected = allReviews.get(sel - 1);
            String response = IO.getUserInput("Scrivi la tua risposta (max 300 caratteri):");
            dataStore.getReviewDAO().replyToReview(selected, response);

            IO.printSuccessMessage("Risposta salvata.");
            IO.getUserInput("Premi invio per continuare.");
        }
    }
}
