package ru.mail.park.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Table;
import ru.mail.park.model.post.VotePost;
import ru.mail.park.model.user.User;
import ru.mail.park.model.user.UserDetails;
import ru.mail.park.service.interfaces.IUserService;
import ru.mail.park.util.ConnectionToMySQL;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by admin on 08.10.16.
 */
@Component
@Transactional
public class UserServiceImpl implements IUserService, AutoCloseable {


    @Autowired
    private DataSource dataSource;

    private ObjectMapper mapper = new ObjectMapper();

    private ArrayList<UserDetails> usersDetail;
    public UserDetails userDetails;

//    public ArrayList<UserDetails> getUsersDetail() {
//        return usersDetail;
//    }

//    private Connection connection;
//    private ResultSet resultSet;
//    private PreparedStatement preparedStatement;

    @Override
    public String create(User user) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

//        if(user.isEmpty())
//            return ResponseStatus.getMessage(
//                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);

        String sqlInsert = "INSERT INTO " + Table.User.TABLE_USER + " ( " +
                Table.User.COLUMN_USERNAME + ',' +
                Table.User.COLUMN_ABOUT + ',' + Table.User.COLUMN_IS_ANONYMOUS + ',' +
                Table.User.COLUMN_NAME + ',' + Table.User.COLUMN_EMAIL + " ) " +
                "VALUES ( ?, ?, ?, ?, ?); ";

        try(PreparedStatement preparedStatement =
                    connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getAbout());
            preparedStatement.setBoolean(3, user.getisAnonymous());
            preparedStatement.setString(4, user.getName());
            preparedStatement.setString(5, user.getEmail());
            preparedStatement.executeUpdate();
            try(ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                while (resultSet.next())
                    user.setId(resultSet.getLong(1));
            }
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
    public String follow(String followerFollowee) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String follower, followee;

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(followerFollowee);
            follower = root.get("follower").asText();
            followee = root.get("followee").asText();
            if(followee.equals(follower))
                throw new IOException();
        } catch (NullPointerException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }
        catch (IOException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        String sql = "INSERT INTO " + Table.Followers.TABLE_FOLLOWERS +
                " (" + Table.Followers.COLUMN_FOLLOWER + ", " + Table.Followers.COLUMN_FOLLOWEE +
                ") VALUES (?, ?);" ;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, follower);
            preparedStatement.setString(2, followee);
            preparedStatement.execute();
        } catch (MySQLIntegrityConstraintViolationException e) {

        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details(follower);
    }

    @Override
    public String listFollowers(String user, Integer limit, String order, Integer sinceId) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String sqlSelectFollowers = "SELECT * "   +
                "  FROM " + Table.Followers.TABLE_FOLLOWERS +
                " INNER JOIN " + Table.User.TABLE_USER +
                " ON " + Table.User.COLUMN_EMAIL + "=" + Table.Followers.COLUMN_FOLLOWER +
                " AND " + Table.Followers.COLUMN_FOLLOWEE + "=?";

        if(sinceId != null)
        sqlSelectFollowers =  sqlSelectFollowers +
               " AND  " + Table.User.COLUMN_ID_USER + ">=" + sinceId.intValue() + " ";
//
        if(order == null) order = "DESC";
        sqlSelectFollowers = sqlSelectFollowers + " ORDER BY " + Table.User.COLUMN_NAME + " " + order;
//
        if(limit != null)
        sqlSelectFollowers = sqlSelectFollowers +  " LIMIT " + limit.intValue();

        ArrayList<String> followers = new ArrayList<>();
        usersDetail = null;
        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectFollowers)) {
            preparedStatement.setString(1, user);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                usersDetail = new ArrayList<>();
                while (resultSet.next()) {
                    followers.add(resultSet.getString("follower"));
                }
            }
        } catch (MySQLSyntaxErrorException e) {
            usersDetail = null;
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            usersDetail = null;
            e.printStackTrace();
        }

        for(int i = 0; i < followers.size(); i++) {
            details(followers.get(i));
        }

        String json = (new ResultJson<ArrayList<UserDetails>>(
                ResponseStatus.ResponceCode.OK.ordinal(), usersDetail)).getStringResult();
        usersDetail = null;
        return json;
    }

    @Override
    public String listFollowing(String user, Integer limit, String order, Integer sinceId) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);


        String sqlSelectFollowing = "SELECT * "   +
                "  FROM " + Table.Followers.TABLE_FOLLOWERS +
                " INNER JOIN " + Table.User.TABLE_USER +
                " ON " + Table.User.COLUMN_EMAIL + "=" + Table.Followers.COLUMN_FOLLOWER +
                " AND " + Table.Followers.COLUMN_FOLLOWER + "=?";

        if(sinceId != null)
            sqlSelectFollowing =  sqlSelectFollowing +
                    " AND  " + Table.User.COLUMN_ID_USER + ">=" + sinceId.intValue() + " ";
