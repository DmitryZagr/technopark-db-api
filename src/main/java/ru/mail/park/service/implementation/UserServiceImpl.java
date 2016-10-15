package ru.mail.park.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.stereotype.Component;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Table;
import ru.mail.park.model.user.User;
import ru.mail.park.model.user.UserDetails;
import ru.mail.park.service.interfaces.IUserService;
import ru.mail.park.util.ConnectionToMySQL;

import java.io.IOException;
import java.sql.*;

/**
 * Created by admin on 08.10.16.
 */
@Component
public class UserServiceImpl implements IUserService, AutoCloseable {

    private ObjectMapper mapper = new ObjectMapper();

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;

    @Override
    public String create(User user) {
        connection =  ConnectionToMySQL.getConnection();

        if(user.isEmpty())
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);

        String sqlInsert = "INSERT INTO " + Table.User.TABLE_USER + " ( " +
                Table.User.COLUMN_USERNAME + ',' +
                Table.User.COLUMN_ABOUT + ',' + Table.User.COLUMN_IS_ANONYMOUS + ',' +
                Table.User.COLUMN_NAME + ',' + Table.User.COLUMN_EMAIL + " ) " +
                "VALUES ( ?, ?, ?, ?, ?); ";

        try {
            preparedStatement = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getAbout());
            preparedStatement.setBoolean(3, user.getisAnonymous());
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
    public String follow(String followerFollowee) {
        connection =  ConnectionToMySQL.getConnection();
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

        try {
            preparedStatement = connection.prepareStatement(sql);
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
    public String unFollow(String followerFollowee) {
        connection =  ConnectionToMySQL.getConnection();

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

        try {
            preparedStatement = connection.prepareStatement(sqlUnFollow);
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
        connection =  ConnectionToMySQL.getConnection();
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

        UserDetails detailUser = new UserDetails();
        detailUser.setEmail(email);

        try {

            preparedStatement = connection.prepareStatement(sqlSelectUser);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                detailUser.setId(resultSet.getLong("idUser"));
                detailUser.setUsername(resultSet.getString("username"));
                detailUser.setAbout(resultSet.getString("about"));
                detailUser.setName(resultSet.getString("name"));
                detailUser.setisAnonymous(resultSet.getBoolean("isAnonymous"));
            }

            if (detailUser.getId() == null)
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.NOT_FOUND.ordinal(),
                        ResponseStatus.FORMAT_JSON);

            preparedStatement = connection.prepareStatement(sqlSelectFollowing);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                detailUser.getFollowing().add(resultSet.getString("followee"));
            }

            preparedStatement = connection.prepareStatement(sqlSelectFollowers);
            preparedStatement.setString(1, detailUser.getEmail());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
//                detailUser.setId(resultSet.getLong("idUser"));
//                detailUser.setUsername(resultSet.getString("username"));
//                detailUser.setAbout(resultSet.getString("about"));
//                detailUser.setName(resultSet.getString("name"));
//                detailUser.setisAnonymous(resultSet.getBoolean("isAnonymous"));
                detailUser.getFollowers().add(resultSet.getString("follower"));
            }

//            if (detailUser.getId() == null)
//                return ResponseStatus.getMessage(
//                        ResponseStatus.ResponceCode.NOT_FOUND.ordinal(),
//                        ResponseStatus.FORMAT_JSON);

            preparedStatement = connection.prepareStatement(sqlSelectSubscriptions);
            preparedStatement.setString(1, detailUser.getEmail());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
                detailUser.getSubscriptions().add(resultSet.getInt("thread"));
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

        String json = (new ResultJson<UserDetails>(
                ResponseStatus.ResponceCode.OK.ordinal(), detailUser)).getStringResult();

        return json;
    }

    @Override
    public String updateProfile(String json) {
        connection =  ConnectionToMySQL.getConnection();

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
        try {
            preparedStatement = connection.prepareStatement(sql);
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

    @Override
    public void close() throws Exception {
        statement.close();
        resultSet.close();
        preparedStatement.close();
    }
}
