package it.uninsubria.laboratoriob.server.remote;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;
import it.uninsubria.laboratoriob.server.utils.PasswordHasher;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

public class AuthRemoteImpl extends UnicastRemoteObject implements AuthServiceInter {
    private final ServerDataStore store;

    public AuthRemoteImpl(ServerDataStore store) throws RemoteException {
        this.store = store;
    }

    @Override
    public User login(String username, String password) throws RemoteException {
        User cached = store.users().findByName(username);

        if (cached == null) throw new RemoteException("Error occurred during login");

        if (PasswordHasher.verify(password, cached.getPasswordSalt(), cached.getPasswordHash())) return cached;
        else throw new RemoteException("Invalid user or password");
    }

    @Override
    public User register(String username, String rawPassword, String firstName,
                         String lastName, LocalDate birthDate, Location location,
                         boolean isOwner) throws RemoteException {
        // 1. Validate input
        if (rawPassword == null || rawPassword.isEmpty()) throw new RemoteException("Password cannot be empty");
        if (store.users().hasByName(username)) throw new RemoteException("Username already exists");


        // 2. Generate salt and hash (SERVER SIDE)
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(rawPassword, salt);

        // 3. Build the correct User object
        UUID id = UUID.randomUUID();

        User newUser;
        if (isOwner) {
            newUser = new Owner(id, username, hash, salt, firstName, lastName,
                    location, birthDate, new HashSet<>(), false);

            store.users().saveOwner((Owner) newUser);
        } else {
            newUser = new Customer(id, username, hash, salt, firstName, lastName,
                    location, birthDate, new HashSet<>(), false);

            store.users().saveCustomer((Customer) newUser);
        }

        return newUser;
    }
}
