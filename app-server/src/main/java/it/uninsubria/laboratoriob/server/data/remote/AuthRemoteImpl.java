package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.server.data.CustomerDAO;
import it.uninsubria.laboratoriob.server.data.OwnerDAO;
import it.uninsubria.laboratoriob.server.utils.Loader;
import it.uninsubria.laboratoriob.server.utils.PasswordHasher;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AuthRemoteImpl extends UnicastRemoteObject implements AuthServiceInter {
    private final CustomerDAO cDAO = new CustomerDAO();
    private final OwnerDAO oDAO = new OwnerDAO();

    public AuthRemoteImpl() throws RemoteException {}

    // TODO: add auth implementation via token and session context
    @Override
    public User login(String username, String password) throws RemoteException {
        User cached = Loader.findUserByName(username);

        if (cached == null) throw new RemoteException("Error occurred during login");

        if (PasswordHasher.verify(password, cached.getPasswordSalt(), cached.getPasswordHash())) return cached;
        else throw new RemoteException("Invalid user or password");
    }

    @Override
    public User register(User utente) throws RemoteException {
        if (utente == null) throw new RemoteException("No user was provided");
        else if (Loader.findUserById(utente.getId()) != null) throw new RemoteException("Error occurred during registration");


        if (utente instanceof Customer) cDAO.save((Customer) utente);
        else if (utente instanceof Owner) oDAO.save((Owner) utente);
        else throw new RemoteException("Unsupported user type: " + utente.getClass().getSimpleName());

        Loader.addUser(utente);
        return utente;
    }
}
