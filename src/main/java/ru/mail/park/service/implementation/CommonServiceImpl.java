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

    private Connection connection;
    private Statement  statement;
    private ResultSet  resultSet;

    @Override
    public int clear() {
        String unsafeMod = "SET SQL_SAFE_UPDATES = 0;";
        String tranckateForum  = "DELETE FROM " + Table.Forum.TABLE_FORUM;
        String tranckatePost   = "DELETE FROM " + Table.Post.TABLE_POST;
        String tranckateThread = "DELETE FROM " + Table.Thread.TABLE_THREAD ;
        String tranckateUser   = "DELETE FROM " + Table.User.TABLE_USER ;
        String userFollower    = "DELETE FROM " + Table.Followers.TABLE_FOLLOWERS;
        String threadSubscribe = "DELETE FROM " + Table.ThreadSubscribe.TABLE_ThreadSubscribe;
        String threadVote      = "DELETE FROM " + Table.ThreadVote.TABLE_THREAD_VOTE;
        String votePost        = "DELETE FROM " + Table.VotePost.TABLE_VOTE_POST;
        String safeMode        = "SET SQL_SAFE_UPDATES = 1;";
        try {
            connection = ConnectionToMySQL.getConnection();
            connection.createStatement().execute(unsafeMod);
            connection.createStatement().execute(tranckateUser);
            connection.createStatement().execute(tranckateForum);
            connection.createStatement().execute(tranckateThread);
            connection.createStatement().execute(tranckatePost);
            connection.createStatement().execute(threadSubscribe);
            connection.createStatement().execute(threadVote);
            connection.createStatement().execute(votePost);
            connection.createStatement().execute(userFollower);
            connection.createStatement().execute(safeMode);

        } catch (MySQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
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
            return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            return ResponseStatus.getMessage(
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
