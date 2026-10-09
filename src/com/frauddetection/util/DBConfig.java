package com.frauddetection.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Single place holding database configuration.
 *
 * Credentials are read from a local "db.properties" file in the project root
 * (which is git-ignored), so a real password never gets committed to GitHub.
 * If the file is missing, safe placeholder defaults are used. See
 * db.properties.example for the expected keys.
 */
public final class DBConfig {

    private static final Properties PROPS = load();

    public static final String URL = PROPS.getProperty("db.url",
            "jdbc:mysql://localhost:3306/fraud_detection?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    public static final String USERNAME = PROPS.getProperty("db.user", "root");
    public static final String PASSWORD = PROPS.getProperty("db.password", "your_mysql_password");

    private static Properties load() {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (IOException e) {
            System.err.println("db.properties not found - using placeholder DB credentials. "
                    + "Copy db.properties.example to db.properties and set your MySQL password.");
        }
        return p;
    }

    private DBConfig() {
        // utility class — never instantiated
    }
}
