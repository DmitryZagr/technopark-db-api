package ru.mail.park.api.post;

import ru.mail.park.model.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by admin on 09.10.16.
 */
public class PostCreateRequset {
    private static Connection connection;
    private static PreparedStatement preparedStatement;
    private static ResultSet resultSet;

    public static long getExistingId(String date, long thread, String user, String forum) {

        String sql = "select * from " + Table.Post.TABLE_POST +
                " WHERE " + Table.Post.COLUMN_DATE + "=? AND " + Table.Post.COLUMN_THREAD + "=? " +
                "AND " + Table.Post.COLUMN_USER + " =? AND " + Table.Forum.COLUMN_SHORT_NAME + "=?";

        int id = 0;

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, date);
            preparedStatement.setLong(2, thread);
            preparedStatement.setString(3, user);
            preparedStatement.setString(4, forum);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                id = resultSet.getInt("idPost");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }
}