//
        if(order == null) order = "DESC";
        sqlSelectFollowing = sqlSelectFollowing + " ORDER BY " + Table.User.COLUMN_NAME + " " + order;
//
        if(limit != null)
            sqlSelectFollowing = sqlSelectFollowing +  " LIMIT " + limit.intValue();

        ArrayList<String> followers = new ArrayList<>();
        usersDetail = null;
        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectFollowing)) {
            preparedStatement.setString(1, user);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                usersDetail = new ArrayList<>();
                while (resultSet.next()) {
                    followers.add(resultSet.getString("followee"));
                }
            }
        } catch (MySQLSyntaxErrorException e) {
            usersDetail = null;
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            usersDetail = null;
            e.printStackTrace();
        }

        for(int i = 0; i < followers.size(); i++) {
            details(followers.get(i));
        }

        String json = (new ResultJson<ArrayList<UserDetails>>(
                ResponseStatus.ResponceCode.OK.ordinal(), usersDetail)).getStringResult();
        usersDetail = null;
        return json;
    }

    public String getUserDetailsListJSON(ArrayList<String> emails) {
        usersDetail = new ArrayList<>();
        for(int i =0; i < emails.size(); i++)
            details(emails.get(i));
        String json = (new ResultJson<ArrayList<UserDetails>>(
                ResponseStatus.ResponceCode.OK.ordinal(), usersDetail)).getStringResult();
        usersDetail = null;
        return json;
    }

    @Override
    public String listPosts(String user, String since, Integer limit, String order) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String  dateContidion, orderCondition, limitCondition;

        String sqlSel = "SELECT * FROM " + Table.Post.TABLE_POST + "INNER JOIN " +
                Table.VotePost.TABLE_VOTE_POST + " ON " +
                Table.VotePost.COLUMN_ID_POST + "=" + Table.Post.COLUMN_ID_POST +
                " AND " + Table.Post.COLUMN_USER + "=? ";
        dateContidion = (since != null) ?  " AND " + Table.Post.COLUMN_DATE + ">=\'" + since + "\'": " ";
        sqlSel = sqlSel + dateContidion;

        orderCondition = (order != null) ? " ORDER BY " + Table.Post.COLUMN_DATE +
                " " + order + " ": " ORDER BY " + Table.Post.COLUMN_DATE + " DESC ";
        sqlSel = sqlSel + orderCondition;

        limitCondition = (limit != null) ? " LIMIT "+ limit.longValue()  : " ";
        sqlSel = sqlSel +  limitCondition;

        ArrayList<VotePost> votePosts = new ArrayList<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)) {
            preparedStatement.setString(1, user);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next()) {
                    VotePost vp = new VotePost();
                    vp.setDate(resultSet.getString("date").replace(".0", ""));
                    vp.setForum(resultSet.getString("forum"));
                    vp.setid(resultSet.getInt("idPost"));
                    vp.setApproved(resultSet.getBoolean("isApproved"));
                    vp.setDeleted(resultSet.getBoolean("isDeleted"));
                    vp.setEdited(resultSet.getBoolean("isEdited"));
                    vp.setHighlighted(resultSet.getBoolean("isHighlighted"));
                    vp.setSpam(resultSet.getBoolean("isSpam"));
                    vp.setMessage(resultSet.getString("message"));
                    vp.setParent((Integer) resultSet.getObject("parent"));
                    vp.setThread((Integer) resultSet.getObject("thread"));
                    vp.setUser(resultSet.getString("user"));
                    vp.setLikes(resultSet.getInt("like"));
                    vp.setDislikes(resultSet.getInt("dislike"));
                    vp.setPoints();
                    votePosts.add(vp);
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

        String json = (new ResultJson<ArrayList<VotePost>>(
                ResponseStatus.ResponceCode.OK.ordinal(), votePosts)).getStringResult();

        return json;
    }

    @Override
    public String unFollow(String followerFollowee) {
//        connection =  ConnectionToMySQL.getConnection();

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String follower, followee;

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(followerFollowee);
            follower = root.get("follower").asText();
            followee = root.get("followee").asText();
            if(followee.equals(follower))
                throw new IOException();
        } catch (NullPointerException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }
        catch (IOException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        String sqlUnFollow = "DELETE FROM " + Table.Followers.TABLE_FOLLOWERS + " WHERE " +
                Table.Followers.COLUMN_FOLLOWER + "=? AND " + Table.Followers.COLUMN_FOLLOWEE + "=?;";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlUnFollow);) {
            preparedStatement.setString(1, follower);
            preparedStatement.setString(2, followee);
            if(preparedStatement.executeUpdate() == 0)
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.NOT_FOUND.ordinal(),
                        ResponseStatus.FORMAT_JSON);
        } catch (MySQLIntegrityConstraintViolationException e) {

        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details(follower);
    }

    @Override
    public String details(String email) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);


