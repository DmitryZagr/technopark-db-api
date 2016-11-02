package ru.mail.park.service.implementation;

//import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.mysql.jdbc.*;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
//import javafx.scene.control.Tab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
//import org.springframework.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Table;
import ru.mail.park.model.post.DetailPost;
import ru.mail.park.model.post.VotePost;
import ru.mail.park.model.thread.*;
import ru.mail.park.model.thread.Thread;
import ru.mail.park.service.interfaces.IThreadService;
import ru.mail.park.util.ConnectionToMySQL;
//import sun.jvm.hotspot.opto.RootNode;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by admin on 08.10.16.
 */
@Component
@Transactional
public class ThreadServiceImpl implements IThreadService, AutoCloseable{

//    private Connection connection;
//    private Statement statement;
//    private ResultSet resultSet;
//    private PreparedStatement preparedStatement;
    private ObjectMapper mapper = new ObjectMapper();
    private ThreadDetails<Object, Object> threadDetails;

    @Autowired
    private DataSource dataSource;
    @Autowired
    private ForumServiceImpl forumService;
    @Autowired
    private PostServiceImpl postService;


    @Override
    public String create(Thread thread) {

//        connection =  ConnectionToMySQL.getConnection();

        final Connection connection = DataSourceUtils.getConnection(dataSource);


        String sqlInsert = "INSERT INTO " + Table.Thread.TABLE_THREAD + " ( " +
                Table.Thread.COLUMN_FORUM + ',' +
                Table.Thread.COLUMN_TITLE + ',' + Table.Thread.COLUMN_IS_CLOSED+ ',' +
                Table.Thread.COLUMN_USER + ',' + Table.Thread.COLUMN_DATE + " , " +
                Table.Thread.COLUMN_MESSAGE + ',' + Table.Thread.COLUMN_SLUG+ " , " +
                Table.Thread.COLUMN_IS_DELETED + " ) " +
                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?); " ;

        String sqlVoteTabel = "INSERT INTO " + Table.ThreadVote.TABLE_THREAD_VOTE + "( " +
                Table.ThreadVote.COLUMN_ID_THREAD + " ) " +
                "VALUES (?);" ;

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
                try(ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    while (resultSet.next()) thread.setId(resultSet.getInt(1));
                }
            }
            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlVoteTabel)) {
                preparedStatement.setLong(1, thread.getId());
                preparedStatement.execute();
            }
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
    public String details(Integer thread, String related) {

        if(related != null && !((related.equals("user") || related.equals("forum") ||
                related.equals("user,forum") || related.equals("forum,user"))))
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);

