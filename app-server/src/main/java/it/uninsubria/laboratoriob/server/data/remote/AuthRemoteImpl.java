package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;
import it.uninsubria.laboratoriob.server.utils.PasswordHasher;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AuthRemoteImpl extends UnicastRemoteObject implements AuthServiceInter {
    private final ServerDataStore store;

    public AuthRemoteImpl(ServerDataStore store) throws RemoteException {
        this.store = store;
    }

    @Override
    public User login(String username, String password) throws RemoteException {
        User cached = store.findUserByName(username);

        if (cached == null) throw new RemoteException("Error occurred during login");

        if (PasswordHasher.verify(password, cached.getPasswordSalt(), cached.getPasswordHash())) return cached;
        else throw new RemoteException("Invalid user or password");
    }

    @Override
    public User register(User utente) throws RemoteException {
        if (utente == null) throw new RemoteException("No user was provided");
        else if (store.findUserById(utente.getId()) != null) throw new RemoteException("Error occurred during registration");

        if (utente instanceof Customer) store.customerDAO().save((Customer) utente);
        else if (utente instanceof Owner) store.ownerDAO().save((Owner) utente);
        else throw new RemoteException("Unsupported user type: " + utente.getClass().getSimpleName());

        store.addUser(utente);
        return utente;
    }
}