//        email = MyJsonUtils.replaceOneQuoteTwoQuotes(email);

        String sqlSelectFollowers = "SELECT * FROM " + Table.User.TABLE_USER + " " +
                "INNER JOIN " + Table.Followers.TABLE_FOLLOWERS + " ON " +
                Table.User.COLUMN_EMAIL + "=" + Table.Followers.COLUMN_FOLLOWEE + " AND " +
                Table.Followers.COLUMN_FOLLOWEE + "=?";

        String sqlSelectFollowing = "SELECT * FROM " + Table.User.TABLE_USER + " " +
                "INNER JOIN " + Table.Followers.TABLE_FOLLOWERS + " ON " +
                Table.User.COLUMN_EMAIL + "=" + Table.Followers.COLUMN_FOLLOWER + " AND " +
                Table.Followers.COLUMN_FOLLOWER + "=?";

        String sqlSelectSubscriptions = "SELECT * FROM " + Table.User.TABLE_USER + " " +
                "INNER JOIN " + Table.ThreadSubscribe.TABLE_ThreadSubscribe + " ON " +
                Table.User.COLUMN_EMAIL + "=" + Table.ThreadSubscribe.COLUMN_USERNAME + " AND " +
                Table.ThreadSubscribe.COLUMN_USERNAME + "=?";

        String sqlSelectUser= "SELECT * FROM " + Table.User.TABLE_USER + " " +
                " WHERE " + Table.User.COLUMN_EMAIL + "=?" ;

        userDetails = new UserDetails();

        userDetails.setEmail(email);

        try {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectUser)) {
                preparedStatement.setString(1, email);
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while (resultSet.next()) {
                        userDetails.setId(resultSet.getLong("idUser"));

//                if(resultSet.getString("username").equals("null"))  userDetails.setUsername(null);
//                else
                        userDetails.setUsername(resultSet.getString("username"));

//                if(resultSet.getString("about").equals("null")) userDetails.setAbout(null);
//                else
                        userDetails.setAbout(resultSet.getString("about"));

//                if(resultSet.getString("name").equals("null")) userDetails.setName(null);
//                else
                        userDetails.setName(resultSet.getString("name"));

                        userDetails.setisAnonymous(resultSet.getBoolean("isAnonymous"));
                    }
                }
            }

            if (userDetails.getId() == null)
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.NOT_FOUND.ordinal(),
                        ResponseStatus.FORMAT_JSON);

            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectFollowing)){
                preparedStatement.setString(1, email);
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        userDetails.getFollowing().add(resultSet.getString("followee"));
                    }
                }
            }

            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectFollowers)) {
                preparedStatement.setString(1, userDetails.getEmail());
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while (resultSet.next()) {
                        userDetails.getFollowers().add(resultSet.getString("follower"));
                    }
                }
            }

            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectSubscriptions)){
                preparedStatement.setString(1, userDetails.getEmail());
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next())
                        userDetails.getSubscriptions().add(resultSet.getInt("thread"));
                }
            }
        }
        catch (NullPointerException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(this.usersDetail != null) usersDetail.add(userDetails);
        String json = (new ResultJson<UserDetails>(
                ResponseStatus.ResponceCode.OK.ordinal(), userDetails)).getStringResult();

        return json;
    }

    @Override
    public String updateProfile(String json) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);


        String sql = "UPDATE " + Table.User.TABLE_USER +
                " SET " + Table.User.COLUMN_ABOUT +
                " =?, " + Table.User.COLUMN_NAME + "=?  WHERE " + Table.User.COLUMN_EMAIL + "=?;";

        String about, user, name;

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(json);
            about = root.get("about").asText();
            user  = root.get("user").asText();
            name  = root.get("name").asText();
        } catch (NullPointerException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }
        catch (IOException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
            preparedStatement.setString(1, about);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, user);
            if(preparedStatement.executeUpdate() == 0)
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details(user);
    }

    public UserDetails getUserDetatil(String email) {
        details(email);
        return this.userDetails;
    }

    @Override
    public void close() throws Exception {
//        statement.close();
//        resultSet.close();
//        preparedStatement.close();
    }
}
