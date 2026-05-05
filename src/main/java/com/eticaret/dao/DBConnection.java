package com.eticaret.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Properties props = new Properties();
            try (InputStream in = DBConnection.class.getClassLoader()
                    .getResourceAsStream("db.properties")) {
                if (in == null) {
                    throw new IOException("db.properties bulunamadi (classpath altinda olmali)");
                }
                props.load(in);
            }
            String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            url      = props.getProperty("db.url");
            user     = props.getProperty("db.user");
            password = props.getProperty("db.password");
            Class.forName(driver);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Veritabani yapilandirmasi yuklenemedi", e);
        }
    }

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
