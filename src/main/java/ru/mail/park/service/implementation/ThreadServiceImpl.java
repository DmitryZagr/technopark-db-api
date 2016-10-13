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
import ru.mail.park.model.thread.ThreadID;
import ru.mail.park.model.thread.ThreadSubscribe;
import ru.mail.park.model.thread.ThreadVote;
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
            String json = (new ResultJson<Thread>(
                    ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();
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
    public String open(ThreadID thread) {
        connection =  ConnectionToMySQL.getConnection();
        String sql = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_CLOSED +
                " =0 WHERE " + Table.Thread.COLUMN_ID_THREAD + "=" + thread.getThread();

        String json;
        int updRows;

        try {
            preparedStatement = connection.prepareStatement(sql);
            updRows = preparedStatement.executeUpdate();
            if(updRows == 0)
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
        } catch (MySQLIntegrityConstraintViolationException e) {
            json = (new ResultJson<ThreadID>(
                    ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();
            return json;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        json = (new ResultJson<ThreadID>(
                ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();
        return json;
    }

    @Override
    public String close(ThreadID thread) {
        connection =  ConnectionToMySQL.getConnection();
        String sql = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_CLOSED +
                " =1 WHERE " + Table.Thread.COLUMN_ID_THREAD + "=" + thread.getThread();

        String json;

        try {
            preparedStatement = connection.prepareStatement(sql);
            if(preparedStatement.executeUpdate() == 0)
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
        } catch (MySQLIntegrityConstraintViolationException e) {
            json = (new ResultJson<ThreadID>(
                    ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();
            return json;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        json = (new ResultJson<ThreadID>(
                ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();
        return json;
    }

    @Override
    public String remove(ThreadID thread) {
        connection =  ConnectionToMySQL.getConnection();
        String sqlUpdTH = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_DELETED +
                " =1 WHERE " + Table.Thread.COLUMN_ID_THREAD + "=" +
                thread.getThread() + ";" ;
        String sqlUpdPost = "UPDATE " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_IS_DELETED + "=1 WHERE " + Table.Post.COLUMN_THREAD + "=" +
                thread.getThread();

        String json;

        try {
            preparedStatement = connection.prepareStatement(sqlUpdTH);
            if(preparedStatement.executeUpdate() == 0)
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
            preparedStatement.executeUpdate(sqlUpdPost);
        } catch (MySQLIntegrityConstraintViolationException e) {
            json = (new ResultJson<ThreadID>(
                    ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();
            return json;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        json = (new ResultJson<ThreadID>(
                ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();
        return json;
    }

    @Override
    public String restore(ThreadID thread) {
        connection =  ConnectionToMySQL.getConnection();
        String sqlUpdTH = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_DELETED +
                " =0 WHERE " + Table.Thread.COLUMN_ID_THREAD + "=" +
                thread.getThread() + ";" ;
        String sqlUpdPost = "UPDATE " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_IS_DELETED + "=0 WHERE " + Table.Post.COLUMN_THREAD + "=" +
                thread.getThread();

        String json;

        try {
            preparedStatement = connection.prepareStatement(sqlUpdTH);
            if(preparedStatement.executeUpdate() == 0)
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
            preparedStatement.executeUpdate(sqlUpdPost);
        } catch (MySQLIntegrityConstraintViolationException e) {
            json = (new ResultJson<ThreadID>(
                    ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();
            return json;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        json = (new ResultJson<ThreadID>(
                ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();
        return json;
    }

    @Override
    public String vote(String voteThread) {
        connection =  ConnectionToMySQL.getConnection();

        String sqlSel = "SELECT *, COUNT(" + Table.Post.COLUMN_THREAD + ") AS posts FROM " +
                Table.Thread.TABLE_THREAD + "INNER JOIN " +
                Table.ThreadVote.TABLE_THREAD_VOTE + " ON " +
                Table.ThreadVote.COLUMN_ID_THREAD   + "=? INNER JOIN " + Table.Post.TABLE_POST +
                " ON " + Table.Post.COLUMN_THREAD + "=? AND " + Table.Post.COLUMN_IS_DELETED + "!= FALSE " ;

        int _vote = 0;
        int idThread = 0;

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(voteThread);
            _vote = root.get("vote").asInt();
            idThread = root.get("thread").asInt();
            if((_vote > 1 || _vote < -1) || _vote == 0 )
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
        } catch (IOException e ) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (java.lang.NullPointerException e ) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        String sqlCol = (_vote == 1) ? Table.ThreadVote.COLUMN_LIKES : Table.ThreadVote.COLUMN_DISLIKES;

        String sqlUpd = "UPDATE " + Table.ThreadVote.TABLE_THREAD_VOTE + " SET " +
                sqlCol + "=? WHERE " +
                Table.ThreadVote.COLUMN_ID_THREAD + "=?;";
        ThreadVote threadVote = new ThreadVote();
        try {
            preparedStatement = connection.prepareStatement(sqlSel);
            preparedStatement.setLong(1, idThread);
            preparedStatement.setLong(2, idThread);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                threadVote.setDate(resultSet.getString("date"));
                threadVote.setForum(resultSet.getString("forum"));
                threadVote.setId(resultSet.getLong("idThread"));
                threadVote.setClosed(resultSet.getBoolean("isClosed"));
                threadVote.setDeleted(resultSet.getBoolean("isDeleted"));
                threadVote.setMessage(resultSet.getString("message"));
                threadVote.setPosts(resultSet.getLong("posts"));
                threadVote.setSlug(resultSet.getString("slug"));
                threadVote.setTitle(resultSet.getString("title"));
                threadVote.setUser(resultSet.getString("user"));
                threadVote.setLikes(resultSet.getLong("likes"));
                threadVote.setDislikes(resultSet.getLong("dislikes"));
                if(_vote == 1) threadVote.setLikes(threadVote.getLikes() + 1);
                else threadVote.setDislikes(threadVote.getDislikes() + 1);
                threadVote.setPoints();
            }
            preparedStatement = connection.prepareStatement(sqlUpd);
            if(_vote == 1) preparedStatement.setLong(1, threadVote.getLikes());
            else preparedStatement.setLong(1, threadVote.getDislikes());
            preparedStatement.setLong(2, threadVote.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String json = (new ResultJson<>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadVote)).getStringResult();

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
