package ru.mail.park.api.forum;

import ru.mail.park.model.Table;
import ru.mail.park.util.ConnectionToMySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by admin on 08.10.16.
 */
public class ForumCreateRequest implements AutoCloseable{

    private static Connection connection;
    private static PreparedStatement preparedStatement;
    private static ResultSet resultSet;

    public static int getExistingId(String name, String short_name) {
        connection =  ConnectionToMySQL.getConnection();

        String sql = "select * from " + Table.Forum.TABLE_FORUM +
                " WHERE " + Table.Forum.COLUMN_NAME + "=? OR " + Table.Forum.COLUMN_SHORT_NAME + "=?";

        int id = 0;

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, short_name);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                if(!name.equals(resultSet.getString(2)) ||
                        !short_name.equals(resultSet.getString(3)))
                    return -1;
                id = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }


    @Override
    public void close() throws Exception {
        resultSet.close();
        preparedStatement.close();
        connection.close();
    }
}