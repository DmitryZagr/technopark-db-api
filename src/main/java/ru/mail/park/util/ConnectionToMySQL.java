package ru.mail.park.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by admin on 09.10.16.
 */
public class ConnectionToMySQL implements AutoCloseable {
    private static Connection connection;

    public static Connection getConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/forum", "zagrebaev", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
