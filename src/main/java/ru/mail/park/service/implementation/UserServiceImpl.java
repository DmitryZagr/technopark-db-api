package ru.mail.park.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import javafx.scene.control.Tab;
import org.springframework.stereotype.Component;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Table;
import ru.mail.park.model.user.FollowUser;
import ru.mail.park.model.user.User;
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
            preparedStatement.setBoolean(3, user.isAnonymous());
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
        } catch (IOException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        String sql = "INSERT INTO " + Table.Followers.TABLE_FOLLOWERS +
                " (" + Table.Followers.COLUMN_FOLLOWER + ", " + Table.Followers.COLUMN_FOLLOWEE +
                ") VALUES (?, ?);" ;
        String sqlSelect = "SELECT * FROM " + Table.User.TABLE_USER + " " +
                "WHERE " + Table.User.COLUMN_EMAIL + "=?";
        FollowUser followUser = new FollowUser();
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, follower);
            preparedStatement.setString(2, followee);
            preparedStatement.execute();
            preparedStatement = connection.prepareStatement(sqlSelect);
            while (resultSet.next()) {
                followUser.setId(resultSet.getInt("idUser"));
                followUser.setUsername(resultSet.getString("username"));
                followUser.setAbout(resultSet.getString("about"));
                followUser.setName(resultSet.getString("name"));
                followUser.setEmail(resultSet.getString("email"));
                followUser.setAnonymous(resultSet.getBoolean("isAnonymus"));
                followUser.getFollowers().add(resultSet.getString("follower"));
                followUser.getFollowers().add(resultSet.getString("followee"));
            }
        }
//        catch (MySQLIntegrityConstraintViolationException e) {
//            return ResponseStatus.getMessage(
//                    ResponseStatus.ResponceCode.USER_EXIST.ordinal(), ResponseStatus.FORMAT_JSON);
//        }
        catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String json = (new ResultJson<FollowUser>(
                ResponseStatus.ResponceCode.OK.ordinal(), followUser)).getStringResult();

        return json;
    }

    @Override
    public void close() throws Exception {
        statement.close();
        resultSet.close();
        preparedStatement.close();
    }
}