//        connection =  ConnectionToMySQL.getConnection();

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String sqlSel = "SELECT *, COUNT(" + Table.Post.COLUMN_ID_POST + ") AS posts FROM " +
                Table.Thread.TABLE_THREAD + "INNER JOIN " +
                Table.ThreadVote.TABLE_THREAD_VOTE + " ON " +
                Table.Thread.COLUMN_ID_THREAD + "=" + Table.ThreadVote.COLUMN_ID_THREAD + " AND " +
                Table.Thread.COLUMN_ID_THREAD   + "=?" +
                " INNER JOIN " + Table.Post.TABLE_POST +
                " ON " + Table.Post.COLUMN_THREAD + "=" + Table.Thread.COLUMN_ID_THREAD +
                " AND " + Table.Post.COLUMN_IS_DELETED + "= FALSE " ;

        threadDetails = new ThreadDetails<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)) {
            preparedStatement.setLong(1, thread.intValue());
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (((String) resultSet.getObject("forum")) == null)
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
                        UserServiceImpl usi = new UserServiceImpl();
                        threadDetails.setUser(usi.getUserDetatil((String) threadDetails.getUser()));
                    }

                    if (!StringUtils.isEmpty(related) && related.contains("forum")) {
//                        ForumServiceImpl fsi = new ForumServiceImpl();
                        threadDetails.setForum(forumService.getForum((String) threadDetails.getForum()));
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

        String json = (new ResultJson<>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadDetails)).getStringResult();

        return json;
    }

    @Override
    public String subscribeUnSub(ThreadSubscribe threadSubscribe, boolean subs) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

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

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
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
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String sql = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_CLOSED +
                " =0 WHERE " + Table.Thread.COLUMN_ID_THREAD + "=" + thread.getThread();

        String json;
        int updRows;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
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
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String sql = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_CLOSED +
                " =1 WHERE " + Table.Thread.COLUMN_ID_THREAD + "=" + thread.getThread();

        String json;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
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
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String sqlUpdTH = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_DELETED +
                " =1 WHERE " + Table.Thread.COLUMN_ID_THREAD + "=" +
                thread.getThread() + ";" ;
        String sqlUpdPost = "UPDATE " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_IS_DELETED + "=1 WHERE " + Table.Post.COLUMN_THREAD + "=" +
                thread.getThread();

        String json;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdTH)) {
//            preparedStatement = connection.prepareStatement(sqlUpdTH);
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
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String sqlUpdTH = "UPDATE " + Table.Thread.TABLE_THREAD +
                " SET " + Table.Thread.COLUMN_IS_DELETED +
                " =0 WHERE " + Table.Thread.COLUMN_ID_THREAD + "=" +
                thread.getThread() + ";" ;
        String sqlUpdPost = "UPDATE " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_IS_DELETED + "=0 WHERE " + Table.Post.COLUMN_THREAD + "=" +
                thread.getThread();

        String json;

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
//        connection =  ConnectionToMySQL.getConnection();

        final Connection connection = DataSourceUtils.getConnection(dataSource);


//       SELECT *, COUNT(idPost) as posts
//        FROM `forum`.`Thread`
//        INNER JOIN`forum`.`ThreadVote` ON
//        forum.Thread.idThread=forum.ThreadVote.idThread AND
//        forum.Thread.idThread = 45
//        INNER JOIN forum.Post ON
//        forum.Post.thread = forum.Thread.idThread
//        GROUP BY Thread.idThread ";


        String sqlSel = "SELECT *, COUNT(" + Table.Post.COLUMN_ID_POST + ") AS posts FROM " +
                Table.Thread.TABLE_THREAD + "INNER JOIN " +
                Table.ThreadVote.TABLE_THREAD_VOTE + " ON " +
                Table.Thread.COLUMN_ID_THREAD + "=" + Table.ThreadVote.COLUMN_ID_THREAD + " AND " +
                Table.Thread.COLUMN_ID_THREAD   + "=?" +
                " INNER JOIN " + Table.Post.TABLE_POST +
                " ON " + Table.Post.COLUMN_THREAD + "=" + Table.Thread.COLUMN_ID_THREAD +
                " AND " + Table.Post.COLUMN_IS_DELETED + "!= FALSE " ;

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
            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)){
                preparedStatement.setLong(1, idThread);
//            preparedStatement.setLong(2, idThread);
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

        String json = (new ResultJson<>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadVote)).getStringResult();

        return json;
    }

    @Override
    public String list(String user, String forum, String since, Integer limit, String order) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String reqCondition, dateContidion, orderCondition, limitCondition;
        reqCondition = (forum != null) ? Table.Thread.COLUMN_FORUM : Table.Thread.COLUMN_USER;

        String sqlSel = "SELECT *, COUNT(" + Table.Post.COLUMN_THREAD + ") AS posts  FROM " +
                Table.Thread.TABLE_THREAD + "INNER JOIN " +
                Table.ThreadVote.TABLE_THREAD_VOTE + " ON " +
                Table.ThreadVote.COLUMN_ID_THREAD + "=" + Table.Thread.COLUMN_ID_THREAD +
                " AND " + reqCondition + "=? "  ;

        dateContidion = (since != null) ?  " AND " + Table.Thread.COLUMN_DATE + ">=\'"
                + since + "\'": " ";
        sqlSel = sqlSel + dateContidion;

        sqlSel = sqlSel + " LEFT JOIN " + Table.Post.TABLE_POST +
        " ON " + Table.Post.COLUMN_THREAD + "=" + Table.Thread.COLUMN_ID_THREAD +
                " AND " + Table.Post.COLUMN_IS_DELETED + "= FALSE ";

        sqlSel = sqlSel + " GROUP BY " + Table.Thread.COLUMN_ID_THREAD + " ";

        orderCondition = (order != null) ? " ORDER BY " + Table.Thread.COLUMN_DATE +
                " " + order + " ": " ORDER BY " + Table.Thread.COLUMN_DATE + " DESC ";
        sqlSel = sqlSel + orderCondition;

        limitCondition = (limit != null) ? " LIMIT "+ limit.longValue()  : " ";
        sqlSel = sqlSel +  limitCondition;

        ArrayList<ThreadVote> threadVotes = new ArrayList<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel);) {
            if(forum != null) preparedStatement.setString(1, forum);
            else preparedStatement.setString(1, user);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    ThreadVote tv = new ThreadVote();
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

        String json = (new ResultJson<ArrayList<ThreadVote>>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadVotes)).getStringResult();

        return json;
    }

    @Override
    public String update(String updJson) {
//        connection =  ConnectionToMySQL.getConnection();

        final Connection connection = DataSourceUtils.getConnection(dataSource);


        String sql = "UPDATE " + Table.Thread.TABLE_THREAD + " SET " +
                Table.Thread.COLUMN_MESSAGE + "=?, " + Table.Thread.COLUMN_SLUG + " =? " +
                "WHERE " + Table.Thread.COLUMN_ID_THREAD + "=?; ";

        String sqlSel = "SELECT *, COUNT(" + Table.Post.COLUMN_THREAD + ") AS posts FROM " +
                Table.Thread.TABLE_THREAD + " INNER JOIN " +
                Table.ThreadVote.TABLE_THREAD_VOTE + " ON " +
                Table.ThreadVote.COLUMN_ID_THREAD   + "=? LEFT JOIN " + Table.Post.TABLE_POST +
                " ON " + Table.Post.COLUMN_THREAD + "=? AND " + Table.Post.COLUMN_IS_DELETED + "!= FALSE " ;

        String message, slug;
        int thread;

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(updJson);
            message = root.get("message").asText();
            slug    = root.get("slug").asText();
            thread  = root.get("thread").asInt();
        } catch (IOException e ) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (java.lang.NullPointerException e ) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        ThreadVote threadVote = new ThreadVote();
        try {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, message);
                preparedStatement.setString(2, slug);
                preparedStatement.setLong(3, thread);
                if (preparedStatement.executeUpdate() == 0) {
                    return ResponseStatus.getMessage(
                            ResponseStatus.ResponceCode.NOT_FOUND.ordinal(),
                            ResponseStatus.FORMAT_JSON);
                }
            }
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

        String json = (new ResultJson<ThreadVote>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadVote)).getStringResult();

        return json;
    }

    @Override
    public String listPosts(Integer thread, String since,
                            Integer limit, String sort, String order) {
//        connection =  ConnectionToMySQL.getConnection();

        final Connection connection = DataSourceUtils.getConnection(dataSource);

//        String sql = "Select * FROM " + Table.Post.TABLE_POST + " INNER JOIN " +
//                Table.VotePost.TABLE_VOTE_POST + " ON " + Table.Post.COLUMN_ID_POST +
//                "=" + Table.VotePost.COLUMN_ID_POST + " AND " +
//                Table.Post.COLUMN_THREAD + "=?";

        String sql = "Select * FROM " + Table.Post.TABLE_POST + " INNER JOIN " +
                Table.VotePost.TABLE_VOTE_POST + " ON " + Table.Post.COLUMN_ID_POST +
                "=" + Table.VotePost.COLUMN_ID_POST + " AND " +
                Table.Post.COLUMN_THREAD + "=?";

        String sqlSince = null;

        if(since != null )
            sqlSince = " " +  Table.Post.COLUMN_DATE + ">=" + "'" +since + "'" + " ";

        String sqlOrder = (order == null) ? " DESC " : order;

        if(sqlSince != null)
            sql = sql + " AND " + sqlSince;

        if(sort != null) {
            if(sort.contains("flat"))
                sql = sql +  " GROUP BY " +
                        Table.Post.COLUMN_DATE + " " + sqlOrder;

            if(sort.contains("parent_tree")) {
                return (new ResultJson<ArrayList<DetailPost<Object, Object,Object>>>(
                        ResponseStatus.ResponceCode.OK.ordinal(), getParentTree(thread, since,
                        limit, sort, order))).getStringResult();
            }

            if(sort.contains("tree")) {
                return (new ResultJson<ArrayList<DetailPost<Object, Object,Object>>>(
                        ResponseStatus.ResponceCode.OK.ordinal(), getTree(thread, since,
                        limit, sort, order))).getStringResult();
            }
        }

        if(sort == null)
            sql = sql + " GROUP BY " + Table.Post.COLUMN_DATE + " " + sqlOrder;

        if(limit != null)
            sql = sql + " LIMIT " + limit.intValue() + " ";


       ArrayList<VotePost> detailPosts = new ArrayList<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
            preparedStatement.setInt(1, thread);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    VotePost votePost = new VotePost();
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

        String json = (new ResultJson<ArrayList<VotePost>>(
                ResponseStatus.ResponceCode.OK.ordinal(), detailPosts)).getStringResult();

        return json;
    }


    private ArrayList<DetailPost<Object,Object,Object>> getTree(Integer thread, String since,
                                                                Integer limit, String sort, String order) {

//        Select * from forum.Post
//        WHERE thread=3
//        group by path

        final Connection connection = DataSourceUtils.getConnection(dataSource);


        String sqlSelect = "SELECT * " +
                " FROM " + Table.Post.TABLE_POST +
                " WHERE " + Table.Post.COLUMN_THREAD + "=? ";
        if(since != null)
            sqlSelect = sqlSelect + " AND " + Table.Post.COLUMN_DATE +
                    ">=" + "'" + since + "' ";
        if(order == null)
            sqlSelect = sqlSelect + " GROUP BY " + Table.Post.COLUMN_ROOT + " DESC , " +
                    Table.Post.COLUMN_PATH + " ASC ";
        else {
            if (order.equals("desc"))
                sqlSelect = sqlSelect + " GROUP BY " + Table.Post.COLUMN_ROOT + " DESC , " +
                        Table.Post.COLUMN_PATH + " ASC ";
            if(order.equals(("asc")))
                sqlSelect = sqlSelect + " GROUP BY " + Table.Post.COLUMN_ROOT + " " +
                        " ASC, " + Table.Post.COLUMN_PATH + " ASC ";
        }

        if(limit != null)
            sqlSelect = sqlSelect + " LIMIT " + limit.intValue();

        ArrayList<DetailPost<Object,Object,Object>> posts = new ArrayList<>();

        ArrayList<Integer> postsID = new ArrayList<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSelect)) {
            preparedStatement.setInt(1, thread);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next()) {
                    postsID.add(resultSet.getInt("idPost"));
                }
            }

