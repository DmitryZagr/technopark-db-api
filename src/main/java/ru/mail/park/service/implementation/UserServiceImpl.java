package ru.mail.park.service.implementation;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.stereotype.Component;
import ru.mail.park.api.common.Result;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Table;
import ru.mail.park.model.User;
import ru.mail.park.service.interfaces.IUserService;
import ru.mail.park.util.ConnectionToMySQL;
import ru.mail.park.util.MySqlUtilRequests;

import java.sql.*;

/**
 * Created by admin on 08.10.16.
 */
@Component
public class UserServiceImpl implements IUserService, AutoCloseable{

//    private static final String tableName = "`forum`.`User`";
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    PreparedStatement preparedStatement;

    @Override
    public String create(User user) {
        connection =  ConnectionToMySQL.getConnection();

        String sqlInsert = "INSERT INTO " + Table.User.TABLE_USER + " ( " +
                Table.User.COLUMN_USERNAME + ',' +
                Table.User.COLUMN_ABOUT + ',' + Table.User.COLUMN_IS_ANONYMOUS + ',' +
                Table.User.COLUMN_NAME + ',' + Table.User.COLUMN_EMAIL + " ) " +
                "VALUES ( ?, ?, ?, ?, ?); ";

        try {
            preparedStatement = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getAbout());
            preparedStatement.setBoolean(3, user.isAnonymous());
            preparedStatement.setString(4, user.getName());
            preparedStatement.setString(5, user.getEmail());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            while(resultSet.next())
                user.setId(resultSet.getLong(1));
        } catch (MySQLIntegrityConstraintViolationException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.USER_EXIST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String json = (new ResultJson<User>(
                ResponseStatus.ResponceCode.OK.ordinal(), user)).getStringResult();

        return json;
    }

    @Override
    public void close() throws Exception {
        statement.close();
        resultSet.close();
        preparedStatement.close();
    }
}
