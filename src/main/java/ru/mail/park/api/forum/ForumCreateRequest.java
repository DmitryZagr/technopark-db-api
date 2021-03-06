package ru.mail.park.api.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.model.Table;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by admin on 08.10.16.
 */
@Service
@Transactional
public class ForumCreateRequest implements AutoCloseable{

    @Autowired
    private DataSource dataSource;

    public int getExistingId(String name, String short_name) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);


        final String sql = "select  " + Table.Forum.COLUMN_ID_FORUM +
                " from " + Table.Forum.TABLE_FORUM +
                " WHERE " + Table.Forum.COLUMN_NAME + "=? OR " + Table.Forum.COLUMN_SHORT_NAME + "=?";

        int id = 0;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, short_name);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    if (!name.equals(resultSet.getString(2)) ||
                            !short_name.equals(resultSet.getString(3)))
                        return -1;
                    id = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }


    @Override
    public void close() throws Exception {
//        resultSet.close();
//        preparedStatement.close();
//        connection.close();
    }
}
