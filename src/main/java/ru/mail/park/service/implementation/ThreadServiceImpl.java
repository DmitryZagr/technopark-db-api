package ru.mail.park.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Table;
import ru.mail.park.model.post.VotePost;
import ru.mail.park.model.thread.*;
import ru.mail.park.model.thread.Thread;
import ru.mail.park.service.interfaces.IThreadService;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by admin on 08.10.16.
 */
@Component
@Transactional
public class ThreadServiceImpl implements IThreadService, AutoCloseable{

    @Autowired
    private DataSource dataSource;
    @Autowired
    private ForumServiceImpl forumService;
    @Autowired
    private  UserServiceImpl userService;


    @Override
    public String create(Thread thread) {

        final Connection connection = DataSourceUtils.getConnection(dataSource);


        final String sqlInsert = "INSERT INTO " + Table.Thread.TABLE_THREAD + " ( " +
                Table.Thread.COLUMN_FORUM + ',' +
                Table.Thread.COLUMN_TITLE + ',' + Table.Thread.COLUMN_IS_CLOSED+ ',' +
                Table.Thread.COLUMN_USER + ',' + Table.Thread.COLUMN_DATE + " , " +
                Table.Thread.COLUMN_MESSAGE + ',' + Table.Thread.COLUMN_SLUG+ " , " +
                Table.Thread.COLUMN_IS_DELETED + " ) " +
                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?); " ;

