package ru.mail.park.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.mail.park.api.common.ResultJson;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.post.DetailPost;
import ru.mail.park.model.post.IdPost;
import ru.mail.park.model.post.Post;
import ru.mail.park.model.Table;
import ru.mail.park.model.post.VotePost;
import ru.mail.park.model.thread.ThreadVote;
import ru.mail.park.model.user.UserDetails;
import ru.mail.park.service.interfaces.IPostService;
import ru.mail.park.util.ConnectionToMySQL;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by admin on 08.10.16.
 */
@Component
public class PostServiceImpl implements IPostService, AutoCloseable{

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private ObjectMapper mapper = new ObjectMapper();
    private DetailPost<Object,Object,Object> votePost ;

    @Autowired
    private ThreadServiceImpl threadService;

    public DetailPost<Object, Object, Object> getVotePost() {
        return votePost;
    }

    @Override
    public String create(Post post) {
        connection =  ConnectionToMySQL.getConnection();

        String sqlApproveThread = "SELECT * FROM " + Table.Thread.TABLE_THREAD +
                " WHERE " + Table.Thread.COLUMN_ID_THREAD + "=" + post.getThread();

        String sqlInsert = "INSERT INTO " + Table.Post.TABLE_POST + " ( " +
                Table.Post.COLUMN_DATE + ',' + Table.Post.COLUMN_THREAD + ',' +
                Table.Post.COLUMN_MESSAGE   + ',' + Table.Post.COLUMN_USER + ',' +
                Table.Post.COLUMN_FORUM    + ',' + Table.Post.COLUMN_PARENT + "," +
                Table.Post.COLUMN_IS_APPROVED  + ',' + Table.Post.COLUMN_IS_HIGHLIGHED + ',' +
                Table.Post.COLUMN_IS_EDITED   + ',' + Table.Post.COLUMN_IS_SPAM+ ',' +
                Table.Post.COLUMN_IS_DELETED  +  ')' +
                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        String sqlUpdPath = "UPDATE  " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_PATH + " =?, " + Table.Post.COLUMN_ROOT + " =? " +
                " WHERE " + Table.Post.COLUMN_ID_POST +
                "=?";

        String insertInVotePost =  "INSERT INTO " + Table.VotePost.TABLE_VOTE_POST +
                " ( " + Table.VotePost.COLUMN_ID_POST + ')' + "VALUES (?);";
        try {
            preparedStatement = connection.prepareStatement(sqlApproveThread);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                if (resultSet.getBoolean("isDeleted"))
                    return ResponseStatus.getMessage(
                            ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                            ResponseStatus.FORMAT_JSON);
            } else
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);

