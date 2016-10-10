package ru.mail.park.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.stereotype.Component;
import ru.mail.park.api.common.Result;
import ru.mail.park.api.forum.ForumCreateRequest;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Forum;
import ru.mail.park.model.Table;
import ru.mail.park.service.interfaces.IForumService;
import ru.mail.park.util.ConnectionToMySQL;
import ru.mail.park.util.MySqlUtilRequests;

import java.sql.*;

/**
 * Created by admin on 08.10.16.
 */
@Component
public class ForumServiceImpl implements IForumService, AutoCloseable{
    private Connection connection;
    private PreparedStatement preparedStatement;

    @Override
    public String create(Forum forum) {

        connection =  ConnectionToMySQL.getConnection();

        String sqlInsert = "INSERT INTO " + Table.Forum.TABLE_FORUM + " ( " +
                Table.Forum.COLUMN_ID_FORUM + ',' + Table.Forum.COLUMN_NAME + ',' +
                Table.Forum.COLUMN_SHORT_NAME + ',' + Table.Forum.COLUMN_USER + ')' +
                "VALUES (?, ?, ?, ?);";

        try {
            forum.setId(ForumCreateRequest.getExistingId(forum.getName(), forum.getShort_name()));
            if(forum.getId() == -1)
                return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.UNKNOWN_ERROR.ordinal(),
                    ResponseStatus.FORMAT_JSON);

            if(forum.getId() != 0)
                return "{ \"code\":" + ResponseStatus.ResponceCode.OK.ordinal() +
                        ",\"responce\": {" + forum.toString() + "}}";

            forum.setId(MySqlUtilRequests.countRowsInTable(Table.Forum.TABLE_FORUM) + 1);

            preparedStatement = connection.prepareStatement(sqlInsert);
            preparedStatement.setLong(1, forum.getId());
            preparedStatement.setString(2, forum.getName());
            preparedStatement.setString(3, forum.getShort_name());
            preparedStatement.setString(4, forum.getUser());
            preparedStatement.execute();

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



        return "{ \"code\":" + ResponseStatus.ResponceCode.OK.ordinal() +
                ",\"responce\": {" + forum.toString() + "}}";
    }

    @Override
    public void close() throws Exception {
        preparedStatement.close();
        connection.close();
    }
}
