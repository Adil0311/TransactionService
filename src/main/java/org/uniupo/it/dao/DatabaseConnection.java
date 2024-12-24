package org.uniupo.it.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/vending_machine";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading PostgreSQL JDBC driver");
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.println("Connected to database");
            connection.setAutoCommit(true);
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.out.println("Error closing database connection");
            }
        }
    }
}
