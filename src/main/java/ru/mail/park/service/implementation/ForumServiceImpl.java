package ru.mail.park.service.implementation;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
//import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.forum.ForumCreateRequest;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.forum.Forum;
import ru.mail.park.model.Table;
import ru.mail.park.model.forum.ForumDetails;
import ru.mail.park.model.post.DetailPost;
import ru.mail.park.model.thread.ThreadDetails;
//import ru.mail.park.model.user.UserDetails;
import ru.mail.park.service.interfaces.IForumService;
import ru.mail.park.util.ConnectionToMySQL;
//import sun.tools.jconsole.inspector.TableSorter;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by admin on 08.10.16.
 */
@Component
public class ForumServiceImpl implements IForumService, AutoCloseable{
//    private Connection connection;
//    private PreparedStatement preparedStatement;
//    private ResultSet resultSet;

    @Autowired
    private DataSource dataSource;

    private Forum forum;

    @Autowired
    private ThreadServiceImpl threadService;

    @Override
    public String create(Forum forum) {

//        connection =  ConnectionToMySQL.getConnection();

        final Connection connection = DataSourceUtils.getConnection(dataSource);


        String sqlInsert = "INSERT INTO " + Table.Forum.TABLE_FORUM + " ( " +
                Table.Forum.COLUMN_NAME + ',' +
                Table.Forum.COLUMN_SHORT_NAME + ',' + Table.Forum.COLUMN_USER + ')' +
                "VALUES (?, ?, ?);";

        try {
            forum.setId(ForumCreateRequest.getExistingId(forum.getName(), forum.getShort_name()));
            if(forum.getId() == -1)
                return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.UNKNOWN_ERROR.ordinal(),
                    ResponseStatus.FORMAT_JSON);

            if(forum.getId() != 0) {
                String json = (new ResultJson<Forum>(
                        ResponseStatus.ResponceCode.OK.ordinal(), forum)).getStringResult();
                return json;
            }

            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)){
                preparedStatement.setString(1, forum.getName());
                preparedStatement.setString(2, forum.getShort_name());
                preparedStatement.setString(3, forum.getUser());
                preparedStatement.executeUpdate();
                try(ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    while (resultSet.next())
                        forum.setId(resultSet.getInt(1));
                }
            }

        }
//        catch (MySQLIntegrityConstraintViolationException e) {
//            return ResponseStatus.getMessage(
//                        ResponseStatus.ResponceCode.USER_EXIST.ordinal(),
//                        ResponseStatus.FORMAT_JSON);
//        }
        catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String json = (new ResultJson<Forum>(
                ResponseStatus.ResponceCode.OK.ordinal(), forum)).getStringResult();

        return json;
    }

    @Override
    public String details(String forum, String related) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);


        String sql= "SELECT * FROM " + Table.Forum.TABLE_FORUM +
                " WHERE " + Table.Forum.COLUMN_SHORT_NAME + "=?";

        ForumDetails<Object> forumDetails = new ForumDetails<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
//            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, forum);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    forumDetails.setId(resultSet.getInt("idForum"));
                    forumDetails.setName(resultSet.getString("name"));
                    forumDetails.setShort_name(resultSet.getString("short_name"));
                    forumDetails.setUser(resultSet.getString("user"));
                    if (!StringUtils.isEmpty(related) && related.contains("user")) {
                        UserServiceImpl usi = new UserServiceImpl();
                        forumDetails.setUser(usi.getUserDetatil((String) forumDetails.getUser()));
                    }
                }
            }

        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        String json = (new ResultJson<ForumDetails<Object>>(
                ResponseStatus.ResponceCode.OK.ordinal(), forumDetails)).getStringResult();

        return json;
    }

    @Override
    public String listPosts(String forum, String since, Integer limit,
                            String order, String related) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);


//        SELECT * FROM forum.Post inner join forum.Forum on
//        forum.Post.forum = forum.Forum.short_name
//
//        inner join
//        forum.VotePost on
//        forum.Post.idPost = forum.VotePost.idPost
//
//        inner join forum.Thread on
//        forum.Post.thread = forum.Thread.idThread
//
//        inner join forum.ThreadVote  on
//        forum.ThreadVote.idThread =forum.Thread.idThread ;

        String sql = "SELECT * FROM " + Table.Post.TABLE_POST +
                " INNER JOIN " + Table.Forum.TABLE_FORUM + " ON " +
                Table.Post.COLUMN_FORUM + "=" + Table.Forum.COLUMN_SHORT_NAME +
                " AND " + Table.Forum.COLUMN_SHORT_NAME +"=? ";
        if(since != null)
            sql = sql + " AND " + Table.Post.COLUMN_DATE + ">=" + "\'" + since + "\'";

