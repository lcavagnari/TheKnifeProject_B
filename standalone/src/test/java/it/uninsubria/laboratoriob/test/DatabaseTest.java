package it.uninsubria.laboratoriob.test;

import it.uninsubria.laboratoriob.utils.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection conn = Database.getConnection()) {

            System.out.println("Connection successful!");

            System.out.println("Database: "
                    + conn.getMetaData().getDatabaseProductName());

            System.out.println("Version: "
                    + conn.getMetaData().getDatabaseProductVersion());

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {

                if (rs.next()) {
                    System.out.println("Test query result: " + rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Database connection failed:");
            e.printStackTrace();

        } finally {
            Database.shutdown();
        }
    }
}