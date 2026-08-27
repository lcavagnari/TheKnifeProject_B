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
    public User loginCustomer(String username, String password) throws RemoteException {
        User cached = Loader.findUserByName(username);
        if (cached instanceof Customer) {
            Customer customer = (Customer) cached;
            if (PasswordHasher.verify(password, customer.getPasswordSalt(), customer.getPasswordHash())) return customer;
            else throw new RemoteException("Invalid password");
        }
        Customer customer = cDAO.findByUsername(username).orElseThrow(() -> new RemoteException("Username not found"));
        if (PasswordHasher.verify(password, customer.getPasswordSalt(), customer.getPasswordHash())) return customer;
        else throw new RemoteException("Invalid password");
    }

    @Override
    public User loginOwner(String username, String password) throws RemoteException {
        User cached = Loader.findUserByName(username);
        if (cached instanceof Owner) {
            Owner owner = (Owner) cached;
            if (PasswordHasher.verify(password, owner.getPasswordSalt(), owner.getPasswordHash())) return owner;
            else throw new RemoteException("Invalid password");
        }
        Owner owner = oDAO.findByUsername(username).orElseThrow(() -> new RemoteException("Username not found"));
        if (PasswordHasher.verify(password, owner.getPasswordSalt(), owner.getPasswordHash())) return owner;
        else throw new RemoteException("Invalid password");
    }

    @Override
    public User register(User utente) throws RemoteException {
        if (utente == null) throw new RemoteException("User is null");
        if (utente instanceof Customer) {
            cDAO.save((Customer) utente);
        } else if (utente instanceof Owner) {
            oDAO.save((Owner) utente);
        } else {
            throw new RemoteException("Unsupported user type: " + utente.getClass().getSimpleName());
        }
        Loader.getAllUsersById().put(utente.getId(), utente);
        Loader.getAllUsersByName().put(utente.getUsername(), utente);
        return utente;
    }
}
