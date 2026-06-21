package com.example.kursovaya3;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            String url = "jdbc:mysql://localhost:3306/carrent";
            String user = "root";
            String dbPassword = "";
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, user, dbPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}