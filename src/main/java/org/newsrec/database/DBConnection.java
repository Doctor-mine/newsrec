package org.newsrec.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBConnection {

    private static final Logger logger = LogManager.getLogger(DBConnection.class);
    private static final Properties props = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new RuntimeException("application.properties not found on classpath");
            }
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    public static Connection getConnection() {

        try {
            return DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password")
            );

        } catch (Exception e) {
            logger.error("Database connection failed", e);
            throw new RuntimeException(e);
        }
    }
}