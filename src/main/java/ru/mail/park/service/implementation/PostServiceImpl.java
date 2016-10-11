package ru.mail.park.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import javafx.geometry.Pos;
import org.springframework.stereotype.Component;
import ru.mail.park.api.common.Result;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.Post;
import ru.mail.park.model.Table;
import ru.mail.park.service.interfaces.IPostService;
import ru.mail.park.util.ConnectionToMySQL;
import ru.mail.park.util.MySqlUtilRequests;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by admin on 08.10.16.
 */
@Component
public class PostServiceImpl implements IPostService, AutoCloseable{

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @Override
    public String create(Post post) {
        connection =  ConnectionToMySQL.getConnection();

        String sqlInsert = "INSERT INTO " + Table.Post.TABLE_POST + " ( " +
                Table.Post.COLUMN_DATE + ',' + Table.Post.COLUMN_THREAD + ',' +
                Table.Post.COLUMN_MESSAGE   + ',' + Table.Post.COLUMN_USER + ',' +
                Table.Post.COLUMN_FORUM    + ',' + Table.Post.COLUMN_PARENT + "," +
                Table.Post.COLUMN_IS_APPROVED  + ',' + Table.Post.COLUMN_IS_HIGHLIGHED + ',' +
                Table.Post.COLUMN_IS_EDITED   + ',' + Table.Post.COLUMN_IS_SPAM+ ',' +
                Table.Post.COLUMN_IS_DELETED  + ')' +
                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try {
            preparedStatement = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, post.getDate());
            preparedStatement.setLong(2, post.getThread());
            preparedStatement.setString(3, post.getMessage());
            preparedStatement.setString(4, post.getUser());
            preparedStatement.setString(5, post.getForum());
            preparedStatement.setLong(6, post.getParent());
            preparedStatement.setBoolean(7, post.isApproved());
            preparedStatement.setBoolean(8, post.isHighlighted());
            preparedStatement.setBoolean(9, post.isEdited());
            preparedStatement.setBoolean(10, post.isSpam());
            preparedStatement.setBoolean(11, post.isDeleted());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            while(resultSet.next())
                post.setIdPost(resultSet.getLong(1));
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

        String json = (new ResultJson<Post>(
                ResponseStatus.ResponceCode.OK.ordinal(), post)).getStringResult();

        return json;
    }

    @Override
    public void close() throws Exception {
        preparedStatement.close();
        connection.close();
    }
}
