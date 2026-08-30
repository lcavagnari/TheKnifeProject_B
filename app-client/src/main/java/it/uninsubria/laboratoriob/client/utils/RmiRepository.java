package it.uninsubria.laboratoriob.client.utils;

import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.api.remote.FavouriteServiceInter;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import lombok.experimental.UtilityClass;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Repository centralizzato per la gestione delle connessioni RMI ai servizi del server.
 * Fornisce lookup opportunistici con cooldown e limite di tentativi falliti,
 * oltre a una riconnessione forzata tramite {@link #acquireAll()}.
 */
@UtilityClass
public class RmiRepository {

    private static final long COOLDOWN_MS = 500;
    private static final int MAX_FAILED_ATTEMPTS = 2;

    private final AtomicReference<RestaurantServiceInter> restaurantService = new AtomicReference<>();
    private final AtomicReference<AuthServiceInter> authService = new AtomicReference<>();
    private final AtomicReference<ReviewServiceInter> reviewService = new AtomicReference<>();
    private final AtomicReference<FavouriteServiceInter> favouriteService = new AtomicReference<>();

    private final AtomicLong restaurantLastAttempt = new AtomicLong();
    private final AtomicLong authLastAttempt = new AtomicLong();
    private final AtomicLong reviewLastAttempt = new AtomicLong();
    private final AtomicLong favouriteLastAttempt = new AtomicLong();

    private final AtomicInteger restaurantFailedAttempts = new AtomicInteger();
    private final AtomicInteger authFailedAttempts = new AtomicInteger();
    private final AtomicInteger reviewFailedAttempts = new AtomicInteger();
    private final AtomicInteger favouriteFailedAttempts = new AtomicInteger();

    private volatile String hostname;
    private volatile int port;

    /**
     * Configura l'hostname e la porta del registro RMI.
     *
     * @param hostname indirizzo del server RMI
     * @param port     porta del registro RMI
     */
    public void configure(String hostname, int port) {
        RmiRepository.hostname = hostname;
        RmiRepository.port = port;
    }

    /**
     * Resetta tutti i riferimenti ai servizi e i contatori di tentativi.
     * Utile dopo una riconnessione forzata o un cambio di configurazione.
     */
    public void reset() {
        restaurantService.set(null);
        authService.set(null);
        reviewService.set(null);
        favouriteService.set(null);

        restaurantLastAttempt.set(0);
        authLastAttempt.set(0);
        reviewLastAttempt.set(0);
        favouriteLastAttempt.set(0);

        restaurantFailedAttempts.set(0);
        authFailedAttempts.set(0);
        reviewFailedAttempts.set(0);
        favouriteFailedAttempts.set(0);
    }

    /**
     * Restituisce il riferimento corrente al servizio dei ristoranti.
     *
     * @return stub del servizio ristoranti, o {@code null} se non ancora acquisito
     */
    public RestaurantServiceInter getRestaurantService() {
        return restaurantService.get();
    }

    /**
     * Restituisce il riferimento corrente al servizio di autenticazione.
     *
     * @return stub del servizio auth, o {@code null} se non ancora acquisito
     */
    public AuthServiceInter getAuthService() {
        return authService.get();
    }

    /**
     * Restituisce il riferimento corrente al servizio delle recensioni.
     *
     * @return stub del servizio review, o {@code null} se non ancora acquisito
     */
    public ReviewServiceInter getReviewService() {
        return reviewService.get();
    }

    /**
     * Restituisce il riferimento corrente al servizio dei preferiti.
     *
     * @return stub del servizio favourite, o {@code null} se non ancora acquisito
     */
    public FavouriteServiceInter getFavouriteService() {
        return favouriteService.get();
    }

    /**
     * Lookup opportunistico del servizio dei ristoranti.
     * Rispetta il cooldown e il limite di tentativi falliti.
     *
     * @return stub del servizio, o {@code null} se il lookup non è possibile
     */
    public RestaurantServiceInter lookupRestaurantService() {
        return lookup("restaurant", restaurantService, restaurantLastAttempt, restaurantFailedAttempts);
    }

    /**
     * Lookup opportunistico del servizio di autenticazione.
     * Rispetta il cooldown e il limite di tentativi falliti.
     *
     * @return stub del servizio, o {@code null} se il lookup non è possibile
     */
    public AuthServiceInter lookupAuthService() {
        return lookup("auth", authService, authLastAttempt, authFailedAttempts);
    }

    /**
     * Lookup opportunistico del servizio delle recensioni.
     * Rispetta il cooldown e il limite di tentativi falliti.
     *
     * @return stub del servizio, o {@code null} se il lookup non è possibile
     */
    public ReviewServiceInter lookupReviewService() {
        return lookup("review", reviewService, reviewLastAttempt, reviewFailedAttempts);
    }

    /**
     * Lookup opportunistico del servizio dei preferiti.
     * Rispetta il cooldown e il limite di tentativi falliti.
     *
     * @return stub del servizio, o {@code null} se il lookup non è possibile
     */
    public FavouriteServiceInter lookupFavouriteService() {
        return lookup("favourite", favouriteService, favouriteLastAttempt, favouriteFailedAttempts);
    }

    /**
     * Explicit, forced reconnect attempt: bypasses the cooldown and the give-up cap
     * (unlike {@link #lookup}), and resets the failure counters so opportunistic
     * per-call lookups get fresh attempts again afterward.
     *
     * @return insieme dei nomi dei servizi acquisiti con successo
     */
    @SuppressWarnings("unchecked")
    public Set<String> acquireAll() {
        String h = hostname;
        int p = port;
        if (h == null) return Set.of();

        record Slot(AtomicReference<?> ref, AtomicLong lastAttempt, AtomicInteger failedAttempts) {
        }

        Map<String, Slot> services = Map.of(
                "restaurant", new Slot(restaurantService, restaurantLastAttempt, restaurantFailedAttempts),
                "auth", new Slot(authService, authLastAttempt, authFailedAttempts),
                "review", new Slot(reviewService, reviewLastAttempt, reviewFailedAttempts),
                "favourite", new Slot(favouriteService, favouriteLastAttempt, favouriteFailedAttempts)
        );

        try {
            Registry registry = LocateRegistry.getRegistry(h, p);

            return services.entrySet().stream()
                    .filter(e -> {
                        Slot slot = e.getValue();
                        slot.lastAttempt().set(System.currentTimeMillis());
                        try {
                            Object stub = registry.lookup(e.getKey());
                            ((AtomicReference<Object>) slot.ref()).set(stub);
                            slot.failedAttempts().set(0);
                            return true;
                        } catch (Exception ex) {
                            System.err.println("RMI lookup failed for '" + e.getKey() + "': " + ex.getMessage());
                            slot.failedAttempts().incrementAndGet();
                            return false;
                        }
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (Exception e) {
            System.err.println("RMI registry unreachable: " + e.getMessage());
            return Set.of();
        }
    }

    /**
     * Opportunistic lookup used by DAOs via {@code ensureService()}. Gated so a
     * downed server doesn't get hammered: at most one attempt per {@link #COOLDOWN_MS},
     * and after {@link #MAX_FAILED_ATTEMPTS} consecutive failures this returns
     * {@code null} immediately with no registry contact until {@link #reset()} or
     * {@link #acquireAll()} re-arms it.
     */
    @SuppressWarnings("unchecked")
    private <T> T lookup(String name, AtomicReference<T> ref, AtomicLong lastAttempt, AtomicInteger failedAttempts) {
        String h = hostname;
        int p = port;
        if (h == null) return null;

        if (failedAttempts.get() >= MAX_FAILED_ATTEMPTS) return null;

        long now = System.currentTimeMillis();
        long prev = lastAttempt.get();
        if (now - prev < COOLDOWN_MS) return null;
        if (!lastAttempt.compareAndSet(prev, now)) return null;

        try {
            Registry registry = LocateRegistry.getRegistry(h, p);
            T stub = (T) registry.lookup(name);
            ref.set(stub);
            failedAttempts.set(0);
            return stub;
        } catch (Exception e) {
            int failures = failedAttempts.incrementAndGet();
            System.err.println("RMI lookup failed for '" + name + "': " + e.getMessage());
            if (failures >= MAX_FAILED_ATTEMPTS)
                System.err.println("RMI lookup for '" + name + "' failed " + failures + " times in a row, giving up until reconnect.");
            return null;
        }
    }
}
