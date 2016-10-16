package ru.mail.park.service.implementation;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.stereotype.Component;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.forum.ForumCreateRequest;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.forum.Forum;
import ru.mail.park.model.Table;
import ru.mail.park.model.user.UserDetails;
import ru.mail.park.service.interfaces.IForumService;
import ru.mail.park.util.ConnectionToMySQL;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by admin on 08.10.16.
 */
@Component
public class ForumServiceImpl implements IForumService, AutoCloseable{
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private Forum forum;

    @Override
    public String create(Forum forum) {

        connection =  ConnectionToMySQL.getConnection();

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

            preparedStatement = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, forum.getName());
            preparedStatement.setString(2, forum.getShort_name());
            preparedStatement.setString(3, forum.getUser());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            while(resultSet.next())
                forum.setId(resultSet.getLong(1));

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
    public String listUsers(String forum, Integer limit, String order, Integer since_id) {
        connection =  ConnectionToMySQL.getConnection();
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

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, forum);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) emails.add(resultSet.getString("email"));

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

    public Forum getForum(String short_name) throws SQLException {
        connection =  ConnectionToMySQL.getConnection();
        String sql = "SELECT * FROM " + Table.Forum.TABLE_FORUM +
                " WHERE " + Table.Forum.COLUMN_SHORT_NAME + "=?";
        forum = new Forum();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, short_name);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                forum.setId(resultSet.getInt("idForum"));
                forum.setName(resultSet.getString("name"));
                forum.setShort_name(resultSet.getString("short_name"));
                forum.setUser(resultSet.getString("user"));
            }

        return forum;
    }

    @Override
    public void close() throws Exception {
        preparedStatement.close();
        connection.close();
    }
}
