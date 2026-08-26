package it.uninsubria.laboratoriob.api.remote;

import it.uninsubria.laboratoriob.api.objects.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthServiceInter extends Remote {
    public User loginCustomer(String username, String password) throws RemoteException;
    public User loginOwner(String username, String password) throws RemoteException;
    public User register(User utente) throws RemoteException;
}
