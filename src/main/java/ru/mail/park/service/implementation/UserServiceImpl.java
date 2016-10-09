package ru.mail.park.service.implementation;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.stereotype.Component;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Table;
import ru.mail.park.model.User;
import ru.mail.park.service.interfaces.IUserService;
import ru.mail.park.util.ConnectionToMySQL;

import java.sql.*;

/**
 * Created by admin on 08.10.16.
 */
@Component
public class UserServiceImpl implements IUserService, AutoCloseable{

//    private static final String tableName = "`forum`.`User`";
    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;
    PreparedStatement preparedStatement;

    @Override
    public int create(User user) {
//        int userID = 0;
        connection =  ConnectionToMySQL.getConnection();

        String sql = "select count(*) from " + Table.User.TABLE_USER;

        try {
            statement   = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) { user.setId(resultSet.getInt(1) + 1); }
//            user.setId(userID + 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sqlInsert = "INSERT INTO " + Table.User.TABLE_USER + " ( " +
                Table.User.COLUMN_ID_USER + ',' + Table.User.COLUMN_USERNAME + ',' +
                Table.User.COLUMN_ABOUT + ',' + Table.User.COLUMN_IS_ANONYMOUS + ',' +
                Table.User.COLUMN_NAME + ',' + Table.User.COLUMN_EMAIL + " ) " +
                "VALUES (?, ?, ?, ?, ?, ?);";

        try {
            preparedStatement = connection.prepareStatement(sqlInsert);
            preparedStatement.setInt(1, user.getId());
            preparedStatement.setString(2, user.getUsername());
            preparedStatement.setString(3, user.getAbout());
            preparedStatement.setBoolean(4, user.isAnonymous());
            preparedStatement.setString(5, user.getName());
            preparedStatement.setString(6, user.getEmail());
            preparedStatement.execute();
        } catch (MySQLIntegrityConstraintViolationException e) {
            return ResponseStatus.ResponceCode.USER_EXIST.ordinal();
        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ResponseStatus.ResponceCode.OK.ordinal();
    }

    @Override
    public void close() throws Exception {
        statement.close();
        resultSet.close();
        preparedStatement.close();
    }
}
