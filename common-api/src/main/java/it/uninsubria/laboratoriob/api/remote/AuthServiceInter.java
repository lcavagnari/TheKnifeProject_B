package it.uninsubria.laboratoriob.api.remote;

import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;

public interface AuthServiceInter extends Remote {
    User login(String username, String password) throws RemoteException;
    User register(String username, String rawPassword, String firstName,
                  String lastName, LocalDate birthDate, Location location,
                  boolean isOwner) throws RemoteException;
}
