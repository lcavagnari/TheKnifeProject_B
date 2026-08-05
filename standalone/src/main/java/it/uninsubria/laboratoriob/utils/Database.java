package it.uninsubria.laboratoriob.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;

import java.sql.Connection;
import java.sql.SQLException;

@UtilityClass
public final class Database {

    private static final String URL = "jdbc:postgresql://localhost:5432/mydb";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "test1234";

    private static final HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(URL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setMinimumIdle(2);

        ds = new HikariDataSource(config);
    }

    /**
     * Restituisce una connessione dal connection pool.
     *
     * @return connessione attiva
     * @throws SQLException se la connessione non è disponibile
     */
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    /**
     * Chiude il connection pool.
     * Da chiamare alla chiusura dell'applicazione.
     */
    public static void shutdown() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }
}