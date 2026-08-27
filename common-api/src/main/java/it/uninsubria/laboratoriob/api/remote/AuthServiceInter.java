package it.uninsubria.laboratoriob.api.remote;

import it.uninsubria.laboratoriob.api.objects.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthServiceInter extends Remote {
    User login(String username, String password) throws RemoteException;
    User register(User utente) throws RemoteException;
}
