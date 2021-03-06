package ru.mail.park.service.implementation;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Table;
import ru.mail.park.service.interfaces.ICommonService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by admin on 09.10.16.
 */
@Component
@Transactional
public class CommonServiceImpl implements ICommonService, AutoCloseable{

    @Autowired
    private DataSource dataSource;

    @Override
    public int clear() {
        final String tranckateForum  = "TRUNCATE " + Table.Forum.TABLE_FORUM + ";";
        final String tranckatePost   = "TRUNCATE " + Table.Post.TABLE_POST + ";";
        final String tranckateThread = "TRUNCATE " + Table.Thread.TABLE_THREAD + ";";
        final String tranckateUser   = "TRUNCATE " + Table.User.TABLE_USER + ";";
        final String userFollower    = "TRUNCATE " + Table.Followers.TABLE_FOLLOWERS + ";";
        final String threadSubscribe = "TRUNCATE " + Table.ThreadSubscribe.TABLE_ThreadSubscribe + ";";

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try(Statement statement = connection.createStatement()) {
            statement.execute(tranckateUser);
            statement.execute(tranckateForum);
            statement.execute(tranckateThread);
            statement.execute(tranckatePost);
            statement.execute(threadSubscribe);
            statement.execute(userFollower);
            connection.commit();

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

        final String countStrUser = "SELECT count(" + Table.User.COLUMN_ID_USER + ") from " +
                Table.User.TABLE_USER;
        final String countStrThread = "SELECT count(" + Table.Thread.COLUMN_ID_THREAD+ ") from " +
                Table.Thread.TABLE_THREAD;
        final String countStrForum = "SELECT count(" + Table.Forum.COLUMN_ID_FORUM + ") from " +
                Table.Forum.TABLE_FORUM;
        final String countStrPost= "SELECT count(" + Table.Post.COLUMN_ID_POST + ") from " +
                Table.Post.TABLE_POST;

        int countForum;
        int countPost;
        int countThread;
        int countUser;
        countForum = countPost = countThread = countUser = 0;

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try(Statement statement = connection.createStatement()) {

            try(ResultSet resultSet = statement.executeQuery(countStrUser)) {
                while (resultSet.next()) countUser = resultSet.getInt(1);
            }

            try(ResultSet resultSet = statement.executeQuery(countStrThread)) {
                while (resultSet.next()) countThread = resultSet.getInt(1);
            }

            try(ResultSet resultSet = statement.executeQuery(countStrForum)) {
                while (resultSet.next()) countForum = resultSet.getInt(1);
            }

            try(ResultSet resultSet = statement.executeQuery(countStrPost)) {
                while (resultSet.next()) countPost = resultSet.getInt(1);
            }

            statement.close();

        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.UNKNOWN_ERROR.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        return '{' +
                    "\"code\":" +  ResponseStatus.ResponceCode.OK.ordinal() + ',' +
                    " \"response\": {" +
                        "\"user\":"    + countUser + ',' +
                        " \"thread\":" + countThread  + ',' +
                        " \"forum\":"  + countForum   + ',' +
                        " \"post\":"   + countPost           +
                '}' +
                '}';
    }

    @Override
    public void close() throws Exception {
//        connection.close();
//        resultSet.close();
//        statement.close();
    }
}
