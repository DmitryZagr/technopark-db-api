package ru.mail.park.util;


import ru.mail.park.model.Table;

import java.sql.*;

/**
 * Created by admin on 09.10.16.
 */
public class MySqlUtilRequests implements AutoCloseable{

    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;
    private static PreparedStatement preparedStatement;

    public static int countRowsInTable(String table) {
        connection =  ConnectionToMySQL.getConnection();

        String sql = "select count(*) from " + table;

        int count = 0;

        try {
            statement   = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) { count = (resultSet.getInt(1)); }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    public static ResultSet getEntityByKey(String table, String colName, String key) {
        connection =  ConnectionToMySQL.getConnection();

        String sql = "select * from " + table + " WHERE " + colName + "=?;";

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, key);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultSet;
    }


    @Override
    public void close() throws Exception {
        resultSet.close();
        statement.close();
        connection.close();
    }

}
