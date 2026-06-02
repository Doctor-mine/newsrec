package org.newsrec.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=SmartNewsDB;encrypt=true;trustServerCertificate=true";

    private static final String USER = "doctormine";
    private static final String PASS = "Truong123@@";

    public static Connection getConnection() {

        try {

            return DriverManager.getConnection(
                    URL,
                    USER,
                    PASS
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}