package it.uninsubria.laboratoriob.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralizza la gestione della connessione al database.
 * <p>
 */
public final class Database {

    private static final String URL = "PLACEHOLDER";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static Connection sharedConnection = null;

    private Database() {
    }

    /**
     * Restituisce una connessione al database condivisa.
     * <p>
     *
     * @return connessione attiva al database
     * @throws SQLException in caso di errore di connessione
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (sharedConnection == null || sharedConnection.isClosed()) {
            sharedConnection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
        return sharedConnection;
    }
}
