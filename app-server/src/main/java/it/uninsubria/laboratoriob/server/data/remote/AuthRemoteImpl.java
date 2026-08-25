package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.AuthServiceInter;
import it.uninsubria.laboratoriob.server.data.CustomerDAO;
import it.uninsubria.laboratoriob.server.data.OwnerDAO;
import it.uninsubria.laboratoriob.server.data.UserDAO;
import it.uninsubria.laboratoriob.server.utils.PasswordHasher;

import java.rmi.RemoteException;
import java.util.Optional;

public class AuthRemoteImpl implements AuthServiceInter {
    private final CustomerDAO cDAO = new CustomerDAO();
    private final OwnerDAO oDAO = new OwnerDAO();

    @Override
    public User loginCustomer(String username, String password, boolean isOwner) throws RemoteException {
        Customer customer =  cDAO.findByUsername(username).orElseThrow(() -> new RemoteException("Username not found"));
        if (PasswordHasher.verify(password, customer.getPasswordSalt(), customer.getPasswordHash())){
            return customer;
        }
        else{
            throw new RemoteException("Invalid password");
        }
    }
    @Override
    public User loginOwner(String username, String password, boolean isOwner) throws RemoteException {
        Owner owner =  oDAO.findByUsername(username).orElseThrow(() -> new RemoteException("Username not found"));
        if (PasswordHasher.verify(password, owner.getPasswordSalt(), owner.getPasswordHash())){
            return owner;
        }
        else{
            throw new RemoteException("Invalid password");
        }
    }

    @Override
    public User register(User utente) throws RemoteException {
        return null;
    }
}