        try {
            try(PreparedStatement preparedStatement =
                        connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, thread.getForum());
                preparedStatement.setString(2, thread.getTitle());
                preparedStatement.setBoolean(3, thread.getisClosed());
                preparedStatement.setString(4, thread.getUser());
                preparedStatement.setString(5, thread.getDate());
                preparedStatement.setString(6, thread.getMessage());
                preparedStatement.setString(7, thread.getSlug());
                preparedStatement.setBoolean(8, thread.getisDeleted());
                preparedStatement.executeUpdate();
                connection.commit();
                try(ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    while (resultSet.next()) thread.setId(resultSet.getInt(1));
                }
            }
        } catch (MySQLIntegrityConstraintViolationException e) {
            return (new ResultJson<Thread>(
                    ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();
        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (new ResultJson<Thread>(
                ResponseStatus.ResponceCode.OK.ordinal(), thread)).getStringResult();
    }

    @Override
    public String details(Integer thread, String related) {

        if(related != null && !((related.equals("user") || related.equals("forum") ||
                related.equals("user,forum") || related.equals("forum,user"))))
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sqlSel = "SELECT "
                + Table.Thread.COLUMN_FORUM + "," + Table.Thread.COLUMN_DATE + ","
                + Table.Thread.COLUMN_ID_THREAD + "," + Table.Thread.COLUMN_IS_CLOSED + ","
                + Table.Thread.COLUMN_IS_DELETED + "," +  Table.Thread.COLUMN_MESSAGE + ","
                + Table.Thread.COLUMN_SLUG + "," + Table.Thread.COLUMN_TITLE + ","
                + Table.Thread.COLUMN_USER + "," + Table.Thread.COLUMN_LIKES + ","
                + Table.Thread.COLUMN_DISLIKES + ", "
                + "COUNT(" + Table.Post.COLUMN_ID_POST + ") AS posts FROM "
                + Table.Thread.TABLE_THREAD
                + " INNER JOIN " + Table.Post.TABLE_POST
                + " ON " + Table.Thread.COLUMN_ID_THREAD   + "=? AND "
                + Table.Post.COLUMN_THREAD + '=' + Table.Thread.COLUMN_ID_THREAD
                + " AND " + Table.Post.COLUMN_IS_DELETED + "= FALSE " ;

        ThreadDetails<Object, Object> threadDetails = new ThreadDetails<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)) {
            preparedStatement.setLong(1, thread.intValue());
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.getObject("forum") == null)
                        throw new NullPointerException();
                    threadDetails.setDate(resultSet.getString("date").replace(".0", ""));
                    threadDetails.setForum(resultSet.getString("forum"));
                    threadDetails.setId(resultSet.getInt("idThread"));
                    threadDetails.setClosed((Boolean) resultSet.getObject("isClosed"));
                    threadDetails.setisDeleted((Boolean) resultSet.getObject("isDeleted"));
                    threadDetails.setMessage(resultSet.getString("message"));
                    threadDetails.setPosts(resultSet.getInt("posts"));
                    threadDetails.setSlug(resultSet.getString("slug"));
                    threadDetails.setTitle(resultSet.getString("title"));
                    threadDetails.setUser(resultSet.getString("user"));
                    threadDetails.setLikes(resultSet.getInt("likes"));
                    threadDetails.setDislikes(resultSet.getInt("dislikes"));
                    threadDetails.setPoints();

                    if (!StringUtils.isEmpty(related) && related.contains("user")) {
                        threadDetails.setUser(userService.getUserDetail((String) threadDetails.getUser()));
                    }

                    if (!StringUtils.isEmpty(related) && related.contains("forum")) {
                        threadDetails.setForum(forumService.getForum(threadDetails.getForum().toString()));
                    }
                }
            }
        } catch (NullPointerException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.NOT_FOUND.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (new ResultJson<>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadDetails)).getStringResult();
    }

    @Override
    public String subscribeUnSub(ThreadSubscribe threadSubscribe, boolean subs) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sqlInsert = "INSERT INTO " + Table.ThreadSubscribe.TABLE_ThreadSubscribe +
                " ( " +
                Table.ThreadSubscribe.COLUMN_THREAD + ',' +
                Table.ThreadSubscribe.COLUMN_USERNAME + " ) " +
                "VALUES ( ?, ?); ";

        final String sqlDelete = "DELETE FROM " + Table.ThreadSubscribe.TABLE_ThreadSubscribe +
                " WHERE " +
                Table.ThreadSubscribe.COLUMN_THREAD + "=? AND " +
                Table.ThreadSubscribe.COLUMN_USERNAME + "=?; ";

        final String json;
        final String sql = (subs) ? sqlInsert : sqlDelete;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, threadSubscribe.getThread());
            preparedStatement.setString(2, threadSubscribe.getUser());
            preparedStatement.execute();
        } catch (MySQLIntegrityConstraintViolationException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        json = (new ResultJson<ThreadSubscribe>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadSubscribe)).getStringResult();
        return json;
    }

    @Override
    public String open(ThreadID thread) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sql = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_CLOSED +
                " =0 WHERE " + Table.Thread.COLUMN_ID_THREAD + '=' + thread.getThread();

        final String json;
        final int updRows;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sql = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_CLOSED +
                " =1 WHERE " + Table.Thread.COLUMN_ID_THREAD + '=' + thread.getThread();

        final String json;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sqlUpdTH = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_DELETED +
                " =1 WHERE " + Table.Thread.COLUMN_ID_THREAD + '=' +
                thread.getThread() + ';';
        final String sqlUpdPost = "UPDATE " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_IS_DELETED + "=1 WHERE " + Table.Post.COLUMN_THREAD + '=' +
                thread.getThread();

        final String json;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdTH)) {
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
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sqlUpdTH = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_DELETED +
                " =0 WHERE " + Table.Thread.COLUMN_ID_THREAD + '=' +
                thread.getThread() + ';';
        final String sqlUpdPost = "UPDATE " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_IS_DELETED + "=0 WHERE " + Table.Post.COLUMN_THREAD + '=' +
                thread.getThread();

        final String json;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdTH)) {
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

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final int _vote;
        final int idThread;

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = (ObjectNode) mapper.readTree(voteThread);
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
        } catch (NullPointerException e ) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        final String sqlCol = (_vote == 1) ? Table.Thread.COLUMN_LIKES : Table.Thread.COLUMN_DISLIKES;

        final String sqlUpd = "UPDATE " + Table.Thread.TABLE_THREAD + " SET " +
                sqlCol + "=? WHERE " +
                Table.Thread.COLUMN_ID_THREAD + "=?;";
        final ThreadVote threadVote = new ThreadVote();
        try {

            final String sqlSel = "SELECT "
                    + Table.Thread.COLUMN_FORUM + "," + Table.Thread.COLUMN_DATE + ","
                    + Table.Thread.COLUMN_ID_THREAD + "," + Table.Thread.COLUMN_IS_CLOSED + ","
                    + Table.Thread.COLUMN_IS_DELETED + "," +  Table.Thread.COLUMN_MESSAGE + ","
                    + Table.Thread.COLUMN_SLUG + "," + Table.Thread.COLUMN_TITLE + ","
                    + Table.Thread.COLUMN_USER + "," + Table.Thread.COLUMN_LIKES + ","
                    + Table.Thread.COLUMN_DISLIKES + ", "
                    + "COUNT(" + Table.Post.COLUMN_ID_POST + ") AS posts FROM "
                    + Table.Thread.TABLE_THREAD
                    + " INNER JOIN " + Table.Post.TABLE_POST
                    + " ON " + Table.Thread.COLUMN_ID_THREAD   + "=? AND "
                    + Table.Post.COLUMN_THREAD + '=' + Table.Thread.COLUMN_ID_THREAD
                    + " AND " + Table.Post.COLUMN_IS_DELETED + "!= FALSE " ;

            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)){
                preparedStatement.setLong(1, idThread);
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        threadVote.setDate(resultSet.getString("date"));
                        threadVote.setForum(resultSet.getString("forum"));
                        threadVote.setId(resultSet.getInt("idThread"));
                        threadVote.setClosed((Boolean) resultSet.getObject("isClosed"));
                        threadVote.setisDeleted((Boolean) resultSet.getObject("isDeleted"));
                        threadVote.setMessage(resultSet.getString("message"));
                        threadVote.setPosts(resultSet.getInt("posts"));
                        threadVote.setSlug(resultSet.getString("slug"));
                        threadVote.setTitle(resultSet.getString("title"));
                        threadVote.setUser(resultSet.getString("user"));
                        threadVote.setLikes(resultSet.getInt("likes"));
                        threadVote.setDislikes(resultSet.getInt("dislikes"));
                        if (_vote == 1) threadVote.setLikes(threadVote.getLikes() + 1);
                        else threadVote.setDislikes(threadVote.getDislikes() + 1);
                        threadVote.setPoints();
                    }
                }
            }
            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlUpd)) {
                if (_vote == 1) preparedStatement.setLong(1, threadVote.getLikes());
                else preparedStatement.setLong(1, threadVote.getDislikes());
                preparedStatement.setLong(2, threadVote.getId());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (new ResultJson<>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadVote)).getStringResult();
    }

    @Override
    public String list(String user, String forum, String since, Integer limit, String order) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String reqCondition;
        final String dateContidion;
        final String orderCondition;
        final String limitCondition;
        reqCondition = (forum != null) ? Table.Thread.COLUMN_FORUM : Table.Thread.COLUMN_USER;

        String sqlSel = "SELECT "
                + Table.Thread.COLUMN_FORUM + "," + Table.Thread.COLUMN_DATE + ","
                + Table.Thread.COLUMN_ID_THREAD + "," + Table.Thread.COLUMN_IS_CLOSED + ","
                + Table.Thread.COLUMN_IS_DELETED + "," +  Table.Thread.COLUMN_MESSAGE + ","
                + Table.Thread.COLUMN_SLUG + "," + Table.Thread.COLUMN_TITLE + ","
                + Table.Thread.COLUMN_USER + "," + Table.Thread.COLUMN_LIKES + ","
                + Table.Thread.COLUMN_DISLIKES + ","
                + "COUNT(" + Table.Post.COLUMN_THREAD + ") AS posts  FROM "
                + Table.Thread.TABLE_THREAD   ;

        dateContidion = (since != null) ?  " AND " + Table.Thread.COLUMN_DATE + ">=\'"
                + since + '\'' : " ";

        sqlSel = sqlSel + " INNER JOIN " + Table.Post.TABLE_POST +
                " ON " + reqCondition + "=? AND "
                + Table.Post.COLUMN_THREAD + '=' + Table.Thread.COLUMN_ID_THREAD +
                " AND " + Table.Post.COLUMN_IS_DELETED + "= FALSE ";
        sqlSel = sqlSel + dateContidion;

        sqlSel = sqlSel + " GROUP BY " + Table.Thread.COLUMN_ID_THREAD + ' ';

        orderCondition = (order != null) ? " ORDER BY " + Table.Thread.COLUMN_DATE +
                ' ' + order + ' ' : " ORDER BY " + Table.Thread.COLUMN_DATE + " DESC ";
        sqlSel = sqlSel + orderCondition;

        limitCondition = (limit != null) ? " LIMIT "+ limit.longValue()  : " ";
        sqlSel = sqlSel +  limitCondition;

        final ArrayList<ThreadVote> threadVotes = new ArrayList<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)) {
            if(forum != null) preparedStatement.setString(1, forum);
            else preparedStatement.setString(1, user);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    final ThreadVote tv = new ThreadVote();
                    tv.setDate(resultSet.getString("date").replace(".0", ""));
                    tv.setForum(resultSet.getString("forum"));
                    tv.setId(resultSet.getInt("idThread"));
                    tv.setClosed((Boolean) resultSet.getObject("isClosed"));
                    tv.setisDeleted((Boolean) resultSet.getObject("isDeleted"));
                    tv.setMessage(resultSet.getString("message"));
                    tv.setPosts(resultSet.getInt("posts"));
                    tv.setSlug(resultSet.getString("slug"));
                    tv.setTitle(resultSet.getString("title"));
                    tv.setUser(resultSet.getString("user"));
                    tv.setLikes(resultSet.getInt("likes"));
                    tv.setDislikes(resultSet.getInt("dislikes"));
                    tv.setPoints();
                    threadVotes.add(tv);
                }
            }
        } catch (MySQLIntegrityConstraintViolationException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.USER_EXIST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (new ResultJson<ArrayList<ThreadVote>>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadVotes)).getStringResult();
    }

    @Override
    public String update(String updJson) {

        final Connection connection = DataSourceUtils.getConnection(dataSource);


        final String sqlUpd = "UPDATE " + Table.Thread.TABLE_THREAD + " SET " +
                Table.Thread.COLUMN_MESSAGE + "=?, " + Table.Thread.COLUMN_SLUG + " =? " +
                "WHERE " + Table.Thread.COLUMN_ID_THREAD + "=?; ";

        final String message;
        final String slug;
        final int thread;

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = (ObjectNode) mapper.readTree(updJson);
            message = root.get("message").asText();
            slug    = root.get("slug").asText();
            thread  = root.get("thread").asInt();
        } catch (IOException e ) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (NullPointerException e ) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        final ThreadVote threadVote = new ThreadVote();
        try {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlUpd)) {
                preparedStatement.setString(1, message);
                preparedStatement.setString(2, slug);
                preparedStatement.setLong(3, thread);
                if (preparedStatement.executeUpdate() == 0) {
                    return ResponseStatus.getMessage(
                            ResponseStatus.ResponceCode.NOT_FOUND.ordinal(),
                            ResponseStatus.FORMAT_JSON);
                }
            }

            final String sqlSel = "SELECT "
                    + Table.Thread.COLUMN_FORUM + "," + Table.Thread.COLUMN_DATE + ","
                    + Table.Thread.COLUMN_ID_THREAD + "," + Table.Thread.COLUMN_IS_CLOSED + ","
                    + Table.Thread.COLUMN_IS_DELETED + "," +  Table.Thread.COLUMN_MESSAGE + ","
                    + Table.Thread.COLUMN_SLUG + "," + Table.Thread.COLUMN_TITLE + ","
                    + Table.Thread.COLUMN_USER + "," + Table.Thread.COLUMN_LIKES + ","
                    + Table.Thread.COLUMN_DISLIKES + ", "
                    + "COUNT(" + Table.Post.COLUMN_THREAD + ") AS posts FROM "
                    + Table.Thread.TABLE_THREAD
                    + " INNER JOIN " + Table.Post.TABLE_POST
                    + " ON " + Table.Thread.COLUMN_ID_THREAD   + "=? AND "
                    + Table.Post.COLUMN_THREAD + "=? AND "
                    + Table.Post.COLUMN_IS_DELETED + "!= FALSE;";

            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)){
                preparedStatement.setLong(1, thread);
                preparedStatement.setLong(2, thread);
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        threadVote.setDate(resultSet.getString("date"));
                        threadVote.setForum(resultSet.getString("forum"));
                        threadVote.setId(resultSet.getInt("idThread"));
                        threadVote.setClosed((Boolean) resultSet.getObject("isClosed"));
                        threadVote.setisDeleted((Boolean) resultSet.getObject("isDeleted"));
                        threadVote.setMessage(resultSet.getString("message"));
                        threadVote.setPosts(resultSet.getInt("posts"));
                        threadVote.setSlug(resultSet.getString("slug"));
                        threadVote.setTitle(resultSet.getString("title"));
                        threadVote.setUser(resultSet.getString("user"));
                        threadVote.setLikes(resultSet.getInt("likes"));
                        threadVote.setDislikes(resultSet.getInt("dislikes"));
                        threadVote.setPoints();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (new ResultJson<ThreadVote>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadVote)).getStringResult();
    }

    @Override
    public String listPosts(Integer thread, String since,
                            Integer limit, String sort, String order) {

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String sql = "Select "
                + Table.Post.COLUMN_ID_POST + "," + Table.Post.COLUMN_FORUM + ","
                + Table.Post.COLUMN_DATE + "," + Table.Post.COLUMN_IS_APPROVED + ","
                + Table.Post.COLUMN_IS_DELETED + "," + Table.Post.COLUMN_IS_EDITED + ","
                + Table.Post.COLUMN_IS_HIGHLIGHED + "," + Table.Post.COLUMN_IS_SPAM + ","
                + Table.Post.COLUMN_MESSAGE + "," + Table.Post.COLUMN_PARENT + ","
                + Table.Post.COLUMN_THREAD + "," + Table.Post.COLUMN_USER + ","
                + Table.Post.COLUMN_LIKE + "," + Table.Post.COLUMN_DISLIKE + " "
                + "FROM " + Table.Post.TABLE_POST + " WHERE "
                + Table.Post.COLUMN_THREAD + "=?";

        String sqlSince = null;

        if(since != null )
            sqlSince = ' ' +  Table.Post.COLUMN_DATE + ">=" + '\'' +since + '\'' + ' ';

        final String sqlOrder = (order == null) ? " DESC " : order;

        if(sqlSince != null)
            sql = sql + " AND " + sqlSince;

        if(sort != null) {
            if(sort.contains("flat"))
                sql = sql +  " GROUP BY " +
                        Table.Post.COLUMN_DATE + ' ' + sqlOrder;

            if(sort.contains("parent_tree")) {
                return (new ResultJson<ArrayList<VotePost>>(
                        ResponseStatus.ResponceCode.OK.ordinal(), getParentTree(thread, since,
                        limit, order))).getStringResult();
            }

            if(sort.contains("tree")) {
                return (new ResultJson<ArrayList<VotePost> >(
                        ResponseStatus.ResponceCode.OK.ordinal(), getTree(thread, since,
                        limit, order))).getStringResult();
            }
        }

        if(sort == null)
            sql = sql + " ORDER BY " + Table.Post.COLUMN_DATE + ' ' + sqlOrder;

        if(limit != null)
            sql = sql + " LIMIT " + limit.intValue() + ' ';


       final ArrayList<VotePost> detailPosts = new ArrayList<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, thread);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    final VotePost votePost = new VotePost();
                    votePost.setDate(resultSet.getString("date").replace(".0", ""));
                    votePost.setForum(resultSet.getString("forum"));
                    votePost.setid(resultSet.getInt("idPost"));
                    votePost.setApproved((Boolean) resultSet.getObject("isApproved"));
                    votePost.setDeleted((Boolean) resultSet.getObject("isDeleted"));
                    votePost.setEdited((Boolean) resultSet.getObject("isEdited"));
                    votePost.setHighlighted((Boolean) resultSet.getObject("isHighlighted"));
                    votePost.setSpam((Boolean) resultSet.getObject("isSpam"));
                    votePost.setMessage(resultSet.getString("message"));
                    votePost.setParent((Integer) resultSet.getObject("parent"));
                    votePost.setThread(resultSet.getInt("thread"));
                    votePost.setUser(resultSet.getString("user"));
                    votePost.setLikes(resultSet.getInt("like"));
                    votePost.setDislikes((resultSet.getInt("dislike")));
                    votePost.setPoints();
                    detailPosts.add(votePost);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (new ResultJson<ArrayList<VotePost>>(
                ResponseStatus.ResponceCode.OK.ordinal(), detailPosts)).getStringResult();
    }


    private ArrayList<VotePost>  getTree(Integer thread, String since,
                                                                Integer limit, String order) {

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String sqlSelect = "SELECT * "
                + " FROM " + Table.Post.TABLE_POST
                + " WHERE " + Table.Post.COLUMN_THREAD + "=? ";
        if(since != null)
            sqlSelect = sqlSelect + " AND " + Table.Post.COLUMN_DATE +
                    ">=" + '\'' + since + "' ";
        if(order == null)
            sqlSelect = sqlSelect + " ORDER BY " + Table.Post.COLUMN_ROOT + " DESC, "
                    + Table.Post.COLUMN_PATH ;
        else {
            if (order.equals("desc"))
                sqlSelect = sqlSelect + " ORDER BY " + Table.Post.COLUMN_ROOT + " DESC, "
                        + Table.Post.COLUMN_PATH ;
            if(order.equals(("asc")))
                sqlSelect = sqlSelect + " ORDER BY "
                        + Table.Post.COLUMN_PATH ;
        }

        if(limit != null)
            sqlSelect = sqlSelect + " LIMIT " + limit.intValue();

        final ArrayList<VotePost> detailPosts = new ArrayList<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSelect)) {
            preparedStatement.setInt(1, thread);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next()) {
                    final VotePost votePost = new VotePost();
                    votePost.setDate(resultSet.getString("date").replace(".0", ""));
                    votePost.setForum(resultSet.getString("forum"));
                    votePost.setid(resultSet.getInt("idPost"));
                    votePost.setApproved((Boolean) resultSet.getObject("isApproved"));
                    votePost.setDeleted((Boolean) resultSet.getObject("isDeleted"));
                    votePost.setEdited((Boolean) resultSet.getObject("isEdited"));
                    votePost.setHighlighted((Boolean) resultSet.getObject("isHighlighted"));
                    votePost.setSpam((Boolean) resultSet.getObject("isSpam"));
                    votePost.setMessage(resultSet.getString("message"));
                    votePost.setParent((Integer) resultSet.getObject("parent"));
                    votePost.setThread(resultSet.getInt("thread"));
                    votePost.setUser(resultSet.getString("user"));
                    votePost.setLikes(resultSet.getInt("like"));
                    votePost.setDislikes((resultSet.getInt("dislike")));
                    votePost.setPoints();
                    detailPosts.add(votePost);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return detailPosts;
    }

    private ArrayList<VotePost>  getParentTree(Integer thread, String since,
                                                                      Integer limit, String order) {

        final Connection connection = DataSourceUtils.getConnection(dataSource);


        String sqlSelect = "SELECT * "
                + " FROM " + Table.Post.TABLE_POST
                + " WHERE " + Table.Post.COLUMN_THREAD + "=? "
                + " AND " + Table.Post.COLUMN_PARENT + " IS NULL ";
        if(since != null)
            sqlSelect = sqlSelect + " AND " + Table.Post.COLUMN_DATE +
                    ">=" + '\'' + since + "' ";
        if(order == null)
            sqlSelect = sqlSelect + " GROUP BY " + Table.Post.COLUMN_PARENT + ", " +
                Table.Post.COLUMN_ID_POST + " DESC ";
        else
            sqlSelect = sqlSelect + " GROUP BY " + Table.Post.COLUMN_PARENT + ", " +
                    Table.Post.COLUMN_ID_POST + ' ' + order;

        if(limit != null)
            sqlSelect = sqlSelect + " LIMIT " + limit.intValue();

        final ArrayList<VotePost> responsePosts = new ArrayList();

        final ArrayList<ArrayList<VotePost>> parentPosts = new ArrayList<>();


        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlSelect)) {
                preparedStatement.setInt(1, thread);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {

                        final ArrayList<VotePost> parentPost = new ArrayList();
                        final VotePost votePost = new VotePost();
                        votePost.setDate(resultSet.getString("date").replace(".0", ""));
                        votePost.setForum(resultSet.getString("forum"));
                        votePost.setid(resultSet.getInt("idPost"));
                        votePost.setApproved((Boolean) resultSet.getObject("isApproved"));
                        votePost.setDeleted((Boolean) resultSet.getObject("isDeleted"));
                        votePost.setEdited((Boolean) resultSet.getObject("isEdited"));
                        votePost.setHighlighted((Boolean) resultSet.getObject("isHighlighted"));
                        votePost.setSpam((Boolean) resultSet.getObject("isSpam"));
                        votePost.setMessage(resultSet.getString("message"));
                        votePost.setParent((Integer) resultSet.getObject("parent"));
                        votePost.setThread(resultSet.getInt("thread"));
                        votePost.setUser(resultSet.getString("user"));
                        votePost.setLikes(resultSet.getInt("like"));
                        votePost.setDislikes((resultSet.getInt("dislike")));
                        votePost.setRoot(resultSet.getInt("root"));
                        votePost.setPoints();
                        parentPost.add(votePost);
                        parentPosts.add(parentPost);

                    }
                }
            }

            final String sqlChilds = " Select * from forum.Post "
                    + " WHERE forum.Post.path LIKE ? "
                    + " ORDER BY " + Table.Post.COLUMN_PATH;

            String like;

            for (int i = 0; i < parentPosts.size(); i++) {

                like = parentPosts.get(i).get(0).getRoot() + ".___%";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlChilds)) {
                preparedStatement.setString(1, like);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {

                        final VotePost votePost = new VotePost();
                        votePost.setDate(resultSet.getString("date").replace(".0", ""));
                        votePost.setForum(resultSet.getString("forum"));
                        votePost.setid(resultSet.getInt("idPost"));
                        votePost.setApproved((Boolean) resultSet.getObject("isApproved"));
                        votePost.setDeleted((Boolean) resultSet.getObject("isDeleted"));
                        votePost.setEdited((Boolean) resultSet.getObject("isEdited"));
                        votePost.setHighlighted((Boolean) resultSet.getObject("isHighlighted"));
                        votePost.setSpam((Boolean) resultSet.getObject("isSpam"));
                        votePost.setMessage(resultSet.getString("message"));
                        votePost.setParent((Integer) resultSet.getObject("parent"));
                        votePost.setThread(resultSet.getInt("thread"));
                        votePost.setUser(resultSet.getString("user"));
                        votePost.setLikes(resultSet.getInt("like"));
                        votePost.setDislikes((resultSet.getInt("dislike")));
                        votePost.setRoot(resultSet.getInt("root"));
                        votePost.setPoints();

                        parentPosts.get(i).add(votePost);
                    }
                }
            }

        }

            for (int i = 0; i < parentPosts.size(); i++) {
                for (int j = 0; j < parentPosts.get(i).size(); j++) {
                    responsePosts.add(parentPosts.get(i).get(j));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

            return responsePosts;
    }


    public ThreadDetails getThreadDetatils(Integer thread, String related) {

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sqlSel = "SELECT "
                + Table.Thread.COLUMN_FORUM + "," + Table.Thread.COLUMN_DATE + ","
                + Table.Thread.COLUMN_ID_THREAD + "," + Table.Thread.COLUMN_IS_CLOSED + ","
                + Table.Thread.COLUMN_IS_DELETED + "," +  Table.Thread.COLUMN_MESSAGE + ","
                + Table.Thread.COLUMN_SLUG + "," + Table.Thread.COLUMN_TITLE + ","
                + Table.Thread.COLUMN_USER + "," + Table.Thread.COLUMN_LIKES + ","
                + Table.Thread.COLUMN_DISLIKES + ", "
                + "COUNT(" + Table.Post.COLUMN_ID_POST + ") AS posts FROM "
                + Table.Thread.TABLE_THREAD
                + " INNER JOIN " + Table.Post.TABLE_POST
                + " ON " + Table.Thread.COLUMN_ID_THREAD   + "=? AND "
                + Table.Post.COLUMN_THREAD + '=' + Table.Thread.COLUMN_ID_THREAD
                + " AND " + Table.Post.COLUMN_IS_DELETED + "= FALSE " ;

        ThreadDetails<Object, Object> threadDetails = new ThreadDetails<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)) {
            preparedStatement.setLong(1, thread.intValue());
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.getObject("forum") == null)
                        throw new NullPointerException();
                    threadDetails.setDate(resultSet.getString("date").replace(".0", ""));
                    threadDetails.setForum(resultSet.getString("forum"));
                    threadDetails.setId(resultSet.getInt("idThread"));
                    threadDetails.setClosed((Boolean) resultSet.getObject("isClosed"));
                    threadDetails.setisDeleted((Boolean) resultSet.getObject("isDeleted"));
                    threadDetails.setMessage(resultSet.getString("message"));
                    threadDetails.setPosts(resultSet.getInt("posts"));
                    threadDetails.setSlug(resultSet.getString("slug"));
                    threadDetails.setTitle(resultSet.getString("title"));
                    threadDetails.setUser(resultSet.getString("user"));
                    threadDetails.setLikes(resultSet.getInt("likes"));
                    threadDetails.setDislikes(resultSet.getInt("dislikes"));
                    threadDetails.setPoints();

                    if (!StringUtils.isEmpty(related) && related.contains("user")) {
                        threadDetails.setUser(userService.getUserDetail((String) threadDetails.getUser()));
                    }

                    if (!StringUtils.isEmpty(related) && related.contains("forum")) {
                        threadDetails.setForum(forumService.getForum(threadDetails.getForum().toString()));
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return threadDetails;

    }


    @Override
    public void close() throws Exception {

    }
}