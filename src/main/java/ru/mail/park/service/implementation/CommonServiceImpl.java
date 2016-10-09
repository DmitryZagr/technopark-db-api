package ru.mail.park.service.implementation;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.stereotype.Component;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Table;
import ru.mail.park.service.interfaces.ICommonService;
import ru.mail.park.util.ConnectionToMySQL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by admin on 09.10.16.
 */
@Component
public class CommonServiceImpl implements ICommonService, AutoCloseable{

    private static Connection connection;
    private static Statement  statement;
    private static ResultSet  resultSet;

    @Override
    public int clear() {
        String tranckateForum  = "TRUNCATE " + Table.Forum.TABLE_FORUM;
        String tranckatePost   = "TRUNCATE " + Table.Post.TABLE_POST ;
        String tranckateThread = "TRUNCATE " + Table.Thread.TABLE_THREAD;
        String tranckateUser   = "TRUNCATE " + Table.User.TABLE_USER;

        try {
            connection = ConnectionToMySQL.getConnection();
            connection.createStatement().execute(tranckateForum);
            connection.createStatement().execute(tranckatePost);
            connection.createStatement().execute(tranckateThread);
            connection.createStatement().execute(tranckateUser);
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
    public String status() {

        String countStrUser = "SELECT count(" + Table.User.COLUMN_ID_USER + ") from " +
                Table.User.TABLE_USER;
        String countStrThread = "SELECT count(" + Table.Thread.COLUMN_ID_THREAD+ ") from " +
                Table.Thread.TABLE_THREAD;
        String countStrForum = "SELECT count(" + Table.Forum.COLUMN_ID_FORUM + ") from " +
                Table.Forum.TABLE_FORUM;
        String countStrPost= "SELECT count(" + Table.Post.COLUMN_ID_POST + ") from " +
                Table.Post.TABLE_POST;

        int countForum, countPost, countThread, countUser;
        countForum = countPost = countThread = countUser = 0;
        int code = 0;
        String response ;

        try {
            connection = ConnectionToMySQL.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(countStrUser);
            while (resultSet.next()) countUser   = resultSet.getInt(1);

            resultSet = statement.executeQuery(countStrThread);
            while (resultSet.next()) countThread   = resultSet.getInt(1);

            resultSet = statement.executeQuery(countStrForum);
            while (resultSet.next()) countForum   = resultSet.getInt(1);

            resultSet = statement.executeQuery(countStrPost);
            while (resultSet.next()) countPost   = resultSet.getInt(1);

        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getErrorMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            return ResponseStatus.getErrorMessage(
                    ResponseStatus.ResponceCode.UNKNOWN_ERROR.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }
        return "{" +
                    "\"code\":" +  ResponseStatus.ResponceCode.OK.ordinal() + "," +
                    " \"response\": {" +
                        "\"user\":"    + countUser +  "," +
                        " \"thread\":" + countThread  +  "," +
                        " \"forum\":"  + countForum   +  "," +
                        " \"post\":"   + countPost           +
                    "}" +
                "}";
    }

    @Override
    public void close() throws Exception {
        connection.close();
        resultSet.close();
        statement.close();
    }
}
