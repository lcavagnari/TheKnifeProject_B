package it.uninsubria.laboratoriob.server.utils;

import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Loader {

    private final ServerDataStore store;

    public Loader(ServerDataStore store) {
        this.store = store;
    }

    public void initialise() {
        try {
            loadUsers();
            loadRestaurants();
            resolveOwners();
        } catch (CompletionException ex) {
            System.err.println("Errore/i durante il caricamento: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Errore/i durante il caricamento: " + ex.getMessage());
        }
    }

    private void resolveOwners() {
        for (Restaurant r : store.restaurants().findAll()) {
            if (r.getOwner() != null) {
                User u = store.users().findById(r.getOwner().getId());
                if (u instanceof Owner) {
                    r.setOwner((Owner) u);
                }
            }
        }
    }

    private void loadRestaurants() throws CompletionException {
        List<Restaurant> restaurants = CompletableFuture
                .supplyAsync(store.restaurants()::loadAllFromDb)
                .exceptionally(ex -> {
                    System.err.println("Errore caricamento restaurants: " + ex.getMessage());
                    return new ArrayList<>();
                })
                .join();

        List<CompletableFuture<List<Review>>> tasks = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            tasks.add(
                    CompletableFuture
                            .supplyAsync(() -> store.reviews().loadForRestaurant(restaurant.getId()))
                            .exceptionally(ex -> {
                                System.err.println("Errore caricamento review for restaurant #"
                                        + restaurant.getId() + ": " + ex.getMessage());
                                return new ArrayList<>();
                            })
            );
        }

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant restaurant = restaurants.get(i);
            List<Review> reviews = tasks.get(i).join();
            for (Review review : reviews) restaurant.addReview(review);
            store.restaurants().putCache(restaurant);
        }
    }

    private void loadUsers() throws CompletionException {
        CompletableFuture<List<Owner>> owners = CompletableFuture
                .supplyAsync(store.users()::loadAllOwnersFromDb)
                .exceptionally(ex -> {
                    System.err.println("Errore caricamento proprietari: " + ex.getMessage());
                    return null;
                });

        CompletableFuture<List<Customer>> customers = CompletableFuture
                .supplyAsync(store.users()::loadAllCustomersFromDb)
                .exceptionally(ex -> {
                    System.err.println("Errore caricamento clienti: " + ex.getMessage());
                    return null;
                });

        CompletableFuture.allOf(owners, customers).join();

        List<Owner> o = owners.join();
        List<Customer> c = customers.join();

        if (o != null) for (Owner owner : o) store.users().putCache(owner);
        if (c != null) for (Customer customer : c) store.users().putCache(customer);
    }

    public void updateMichelinDataset(String path) throws IOException {
        path = (path != null && !path.isBlank()) ? path : "michelin_my_maps.csv";
        Path inputPath = Paths.get(path).normalize().toRealPath();

        File dataset = inputPath.toFile();

        if (!dataset.exists() || !dataset.isFile() || !dataset.getName().endsWith(".csv")) {
            System.err.println("File or path " + path
                    + " does not exist or it is not supported by the program, please check and try again.");
            return;
        }

        System.err.println("Updating michelin data from file...");
        long timestamp = System.currentTimeMillis();
        CsvParser.parseFromDataset(inputPath, store);
        System.out.println("Update completed in " + (System.currentTimeMillis() - timestamp) + "ms");
    }
}