            preparedStatement = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, post.getDate());
            preparedStatement.setLong(2, post.getThread());
            preparedStatement.setString(3, post.getMessage());
            preparedStatement.setString(4, post.getUser());
            preparedStatement.setString(5, post.getForum());
            if(post.getParent() == null) preparedStatement.setNull(6, Types.INTEGER);
            else preparedStatement.setInt(6, post.getParent());
            preparedStatement.setBoolean(7, post.getisApproved());
            preparedStatement.setBoolean(8, post.getisHighlighted());
            preparedStatement.setBoolean(9, post.getisEdited());
            preparedStatement.setBoolean(10, post.getisSpam());
            preparedStatement.setBoolean(11, post.getisDeleted());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            if(resultSet.next()) post.setid(resultSet.getInt(1));

            String path = getPath(post.getParent(), post.getid());

            String root = "";
            if(path != null && path.contains(".")) {
                root = path.substring(0, path.indexOf("."));
            }

            preparedStatement = connection.prepareStatement(sqlUpdPath);
            preparedStatement.setString(1, path);
            if(post.getParent() == null)
            preparedStatement.setString(2, Integer.toString(post.getid().intValue()));
            else preparedStatement.setString(2, root);
            preparedStatement.setInt(3, post.getid().intValue());
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement(insertInVotePost,
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, post.getid());
            preparedStatement.execute();
        } catch (MySQLIntegrityConstraintViolationException e) {
//            return ResponseStatus.getMessage(
//                        ResponseStatus.ResponceCode.USER_EXIST.ordinal(),
//                        ResponseStatus.FORMAT_JSON);
            e.printStackTrace();
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
    public String removeOrRestore(IdPost idPost, boolean isDeleted) {
        connection =  ConnectionToMySQL.getConnection();
        String sql = "UPDATE " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_IS_DELETED + "=? WHERE " +
                Table.Post.COLUMN_ID_POST + "=?;";
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setBoolean(1, isDeleted);
            preparedStatement.setLong(2, idPost.getid());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String json = (new ResultJson<IdPost>(
                ResponseStatus.ResponceCode.OK.ordinal(), idPost)).getStringResult();

        return json;
    }

    @Override
    public String vote(String vote) {
        connection =  ConnectionToMySQL.getConnection();

        String sqlSel = "SELECT * FROM " + Table.Post.TABLE_POST + "INNER JOIN " +
                Table.VotePost.TABLE_VOTE_POST + " ON " +
                Table.VotePost.COLUMN_ID_POST + "=? AND " + Table.Post.COLUMN_ID_POST + "=" +
                Table.VotePost.COLUMN_ID_POST + ";";

        int _vote = 0;
        int idPost = 0;

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(vote);
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
        } catch (java.lang.NullPointerException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        String sqlCol = (_vote == 1) ? Table.VotePost.COLUMN_LIKE : Table.VotePost.COLUMN_DISLIKE;

        String sqlUpd = "UPDATE " + Table.VotePost.TABLE_VOTE_POST + " SET " +
                sqlCol + "=? WHERE " +
                Table.VotePost.COLUMN_ID_POST + "=? ;";
        VotePost votePost = new VotePost();
        try {
            preparedStatement = connection.prepareStatement(sqlSel);
            preparedStatement.setLong(1, idPost);
            resultSet = preparedStatement.executeQuery();
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
                if(_vote == 1) votePost.setLikes(votePost.getLikes() + 1);
                else votePost.setDislikes(votePost.getDislikes() + 1);
                votePost.setPoints();
            }
            preparedStatement = connection.prepareStatement(sqlUpd);
            if(_vote == 1) preparedStatement.setLong(1, votePost.getLikes());
            else preparedStatement.setLong(1, votePost.getDislikes());
            preparedStatement.setLong(2, votePost.getid());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String json = (new ResultJson<>(
                ResponseStatus.ResponceCode.OK.ordinal(), votePost)).getStringResult();

        return json;
    }

    @Override
    public String update(String update) {
        connection =  ConnectionToMySQL.getConnection();

        String sqlSel = "SELECT * FROM " + Table.Post.TABLE_POST + "INNER JOIN " +
                Table.VotePost.TABLE_VOTE_POST + " ON " +
                Table.VotePost.COLUMN_ID_POST + "=? AND " +
                Table.VotePost.COLUMN_ID_POST + "=" + Table.Post.COLUMN_ID_POST;

        String message = null;
        int idPost = 0;

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(update);
            message = root.get("message").asText();
            idPost = root.get("post").asInt();
        } catch (IOException e) {
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                    ResponseStatus.FORMAT_JSON);
        }

        String sqlUpd = "UPDATE " + Table.Post.TABLE_POST + " SET " +
                Table.Post.COLUMN_MESSAGE + "=? " ;


        String sqlUpdateback = " WHERE " +
                Table.Post.COLUMN_ID_POST + "=?";

        String sqlEdited = " , " + Table.Post.COLUMN_IS_EDITED + "=1 ";

        VotePost votePost = new VotePost();
        try {
            preparedStatement = connection.prepareStatement(sqlSel);
            preparedStatement.setLong(1, idPost);
            resultSet = preparedStatement.executeQuery();
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

            if(!votePost.getMessage().equals(message))
                sqlUpd = sqlUpd + sqlEdited + sqlUpdateback;
            else sqlUpd = sqlUpd + sqlUpdateback;

            preparedStatement = connection.prepareStatement(sqlUpd);
            preparedStatement.setString(1, message);
            preparedStatement.setLong(2, votePost.getid());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String json = (new ResultJson<>(
                ResponseStatus.ResponceCode.OK.ordinal(), votePost)).getStringResult();

        return json;
    }

    @Override
    public String list(String forum, Long thread, String since, Long limit, String order) {
        connection =  ConnectionToMySQL.getConnection();
        String reqCondition, dateContidion, orderCondition, limitCondition;
        reqCondition = (forum != null) ? Table.Post.COLUMN_FORUM : Table.Post.COLUMN_THREAD;

        String sqlSel = "SELECT * FROM " + Table.Post.TABLE_POST + "INNER JOIN " +
                Table.VotePost.TABLE_VOTE_POST + " ON " +
                Table.VotePost.COLUMN_ID_POST + "=" + Table.Post.COLUMN_ID_POST +
                " AND " + reqCondition + "=?"  ;

        dateContidion = (since != null) ?  " AND " + Table.Post.COLUMN_DATE + ">=\'" + since + "\'": " ";
        sqlSel = sqlSel + dateContidion;

        orderCondition = (order != null) ? " ORDER BY " + Table.Post.COLUMN_DATE +
                                                                " " + order + " ": " ";
        sqlSel = sqlSel + orderCondition;

        limitCondition = (limit != null) ? " LIMIT "+ limit.longValue()  : " ";
        sqlSel = sqlSel +  limitCondition;

        ArrayList<VotePost> votePosts = new ArrayList<>();

        try {
            preparedStatement = connection.prepareStatement(sqlSel);
            if(forum != null) preparedStatement.setString(1, forum);
            else preparedStatement.setLong(1, thread);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                VotePost vp  = new VotePost();
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
    public String details(Integer post, String related) {
        connection =  ConnectionToMySQL.getConnection();

        String sqlSel = "SELECT * FROM " + Table.Post.TABLE_POST + "INNER JOIN " +
                Table.VotePost.TABLE_VOTE_POST + " ON " +
                Table.Post.COLUMN_ID_POST + "="+ Table.VotePost.COLUMN_ID_POST + " AND " +
                Table.VotePost.COLUMN_ID_POST + "=?;";

        DetailPost<Object, Object, Object> postDetail = new DetailPost<>();
        UserDetails userDetails = new UserDetails();
        ThreadVote threadVote = new ThreadVote();
//        VotePost votePost = new VotePost();

        this.votePost = null;

        try {
            preparedStatement = connection.prepareStatement(sqlSel);
            preparedStatement.setLong(1, post.intValue());
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
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

                if(!StringUtils.isEmpty(related) && related.contains("forum")) {
                    ForumServiceImpl fsi = new ForumServiceImpl();
                    postDetail.setForum( fsi.getForum((String)postDetail.getForum()));
                }

                if(!StringUtils.isEmpty(related) && related.contains("thread")) {
//                    ThreadServiceImpl tsi = new ThreadServiceImpl();
                    postDetail.setThread(threadService.getThreadDetatils((Integer)postDetail.getThread(), null));
                }

                if(!StringUtils.isEmpty(related) && related.contains("user")) {
                    UserServiceImpl usi = new UserServiceImpl();
                    usi.getUserDetatil((String)postDetail.getUser());
                    postDetail.setUser(usi.getUserDetatil((String)postDetail.getUser()));
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

        if(postDetail.getid() == null)
            return ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.NOT_FOUND.ordinal(),
                    ResponseStatus.FORMAT_JSON);

        this.votePost = postDetail;

        String json = (new ResultJson<DetailPost<Object, Object, Object>>(
                ResponseStatus.ResponceCode.OK.ordinal(), postDetail)).getStringResult();
        return json;
    }

    private String getPath(Integer parent, Integer idPost) {
        connection =  ConnectionToMySQL.getConnection();

        if(parent == null)
            return Integer.toString(idPost.intValue());

        String matPath = "";

        String sql = " Select * FROM " + Table.Post.TABLE_POST +
                " WHERE " + Table.Post.COLUMN_ID_POST + "=?";

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, parent.intValue());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                matPath = resultSet.getString("path");
            }
            matPath = matPath + "." + idPost.intValue();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return matPath;
    }



    @Override
    public void close() throws Exception {
        preparedStatement.close();
        connection.close();
    }
}
