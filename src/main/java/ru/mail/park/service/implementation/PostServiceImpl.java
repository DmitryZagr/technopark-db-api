package ru.mail.park.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.post.DetailPost;
import ru.mail.park.model.post.IdPost;
import ru.mail.park.model.post.Post;
import ru.mail.park.model.Table;
import ru.mail.park.model.post.VotePost;
import ru.mail.park.service.interfaces.IPostService;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by admin on 08.10.16.
 */
@Component
@Transactional
public class PostServiceImpl implements IPostService, AutoCloseable{

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ThreadServiceImpl threadService;

    @Autowired
    private ForumServiceImpl forumService;

    @Autowired
    private UserServiceImpl userService;

    @Override
    public String create(Post post) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sqlInsert = "INSERT INTO " + Table.Post.TABLE_POST + " ( " +
                Table.Post.COLUMN_DATE + ',' + Table.Post.COLUMN_THREAD + ',' +
                Table.Post.COLUMN_MESSAGE   + ',' + Table.Post.COLUMN_USER + ',' +
                Table.Post.COLUMN_FORUM    + ',' + Table.Post.COLUMN_PARENT + ',' +
                Table.Post.COLUMN_IS_APPROVED  + ',' + Table.Post.COLUMN_IS_HIGHLIGHED + ',' +
                Table.Post.COLUMN_IS_EDITED   + ',' + Table.Post.COLUMN_IS_SPAM + ',' +
                Table.Post.COLUMN_IS_DELETED  +  ')' +
                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        final String sqlUpdPath = "UPDATE  " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_PATH + " =?, " + Table.Post.COLUMN_ROOT + " =? " +
                " WHERE " + Table.Post.COLUMN_ID_POST +
                "=?";