//        if(!StringUtils.isEmpty(related) && related.contains("thread")) {
//            sql = sql + " INNER JOIN " + Table.VotePost.TABLE_VOTE_POST +
//                    " ON " + Table.Post.COLUMN_ID_POST = ;
//        }

        sql = sql + " INNER  JOIN " + Table.VotePost.TABLE_VOTE_POST + " ON " +
                Table.VotePost.COLUMN_ID_POST + "=" + Table.Post.COLUMN_ID_POST;

        if(order == null)
            sql = sql + " GROUP BY " + Table.Post.COLUMN_DATE + " DESC ";
        else sql = sql + " GROUP BY " + Table.Post.COLUMN_DATE + order;

        if(limit != null)
            sql = sql + " LIMIT " + limit.intValue();


        ArrayList<DetailPost<Object, Object, Object >> posts = new ArrayList<>();
        DetailPost<Object, Object, Object > post;
        Forum forumObj;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, forum);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    post = new DetailPost<>();
                    post.setDate(resultSet.getString("date").replace(".0", ""));
                    if (related != null && related.contains("forum")) {
                        forumObj = new Forum();
                        forumObj.setId(resultSet.getInt("idForum"));
                        forumObj.setName(resultSet.getString("name"));
                        forumObj.setShort_name(resultSet.getString("short_name"));
                        forumObj.setUser(resultSet.getString("Forum.user"));
                        post.setForum(forumObj);
                    } else post.setForum(resultSet.getString("forum"));
                    post.setid(resultSet.getInt("idPost"));
                    post.setApproved((Boolean) resultSet.getObject("isApproved"));
                    post.setDeleted((Boolean) resultSet.getObject("isDeleted"));
                    post.setEdited((Boolean) resultSet.getObject("isEdited"));
                    post.setHighlighted((Boolean) resultSet.getObject("isHighlighted"));
                    post.setSpam((Boolean) resultSet.getObject("isSpam"));
                    post.setMessage(resultSet.getString("message"));
                    post.setParent((Integer) resultSet.getObject("parent"));
                    post.setThread(resultSet.getInt("thread"));
                    post.setUser(resultSet.getString("user"));
                    post.setLikes(resultSet.getInt(("like")));
                    post.setDislikes(resultSet.getInt("dislike"));
                    post.setPoints();
                    posts.add(post);
                }
            }

            UserServiceImpl usi = new UserServiceImpl();
//            ThreadServiceImpl tsi = new ThreadServiceImpl();

            for(int i = 0; i < posts.size(); i++){
                if(related != null ) {
                    if(related.contains("user"))
                        posts.get(i).setUser(usi.getUserDetatil((String) posts.get(i).getUser()));
                    if(related.contains("thread"))
                        posts.get(i).setThread(threadService.getThreadDetatils((Integer) posts.get(i).getThread(), null));
                }

            }

        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        String json = (new ResultJson<ArrayList<DetailPost<Object, Object, Object >>> (
                ResponseStatus.ResponceCode.OK.ordinal(), posts)).getStringResult();

        return json;
    }

    @Override
    public String listUsers(String forum, Integer limit, String order, Integer since_id) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String sql = "SELECT " + Table.User.COLUMN_ID_USER + ", " +
                Table.User.COLUMN_EMAIL + ", " + Table.User.COLUMN_NAME +
                "  FROM " +
                Table.Post.TABLE_POST +
                " INNER JOIN " +  Table.User.TABLE_USER + " ON " +
                Table.Post.COLUMN_FORUM + "=?" + " AND " +
                Table.User.COLUMN_EMAIL + "=" + Table.Post.COLUMN_USER;
        if(since_id != null)
            sql = sql +  " AND " + Table.User.COLUMN_ID_USER + ">=" + since_id.intValue();
        if(order == null)
            order = " DESC ";
        sql = sql + " GROUP BY " + Table.User.COLUMN_NAME + " " + order;
        if(limit != null)
            sql = sql  + " LIMIT " + limit.intValue();

        ArrayList<String> emails = new ArrayList<>();

        UserServiceImpl usi = new UserServiceImpl();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, forum);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) emails.add(resultSet.getString("email"));
            }
        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        String userDetailseList =  usi.getUserDetailsListJSON(emails);

        return userDetailseList;
    }

    @Override
    public String listThreads(String forum, String since,
                              Integer limit, String order, String related) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String sql = "SELECT * FROM " + Table.Forum.TABLE_FORUM +
                " INNER JOIN " + Table.Thread.TABLE_THREAD + " ON " +
                Table.Forum.COLUMN_SHORT_NAME + "=? AND " +
                Table.Thread.COLUMN_FORUM + "=" + Table.Forum.COLUMN_SHORT_NAME;

        if(since != null)
            sql = sql + " AND " + Table.Thread.COLUMN_DATE +">=" + "\'" + since + "\'";

        String sqlSort = " GROUP BY " + Table.Thread.COLUMN_DATE ;
        if(since == null ) sqlSort = sqlSort + " DESC";
        else sqlSort = sqlSort + " " + order;

        sql = sql + sqlSort;

        if(limit != null)
            sql = sql + " LIMIT " + limit.intValue();

        ArrayList<ThreadDetails<Object, Object>> threadDetailses = new ArrayList<>();

        ArrayList<Integer> threadId;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, forum);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                threadId = new ArrayList<>();
                while (resultSet.next()) {
                    threadId.add(resultSet.getInt("idThread"));
                }
            }

//            ThreadServiceImpl tsi = new ThreadServiceImpl();

            for(int i = 0; i < threadId.size(); i++) {
                threadDetailses.add(threadService.getThreadDetatils(threadId.get(i), related));
            }

        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        String json = (new ResultJson<ArrayList<ThreadDetails<Object, Object>>>(
                ResponseStatus.ResponceCode.OK.ordinal(), threadDetailses)).getStringResult();

        return json;
    }

    public Forum getForum(String short_name) throws SQLException {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String sql = "SELECT * FROM " + Table.Forum.TABLE_FORUM +
                " WHERE " + Table.Forum.COLUMN_SHORT_NAME + "=?";
        forum = new Forum();
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, short_name);
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        forum.setId(resultSet.getInt("idForum"));
                        forum.setName(resultSet.getString("name"));
                        forum.setShort_name(resultSet.getString("short_name"));
                        forum.setUser(resultSet.getString("user"));
                    }
                }
            }

        return forum;
    }

    @Override
    public void close() throws Exception {
//        preparedStatement.close();
//        connection.close();
    }
}
