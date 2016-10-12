package ru.mail.park.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import javafx.scene.control.Tab;
import org.springframework.stereotype.Component;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Table;
import ru.mail.park.model.thread.Thread;
import ru.mail.park.model.thread.ThreadSubscribe;
import ru.mail.park.service.interfaces.IThreadService;
import ru.mail.park.util.ConnectionToMySQL;
import sun.jvm.hotspot.opto.RootNode;

import java.io.IOException;
import java.sql.*;

/**
 * Created by admin on 08.10.16.
 */
@Component
public class ThreadServiceImpl implements IThreadService, AutoCloseable{

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private ObjectMapper mapper = new ObjectMapper();


    @Override
    public String create(Thread thread) {
        connection =  ConnectionToMySQL.getConnection();

        String sqlInsert = "INSERT INTO " + Table.Thread.TABLE_THREAD + " ( " +
                Table.Thread.COLUMN_FORUM + ',' +
                Table.Thread.COLUMN_TITLE + ',' + Table.Thread.COLUMN_IS_CLOSED+ ',' +
                Table.Thread.COLUMN_USER + ',' + Table.Thread.COLUMN_DATE + " , " +
                Table.Thread.COLUMN_MESSAGE + ',' + Table.Thread.COLUMN_SLUG+ " , " +
                Table.Thread.COLUMN_IS_DELETED + " ) " +
                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?); ";

        try {
            preparedStatement = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, thread.getForum());
            preparedStatement.setString(2, thread.getTitle());
            preparedStatement.setBoolean(3, thread.isClosed());
            preparedStatement.setString(4, thread.getUser());
            preparedStatement.setString(5, thread.getDate());
            preparedStatement.setString(6, thread.getMessage());
            preparedStatement.setString(7, thread.getSlug());
            preparedStatement.setBoolean(8, thread.isDeleted());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            while(resultSet.next())
                thread.setId(resultSet.getLong(1));
        } catch (MySQLIntegrityConstraintViolationException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.USER_EXIST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String json = (new ResultJson<Thread>(
                ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();

        return json;
    }

    @Override
    public String update(Thread thread) {
//        connection =  ConnectionToMySQL.getConnection();
//        String sql = "UPDATE " + Table.Thread.TABLE_THREAD + " SET " +
//                Table.Thread.COLUMN_MESSAGE + "=?, " + Table.Thread.COLUMN_SLUG + " =?, " +
//                "WHERE " + Table.Thread.COLUMN_ID_THREAD + "=?; " +
//                "SELECT * FROM " + Table.Thread.TABLE_THREAD + " WHERE "
//                + Table.Thread.COLUMN_ID_THREAD + "=?";
//        try {
//            preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setString(1, thread.getMessage());
//            preparedStatement.setString(2, thread.getSlug());
//            preparedStatement.setLong(3, thread.getId());
//            preparedStatement.setLong(4, thread.getId());
//            resultSet = preparedStatement.executeQuery();
//            while (resultSet.next()) {
//                thread.setDate(resultSet.getString("date"));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        String json = (new ResultJson<IdPost>(
//                ResponseStatus.ResponceCode.OK.ordinal(), rmPost)).getStringResult();
//
//        return json;
        return null;
    }

    @Override
    public String subscribeUnSub(ThreadSubscribe threadSubscribe, boolean subs) {
        connection =  ConnectionToMySQL.getConnection();

        String sqlInsert = "INSERT INTO " + Table.ThreadSubscribe.TABLE_ThreadSubscribe +
                " ( " +
                Table.ThreadSubscribe.COLUMN_THREAD + ',' +
                Table.ThreadSubscribe.COLUMN_USERNAME + " ) " +
                "VALUES ( ?, ?); ";

        String sqlDelete = "DELETE FROM " + Table.ThreadSubscribe.TABLE_ThreadSubscribe +
                " WHERE " +
                Table.ThreadSubscribe.COLUMN_THREAD + "=? AND " +
                Table.ThreadSubscribe.COLUMN_USERNAME + "=?; ";

        String json = null;
        String sql = (subs == true) ? sqlInsert : sqlDelete;

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, threadSubscribe.getThread());
            preparedStatement.setString(2, threadSubscribe.getUser());
            preparedStatement.execute();
        } catch (MySQLIntegrityConstraintViolationException e) {
            json = (new ResultJson<ThreadSubscribe>(
                    ResponseStatus.ResponceCode.OK.ordinal(), threadSubscribe)).getStringResult();
            return json;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        json = (new ResultJson<ThreadSubscribe>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadSubscribe)).getStringResult();
        return json;
    }


    @Override
    public void close() throws Exception {
        resultSet.close();
        preparedStatement.close();
        statement.close();
        connection.close();

    }
}