//            PostServiceImpl psi = new PostServiceImpl();

            for(int i = 0; i < postsID.size(); i++ ) {
                postService.details(postsID.get(i), null);
                posts.add(postService.getVotePost());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }

    private ArrayList<DetailPost<Object,Object,Object>> getParentTree(Integer thread, String since,
                               Integer limit, String sort, String order) {

        final Connection connection = DataSourceUtils.getConnection(dataSource);


        String sqlSelect = "SELECT * " +
                " FROM " + Table.Post.TABLE_POST +
                " WHERE " + Table.Post.COLUMN_THREAD + "=? " +
                " AND " + Table.Post.COLUMN_PARENT + " IS NULL ";
        if(since != null)
            sqlSelect = sqlSelect + " AND " + Table.Post.COLUMN_DATE +
                    ">=" + "'" + since + "' ";
        if(order == null)
            sqlSelect = sqlSelect + " GROUP BY " + Table.Post.COLUMN_PARENT + ", " +
                Table.Post.COLUMN_ID_POST + " DESC ";
        else
            sqlSelect = sqlSelect + " GROUP BY " + Table.Post.COLUMN_PARENT + ", " +
                    Table.Post.COLUMN_ID_POST + " " + order;

        if(limit != null)
            sqlSelect = sqlSelect + " LIMIT " + limit.intValue();

        ArrayList<DetailPost<Object,Object,Object>> posts = new ArrayList<>();

        ArrayList<Integer> rootId = new ArrayList<>();

        try {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSelect)) {
                preparedStatement.setInt(1, thread);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    rootId.add(resultSet.getInt("idPost"));
                }
            }

//            PostServiceImpl psi = new PostServiceImpl();

            for(int i = 0; i < rootId.size(); i++ ) {
                postService.details(rootId.get(i), null);
                posts.add(postService.getVotePost());



                String childs = " Select * from forum.Post " +
                        " WHERE   parent is not null and path like concat('" +
                        rootId.get(i).intValue() + "', '.%') " +
                        " GROUP BY " + Table.Post.COLUMN_PATH;

                ArrayList<Integer> child;

                try(PreparedStatement preparedStatement = connection.prepareStatement(childs)){
                    try(ResultSet resultSet = preparedStatement.executeQuery()) {

                        child = new ArrayList<>();

                        while (resultSet.next()) {
                            child.add(resultSet.getInt("idPost"));
                        }
                    }
                }

                for(int j = 0; j < child.size(); j++ ) {
                    postService.details(child.get(j), null);
                    posts.add(postService.getVotePost());
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }



    public ThreadDetails getThreadDetatils(Integer thread, String related) {
        details(thread, related);
        return this.threadDetails;
    }


    @Override
    public void close() throws Exception {
//        resultSet.close();
//        preparedStatement.close();
//        statement.close();
//        connection.close();

    }
}