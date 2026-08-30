package it.uninsubria.laboratoriob.api.remote;

import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;

/**
 * Servizio RMI per l'autenticazione degli utenti.
 * <p>
 * Fornisce operazioni di login e registrazione tramite RMI.
 * Le implementazioni concrete delegano la logica ai DAO del server.
 */
public interface AuthServiceInter extends Remote {
    /**
     * Autentica un utente tramite credenziali.
     *
     * @param username nome utente
     * @param password password in chiaro
     * @return l'utente autenticato
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    User login(String username, String password) throws RemoteException;

    /**
     * Registra un nuovo utente nel sistema.
     *
     * @param username   nome utente univoco
     * @param rawPassword password in chiaro (verrà hashata lato server)
     * @param firstName  nome
     * @param lastName   cognome
     * @param birthDate  data di nascita
     * @param location   posizione dell'utente
     * @param isOwner    {@code true} se l'utente è un proprietario di ristorante
     * @return l'utente appena registrato
     * @throws RemoteException se la comunicazione RMI fallisce
     */
    User register(String username, String rawPassword, String firstName,
                  String lastName, LocalDate birthDate, Location location,
                  boolean isOwner) throws RemoteException;
}