        try {

            try(PreparedStatement preparedStatement =
                        connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, post.getDate());
                preparedStatement.setLong(2, post.getThread());
                preparedStatement.setString(3, post.getMessage());
                preparedStatement.setString(4, post.getUser());
                preparedStatement.setString(5, post.getForum());
                if (post.getParent() == null) preparedStatement.setNull(6, Types.INTEGER);
                else preparedStatement.setInt(6, post.getParent());
                preparedStatement.setBoolean(7, post.getisApproved());
                preparedStatement.setBoolean(8, post.getisHighlighted());
                preparedStatement.setBoolean(9, post.getisEdited());
                preparedStatement.setBoolean(10, post.getisSpam());
                preparedStatement.setBoolean(11, post.getisDeleted());
                preparedStatement.executeUpdate();
                connection.commit();
                try(ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) post.setid(resultSet.getInt(1));
                }
            }

            final String path = getPath(post.getParent(), post.getid());

            int root = 0;
            if(path != null && path.contains(".")) {
                root = Integer.parseInt(path.substring(0, path.indexOf('.')));
            }

            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdPath)) {
                preparedStatement.setString(1, path);
                if (post.getParent() == null)
                    preparedStatement.setString(2, Integer.toString(post.getid().intValue()));
                else preparedStatement.setInt(2, root);
                preparedStatement.setInt(3, post.getid().intValue());
                preparedStatement.executeUpdate();
                connection.commit();
            }

        } catch (MySQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (new ResultJson<Post>(
                ResponseStatus.ResponceCode.OK.ordinal(), post)).getStringResult();
    }

    @Override
    public String removeOrRestore(IdPost idPost, boolean isDeleted) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sql = "UPDATE " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_IS_DELETED + "=? WHERE " +
                Table.Post.COLUMN_ID_POST + "=?;";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setBoolean(1, isDeleted);
            preparedStatement.setLong(2, idPost.getid());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (new ResultJson<IdPost>(
                ResponseStatus.ResponceCode.OK.ordinal(), idPost)).getStringResult();
    }

    @Override
    public String vote(String vote) {

        final String sqlSel = "SELECT "
                + Table.Post.COLUMN_ID_POST + "," + Table.Post.COLUMN_FORUM + ","
                + Table.Post.COLUMN_DATE + "," + Table.Post.COLUMN_IS_APPROVED + ","
                + Table.Post.COLUMN_IS_DELETED + "," + Table.Post.COLUMN_IS_EDITED + ","
                + Table.Post.COLUMN_IS_HIGHLIGHED + "," + Table.Post.COLUMN_IS_SPAM + ","
                + Table.Post.COLUMN_MESSAGE + "," + Table.Post.COLUMN_PARENT + ","
                + Table.Post.COLUMN_THREAD + "," + Table.Post.COLUMN_USER + ","
                + Table.Post.COLUMN_LIKE + "," + Table.Post.COLUMN_DISLIKE + " "
                + "FROM " + Table.Post.TABLE_POST + " WHERE "

                + Table.Post.COLUMN_ID_POST + "=? " + ';';

        final int _vote;
        final int idPost;

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = (ObjectNode) mapper.readTree(vote);
            _vote = root.get("vote").asInt();
            idPost = root.get("post").asInt();
            if((_vote > 1 || _vote < -1) || _vote == 0 )
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
        } catch (IOException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (NullPointerException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        final String sqlCol = (_vote == 1) ? Table.Post.COLUMN_LIKE : Table.Post.COLUMN_DISLIKE;

        final String sqlUpd = "UPDATE " + Table.Post.TABLE_POST+ " SET " +
                sqlCol + "=? WHERE " +
                Table.Post.COLUMN_ID_POST + "=? ;";
        final VotePost votePost = new VotePost();

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)) {
                preparedStatement.setLong(1, idPost);
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        votePost.setDate(resultSet.getString("date").replace(".0", ""));
                        votePost.setForum(resultSet.getString("forum"));
                        votePost.setid(resultSet.getInt("idPost"));
                        votePost.setApproved(resultSet.getBoolean("isApproved"));
                        votePost.setDeleted(resultSet.getBoolean("isDeleted"));
                        votePost.setEdited(resultSet.getBoolean("isEdited"));
                        votePost.setHighlighted(resultSet.getBoolean("isHighlighted"));
                        votePost.setSpam(resultSet.getBoolean("isSpam"));
                        votePost.setMessage(resultSet.getString("message"));
                        votePost.setParent((Integer) resultSet.getObject("parent"));
                        votePost.setThread(resultSet.getInt("thread"));
                        votePost.setUser(resultSet.getString("user"));
                        votePost.setLikes(resultSet.getInt("like"));
                        votePost.setDislikes((resultSet.getInt("dislike")));
                        if (_vote == 1) votePost.setLikes(votePost.getLikes() + 1);
                        else votePost.setDislikes(votePost.getDislikes() + 1);
                        votePost.setPoints();
                    }
                }
            }
            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlUpd)) {
                if (_vote == 1) preparedStatement.setLong(1, votePost.getLikes());
                else preparedStatement.setLong(1, votePost.getDislikes());
                preparedStatement.setLong(2, votePost.getid());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (new ResultJson<>(
                ResponseStatus.ResponceCode.OK.ordinal(), votePost)).getStringResult();
    }

    @Override
    public String update(String update) {

        final String sqlSel = "SELECT "
                + Table.Post.COLUMN_ID_POST + "," + Table.Post.COLUMN_FORUM + ","
                + Table.Post.COLUMN_DATE + "," + Table.Post.COLUMN_IS_APPROVED + ","
                + Table.Post.COLUMN_IS_DELETED + "," + Table.Post.COLUMN_IS_EDITED + ","
                + Table.Post.COLUMN_IS_HIGHLIGHED + "," + Table.Post.COLUMN_IS_SPAM + ","
                + Table.Post.COLUMN_MESSAGE + "," + Table.Post.COLUMN_PARENT + ","
                + Table.Post.COLUMN_THREAD + "," + Table.Post.COLUMN_USER + ","
                + Table.Post.COLUMN_LIKE + "," + Table.Post.COLUMN_DISLIKE + " "
                + "FROM " + Table.Post.TABLE_POST + " WHERE "
                + Table.Post.COLUMN_ID_POST + "=?; ";

        final String message;
        final int idPost;

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = (ObjectNode) mapper.readTree(update);
            message = root.get("message").asText();
            idPost = root.get("post").asInt();
        } catch (IOException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        String sqlUpd = "UPDATE " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_MESSAGE + "=? " ;


        final String sqlUpdateback = " WHERE " +
                Table.Post.COLUMN_ID_POST + "=?";

        final String sqlEdited = " , " + Table.Post.COLUMN_IS_EDITED + "=1 ";

        final VotePost votePost = new VotePost();

        final Connection connection = DataSourceUtils.getConnection(dataSource);


        try {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)) {
                preparedStatement.setLong(1, idPost);
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        votePost.setDate(resultSet.getString("date").replace(".0", ""));
                        votePost.setForum(resultSet.getString("forum"));
                        votePost.setid(resultSet.getInt("idPost"));
                        votePost.setApproved(resultSet.getBoolean("isApproved"));
                        votePost.setDeleted(resultSet.getBoolean("isDeleted"));
                        votePost.setEdited(resultSet.getBoolean("isEdited"));
                        votePost.setHighlighted(resultSet.getBoolean("isHighlighted"));
                        votePost.setSpam(resultSet.getBoolean("isSpam"));
                        votePost.setMessage(message);
                        votePost.setParent((Integer) resultSet.getObject("parent"));
                        votePost.setThread(resultSet.getInt("thread"));
                        votePost.setUser(resultSet.getString("user"));
                        votePost.setLikes(votePost.getLikes());
                        votePost.setDislikes(votePost.getDislikes());
                        votePost.setPoints();
                    }
                }
            }

            if(!votePost.getMessage().equals(message))
                sqlUpd = sqlUpd + sqlEdited + sqlUpdateback;
            else sqlUpd = sqlUpd + sqlUpdateback;

            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlUpd)) {
                preparedStatement.setString(1, message);
                preparedStatement.setLong(2, votePost.getid());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (new ResultJson<>(
                ResponseStatus.ResponceCode.OK.ordinal(), votePost)).getStringResult();
    }

    @Override
    public String list(String forum, Long thread, String since, Long limit, String order) {
        final String reqCondition;
        final String dateContidion;
        final String orderCondition;
        final String limitCondition;
        reqCondition = (forum != null) ? Table.Post.COLUMN_FORUM : Table.Post.COLUMN_THREAD;

        String sqlSel = "SELECT "
                + Table.Post.COLUMN_ID_POST + "," + Table.Post.COLUMN_FORUM + ","
                + Table.Post.COLUMN_DATE + "," + Table.Post.COLUMN_IS_APPROVED + ","
                + Table.Post.COLUMN_IS_DELETED + "," + Table.Post.COLUMN_IS_EDITED + ","
                + Table.Post.COLUMN_IS_HIGHLIGHED + "," + Table.Post.COLUMN_IS_SPAM + ","
                + Table.Post.COLUMN_MESSAGE + "," + Table.Post.COLUMN_PARENT + ","
                + Table.Post.COLUMN_THREAD + "," + Table.Post.COLUMN_USER + ","
                + Table.Post.COLUMN_LIKE + "," + Table.Post.COLUMN_DISLIKE + " "
                + "FROM " + Table.Post.TABLE_POST + " WHERE "
                + "  " + reqCondition + "=?"  ;

        dateContidion = (since != null) ?  " AND " + Table.Post.COLUMN_DATE + ">=\'" + since + '\'' : " ";
        sqlSel = sqlSel + dateContidion;

        orderCondition = (order != null) ? " ORDER BY " + Table.Post.COLUMN_DATE +
                ' ' + order + ' ' : " ";
        sqlSel = sqlSel + orderCondition;

        limitCondition = (limit != null) ? " LIMIT "+ limit.longValue()  : " ";
        sqlSel = sqlSel +  limitCondition;

        final ArrayList<VotePost> votePosts = new ArrayList<>();

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)) {
            if(forum != null) preparedStatement.setString(1, forum);
            else preparedStatement.setLong(1, thread);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    final VotePost vp = new VotePost();
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
                    vp.setThread(resultSet.getInt("thread"));
                    vp.setUser(resultSet.getString("user"));
                    vp.setLikes(resultSet.getInt("like"));
                    vp.setDislikes(resultSet.getInt("dislike"));
                    vp.setPoints();
                    votePosts.add(vp);
                }
            }
        } catch (MySQLIntegrityConstraintViolationException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.EXIST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (new ResultJson<ArrayList<VotePost>>(
                ResponseStatus.ResponceCode.OK.ordinal(), votePosts)).getStringResult();
    }

    public DetailPost<Object, Object, Object> getPostDetails(Integer post, String related) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sqlSel = "SELECT "
                + Table.Post.COLUMN_ID_POST + "," + Table.Post.COLUMN_FORUM + ","
                + Table.Post.COLUMN_DATE + "," + Table.Post.COLUMN_IS_APPROVED + ","
                + Table.Post.COLUMN_IS_DELETED + "," + Table.Post.COLUMN_IS_EDITED + ","
                + Table.Post.COLUMN_IS_HIGHLIGHED + "," + Table.Post.COLUMN_IS_SPAM + ","
                + Table.Post.COLUMN_MESSAGE + "," + Table.Post.COLUMN_PARENT + ","
                + Table.Post.COLUMN_THREAD + "," + Table.Post.COLUMN_USER + ","
                + Table.Post.COLUMN_LIKE + "," + Table.Post.COLUMN_DISLIKE + " "
                + "FROM " + Table.Post.TABLE_POST + " WHERE "
                + Table.Post.COLUMN_ID_POST + "=?;";

        final DetailPost<Object, Object, Object> postDetail = new DetailPost<>();


        try {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)) {
                preparedStatement.setLong(1, post.intValue());
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        postDetail.setDate(resultSet.getString("date").replace(".0", ""));
                        postDetail.setForum(resultSet.getString("forum"));
                        postDetail.setid((Integer) resultSet.getObject("idPost"));
                        postDetail.setApproved(resultSet.getBoolean("isApproved"));
                        postDetail.setDeleted(resultSet.getBoolean("isDeleted"));
                        postDetail.setEdited(resultSet.getBoolean("isEdited"));
                        postDetail.setHighlighted(resultSet.getBoolean("isHighlighted"));
                        postDetail.setSpam(resultSet.getBoolean("isSpam"));
                        postDetail.setMessage(resultSet.getString("message"));
                        postDetail.setParent((Integer) resultSet.getObject("parent"));
                        postDetail.setThread(resultSet.getInt("thread"));
                        postDetail.setUser(resultSet.getString("user"));
                        postDetail.setLikes(resultSet.getInt("like"));
                        postDetail.setDislikes(resultSet.getInt("dislike"));
                        postDetail.setPoints();

                        if (!StringUtils.isEmpty(related) && related.contains("forum")) {
                            postDetail.setForum(forumService.getForum((String) postDetail.getForum()));
                        }

                        if (!StringUtils.isEmpty(related) && related.contains("thread")) {
                            postDetail.setThread(threadService.getThreadDetatils((Integer) postDetail.getThread(), null));
                        }

                        if (!StringUtils.isEmpty(related) && related.contains("user")) {
                            userService.getUserDetail((String) postDetail.getUser());
                            postDetail.setUser(userService.getUserDetail((String) postDetail.getUser()));
                        }
                    }
                }
            }
        } catch (MySQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
        } catch (MySQLSyntaxErrorException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return postDetail;
    }


    @Override
    public String details(Integer post, String related) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sqlSel = "SELECT "
                + Table.Post.COLUMN_ID_POST + "," + Table.Post.COLUMN_FORUM + ","
                + Table.Post.COLUMN_DATE + "," + Table.Post.COLUMN_IS_APPROVED + ","
                + Table.Post.COLUMN_IS_DELETED + "," + Table.Post.COLUMN_IS_EDITED + ","
                + Table.Post.COLUMN_IS_HIGHLIGHED + "," + Table.Post.COLUMN_IS_SPAM + ","
                + Table.Post.COLUMN_MESSAGE + "," + Table.Post.COLUMN_PARENT + ","
                + Table.Post.COLUMN_THREAD + "," + Table.Post.COLUMN_USER + ","
                + Table.Post.COLUMN_LIKE + "," + Table.Post.COLUMN_DISLIKE + " "
                + "FROM " + Table.Post.TABLE_POST + " WHERE "
                + Table.Post.COLUMN_ID_POST + "=?;";

        final DetailPost<Object, Object, Object> postDetail = new DetailPost<>();

        try {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)) {
                preparedStatement.setLong(1, post.intValue());
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        postDetail.setDate(resultSet.getString("date").replace(".0", ""));
                        postDetail.setForum(resultSet.getString("forum"));
                        postDetail.setid((Integer) resultSet.getObject("idPost"));
                        postDetail.setApproved(resultSet.getBoolean("isApproved"));
                        postDetail.setDeleted(resultSet.getBoolean("isDeleted"));
                        postDetail.setEdited(resultSet.getBoolean("isEdited"));
                        postDetail.setHighlighted(resultSet.getBoolean("isHighlighted"));
                        postDetail.setSpam(resultSet.getBoolean("isSpam"));
                        postDetail.setMessage(resultSet.getString("message"));
                        postDetail.setParent((Integer) resultSet.getObject("parent"));
                        postDetail.setThread(resultSet.getInt("thread"));
                        postDetail.setUser(resultSet.getString("user"));
                        postDetail.setLikes(resultSet.getInt("like"));
                        postDetail.setDislikes(resultSet.getInt("dislike"));
                        postDetail.setPoints();

                        if (!StringUtils.isEmpty(related) && related.contains("forum")) {
                            postDetail.setForum(forumService.getForum((String) postDetail.getForum()));
                        }

                        if (!StringUtils.isEmpty(related) && related.contains("thread")) {
                            postDetail.setThread(threadService.getThreadDetatils((Integer) postDetail.getThread(), null));
                        }

                        if (!StringUtils.isEmpty(related) && related.contains("user")) {
                            userService.getUserDetail((String) postDetail.getUser());
                            postDetail.setUser(userService.getUserDetail((String) postDetail.getUser()));
                        }
                    }
                }
            }
        } catch (MySQLIntegrityConstraintViolationException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.EXIST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (MySQLSyntaxErrorException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(postDetail.getid() == null)
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.NOT_FOUND.ordinal(),
                    ResponseStatus.FORMAT_JSON);

        return (new ResultJson<DetailPost<Object, Object, Object>>(
                ResponseStatus.ResponceCode.OK.ordinal(), postDetail)).getStringResult();
    }

    private String getPath(Integer parent, Integer idPost) {

        if(parent == null)
            return Integer.toString(idPost.intValue());

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        String matPath = "";

        final String sql = " Select " +
                Table.Post.COLUMN_PATH + " " +
                " FROM " + Table.Post.TABLE_POST +
                " WHERE " + Table.Post.COLUMN_ID_POST + "=?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, parent.intValue());
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    matPath = resultSet.getString("path");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final String sqlPath = " Select "
                + Table.Post.COLUMN_PATH + " "
                + "FROM " + Table.Post.TABLE_POST
                + " WHERE " + Table.Post.COLUMN_PATH + " LIKE " + "?"
                + " ORDER BY " + Table.Post.COLUMN_PATH + " DESC LIMIT 1";

        String lastPath = null;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlPath)) {
            //^(STRING)([.])([0-9]{3})$
            preparedStatement.setString(1,   matPath.concat(".___"));
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    lastPath = resultSet.getString("path");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(lastPath == null) {
            matPath = matPath + ".000";
        } else {
            String subPath = Integer.toString(
                    Integer.parseInt(
                        lastPath.substring(lastPath.lastIndexOf('.') + 1, lastPath.length() )) + 1);
            if(subPath.length() != 3)
                subPath = addStrZero(subPath);

            matPath = matPath + "." + subPath;
        }

        return matPath;
    }

    private String addStrZero(String str) {
        if(!StringUtils.isEmpty(str) && str.length() != 3) {
            switch (str.length()) {
                case 0: { str = "000"; break;}
                case 1: { str = "00" + str; break;}
                case 2: { str = "0" + str; break;}
//                case 3: { str = "0" + str; break;}
            }

        }
        return str;
    }



    @Override
    public void close() throws Exception {
    }
}
