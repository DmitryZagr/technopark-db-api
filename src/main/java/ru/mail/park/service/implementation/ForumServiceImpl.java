package ru.mail.park.service.implementation;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.stereotype.Component;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.forum.ForumCreateRequest;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.forum.Forum;
import ru.mail.park.model.Table;
import ru.mail.park.service.interfaces.IForumService;
import ru.mail.park.util.ConnectionToMySQL;

import java.sql.*;

/**
 * Created by admin on 08.10.16.
 */
@Component
public class ForumServiceImpl implements IForumService, AutoCloseable{
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

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
    public void close() throws Exception {
        preparedStatement.close();
        connection.close();
    }
}
