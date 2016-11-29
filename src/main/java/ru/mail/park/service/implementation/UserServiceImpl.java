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

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 08.10.16.
 */
@Component
@Transactional
public class UserServiceImpl implements IUserService, AutoCloseable {

//    private final ObjectMapper mapper = new ObjectMapper();

//    private ArrayList<UserDetails> usersDetail;
//    private UserDetails userDetails;

    @Autowired
    private DataSource dataSource;

    @Override
    public String create(User user) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sqlInsert = "INSERT INTO " + Table.User.TABLE_USER + " ( " +
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

        return (new ResultJson<User>(
                ResponseStatus.ResponceCode.OK.ordinal(), user)).getStringResult();
    }

    @Override
    public String follow(String followerFollowee) {
//        connection =  ConnectionToMySQL.getConnection();
        final String follower;
        final String followee;

        try {
            ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = (ObjectNode) mapper.readTree(followerFollowee);
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

        final String sql = "INSERT INTO " + Table.Followers.TABLE_FOLLOWERS +
                " (" + Table.Followers.COLUMN_FOLLOWER + ", " + Table.Followers.COLUMN_FOLLOWEE +
                ") VALUES (?, ?);" ;

        final Connection connection = DataSourceUtils.getConnection(dataSource);


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

        String sqlSelectFollowers = "SELECT  " + Table.Followers.COLUMN_FOLLOWER   +
                "  FROM " + Table.Followers.TABLE_FOLLOWERS +
                " INNER JOIN " + Table.User.TABLE_USER +
                " ON " + Table.User.COLUMN_EMAIL + '=' + Table.Followers.COLUMN_FOLLOWER +
                " AND " + Table.Followers.COLUMN_FOLLOWEE + "=?";

        if(sinceId != null)
        sqlSelectFollowers =  sqlSelectFollowers +
               " AND  " + Table.User.COLUMN_ID_USER + ">=" + sinceId.intValue() + ' ';
//
        if(order == null) order = "DESC";
        sqlSelectFollowers = sqlSelectFollowers + " ORDER BY " + Table.User.COLUMN_NAME + ' ' + order;
//
        if(limit != null)
        sqlSelectFollowers = sqlSelectFollowers +  " LIMIT " + limit.intValue();

        final ArrayList<String> followers = new ArrayList<>();

        final Connection connection = DataSourceUtils.getConnection(dataSource);


        ArrayList<UserDetails> usersDetail = null;
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
            usersDetail.add(this.getUserDetail(followers.get(i)));
//            details(followers.get(i));
        }

        final String json = (new ResultJson<ArrayList<UserDetails>>(
                ResponseStatus.ResponceCode.OK.ordinal(), usersDetail)).getStringResult();
        usersDetail = null;
        return json;
    }

    @Override
    public String listFollowing(String user, Integer limit, String order, Integer sinceId) {
//        connection =  ConnectionToMySQL.getConnection();

        String sqlSelectFollowing = "SELECT " + Table.Followers.COLUMN_FOLLOWEE +
                "  FROM " + Table.Followers.TABLE_FOLLOWERS +
                " INNER JOIN " + Table.User.TABLE_USER +
                " ON " + Table.User.COLUMN_EMAIL + '=' + Table.Followers.COLUMN_FOLLOWER +
                " AND " + Table.Followers.COLUMN_FOLLOWER + "=?";

        if(sinceId != null)
            sqlSelectFollowing =  sqlSelectFollowing +
                    " AND  " + Table.User.COLUMN_ID_USER + ">=" + sinceId.intValue() + ' ';
//
        if(order == null) order = "DESC";
        sqlSelectFollowing = sqlSelectFollowing + " ORDER BY " + Table.User.COLUMN_NAME + ' ' + order;
//
        if(limit != null)
            sqlSelectFollowing = sqlSelectFollowing +  " LIMIT " + limit.intValue();

        final Connection connection = DataSourceUtils.getConnection(dataSource);


        final ArrayList<String> followers = new ArrayList<>();
        ArrayList<UserDetails> usersDetail = null;
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
            usersDetail.add(getUserDetail(followers.get(i)));
//            details(followers.get(i));
        }

        final String json = (new ResultJson<ArrayList<UserDetails>>(
                ResponseStatus.ResponceCode.OK.ordinal(), usersDetail)).getStringResult();
        usersDetail = null;
        return json;
    }

    public String getUserDetailsListJSON(ArrayList<String> emails) {
        ArrayList<UserDetails> usersDetail = new ArrayList<>();
        for(int i =0; i < emails.size(); i++)
            usersDetail.add(getUserDetail(emails.get(i)));
//            details(emails.get(i));
        final String json = (new ResultJson<ArrayList<UserDetails>>(
                ResponseStatus.ResponceCode.OK.ordinal(), usersDetail)).getStringResult();
        usersDetail = null;
        return json;
    }

    @Override
    public String listPosts(String user, String since, Integer limit, String order) {
//        connection =  ConnectionToMySQL.getConnection();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String  dateContidion;
        final String orderCondition;
        final String limitCondition;

        String sqlSel = "SELECT "
                + Table.Post.COLUMN_ID_POST       + ", " + Table.Post.COLUMN_FORUM       + ", "
                + Table.Post.COLUMN_DATE          + ", " + Table.Post.COLUMN_IS_APPROVED + ", "
                + Table.Post.COLUMN_IS_DELETED    + ", " + Table.Post.COLUMN_IS_EDITED   + ", "
                + Table.Post.COLUMN_IS_HIGHLIGHED + ", " + Table.Post.COLUMN_IS_SPAM     + ", "
                + Table.Post.COLUMN_MESSAGE       + ", " + Table.Post.COLUMN_PARENT      + ", "
                + Table.Post.COLUMN_THREAD        + ", " + Table.Post.COLUMN_THREAD      + ", "
                + Table.Post.COLUMN_USER          + ", " + Table.Post.COLUMN_LIKE    + ", "
                + Table.Post.COLUMN_DISLIKE   + " " +
                " FROM " + Table.Post.TABLE_POST  + " WHERE"
                + Table.Post.COLUMN_USER + "=? ";
        dateContidion = (since != null) ?  " AND " + Table.Post.COLUMN_DATE + ">=\'" + since + '\'' : " ";
        sqlSel = sqlSel + dateContidion;

        orderCondition = (order != null) ? " ORDER BY " + Table.Post.COLUMN_DATE +
                ' ' + order + ' ' : " ORDER BY " + Table.Post.COLUMN_DATE + " DESC ";
        sqlSel = sqlSel + orderCondition;

        limitCondition = (limit != null) ? " LIMIT "+ limit.longValue()  : " ";
        sqlSel = sqlSel +  limitCondition;

        final ArrayList<VotePost> votePosts = new ArrayList<>();

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlSel)) {
            preparedStatement.setString(1, user);
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

        return (new ResultJson<ArrayList<VotePost>>(
                ResponseStatus.ResponceCode.OK.ordinal(), votePosts)).getStringResult();
    }

    @Override
    public String unFollow(String followerFollowee) {
//        connection =  ConnectionToMySQL.getConnection();

        final String follower;
        final String followee;

        try {
            ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = (ObjectNode) mapper.readTree(followerFollowee);
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

        final String sqlUnFollow = "DELETE FROM " + Table.Followers.TABLE_FOLLOWERS + " WHERE " +
                Table.Followers.COLUMN_FOLLOWER + "=? AND " + Table.Followers.COLUMN_FOLLOWEE + "=?;";

        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlUnFollow)) {
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
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sqlUserDetatils = "SELECT `forum`.`User`.`idUser`, \n" +
                "\t\t`forum`.`User`.`username`, \n" +
                "\t\t`forum`.`User`.`about`, \n" +
                "\t\t`forum`.`User`.`name`, \n" +
                "        `forum`.`User`.`email`,\n" +
                "         group_concat(Followers.`follower`) as followers,\n" +
                "         group_concat(distinct Followee.`followee`) as followee,\n" +
                "         group_concat(distinct `forum`.`ThreadSubscribe`.`thread`) as subscriptions,\n" +
                "        `forum`.`User`.`isAnonymous`  \n" +
                "FROM `forum`.`User`\n" +
                "LEFT JOIN `forum`.`Followers` as Followers on\n" +
                "  `forum`.`User`.`email` = Followers.`followee`\n" +
                "  \n" +
                "LEFT JOIN `forum`.`Followers` as Followee on \n" +
                "  `forum`.`User`.`email` = Followee.`follower`\n" +
                "  \n" +
                "LEFT JOIN `forum`.`ThreadSubscribe` ON \n" +
                "\t`forum`.`User`.`email` = `forum`.`ThreadSubscribe`.`user`\n" +
                "  \n" +
                "  where `forum`.`User`.`email`=?\n" +
                "  \n" +
                "group by `forum`.`User`.`email`;" ;

        UserDetails userDetails = new UserDetails();

        userDetails.setEmail(email);

        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlUserDetatils)) {
                preparedStatement.setString(1, email);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        userDetails.setId(resultSet.getLong("idUser"));
                        userDetails.setUsername(resultSet.getString("username"));
                        userDetails.setAbout(resultSet.getString("about"));
                        userDetails.setName(resultSet.getString("name"));
                        userDetails.setisAnonymous(resultSet.getBoolean("isAnonymous"));

                        String strSubs;
                        if((strSubs = resultSet.getString("subscriptions")) != null){
                            List<String> subscriptions = Arrays.asList(strSubs.split("\\s*,\\s*"));
                            for(int i = 0; i < subscriptions.size(); i++) {
                                userDetails.getSubscriptions().add(Integer.parseInt(subscriptions.get(i)));

                            }
                        }

                        String strFollowers;
                        if((strFollowers = resultSet.getString("followers")) != null){
                            List<String> followers = Arrays.asList(strFollowers.split("\\s*,\\s*"));
                            userDetails.setFollowers(followers);
                        }

                        String strFollowee;
                        if((strFollowee = resultSet.getString("followee")) != null){
                            List<String> followee = Arrays.asList(strFollowee.split("\\s*,\\s*"));
                            userDetails.setFollowing(followee);
                        }

                    }
                }
            }
        }catch (NullPointerException e) {
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(),
                        ResponseStatus.FORMAT_JSON);
            } catch (MySQLSyntaxErrorException e) {
                return ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.INVALID_REQUEST.ordinal(), ResponseStatus.FORMAT_JSON);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return (new ResultJson<UserDetails>(
                    ResponseStatus.ResponceCode.OK.ordinal(), userDetails)).getStringResult();
    }

    @Override
    public String updateProfile(String json) {
//        connection =  ConnectionToMySQL.getConnection();

        final String sql = "UPDATE " + Table.User.TABLE_USER +
                " SET " + Table.User.COLUMN_ABOUT +
                " =?, " + Table.User.COLUMN_NAME + "=?  WHERE " + Table.User.COLUMN_EMAIL + "=?;";

        final String about;
        final String user;
        final String name;

        try {
            ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = (ObjectNode) mapper.readTree(json);
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
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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

    public UserDetails getUserDetail(String email) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        final String sqlUserDetatils = "SELECT `forum`.`User`.`idUser`, \n" +
                "\t\t`forum`.`User`.`username`, \n" +
                "\t\t`forum`.`User`.`about`, \n" +
                "\t\t`forum`.`User`.`name`, \n" +
                "        `forum`.`User`.`email`,\n" +
                "         group_concat(Followers.`follower`) as followers,\n" +
                "         group_concat(distinct Followee.`followee`) as followee,\n" +
                "         group_concat(distinct `forum`.`ThreadSubscribe`.`thread`) as subscriptions,\n" +
                "        `forum`.`User`.`isAnonymous`  \n" +
                "FROM `forum`.`User`\n" +
                "LEFT JOIN `forum`.`Followers` as Followers on\n" +
                "  `forum`.`User`.`email` = Followers.`followee`\n" +
                "  \n" +
                "LEFT JOIN `forum`.`Followers` as Followee on \n" +
                "  `forum`.`User`.`email` = Followee.`follower`\n" +
                "  \n" +
                "LEFT JOIN `forum`.`ThreadSubscribe` ON \n" +
                "\t`forum`.`User`.`email` = `forum`.`ThreadSubscribe`.`user`\n" +
                "  \n" +
                "  where `forum`.`User`.`email`=?\n" +
                "  \n" +
                "group by `forum`.`User`.`email`;" ;

        UserDetails userDetails = new UserDetails();

        userDetails.setEmail(email);

        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlUserDetatils)) {
                preparedStatement.setString(1, email);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        userDetails.setId(resultSet.getLong("idUser"));
                        userDetails.setUsername(resultSet.getString("username"));
                        userDetails.setAbout(resultSet.getString("about"));
                        userDetails.setName(resultSet.getString("name"));
                        userDetails.setisAnonymous(resultSet.getBoolean("isAnonymous"));

                        String strSubs;
                        if((strSubs = resultSet.getString("subscriptions")) != null){
                            List<String> subscriptions = Arrays.asList(strSubs.split("\\s*,\\s*"));
                            for(int i = 0; i < subscriptions.size(); i++) {
                                userDetails.getSubscriptions().add(Integer.parseInt(subscriptions.get(i)));

                            }
                        }

                        String strFollowers;
                        if((strFollowers = resultSet.getString("followers")) != null){
                            List<String> followers = Arrays.asList(strFollowers.split("\\s*,\\s*"));
                            userDetails.setFollowers(followers);
                        }

                        String strFollowee;
                        if((strFollowee = resultSet.getString("followee")) != null){
                            List<String> followee = Arrays.asList(strFollowee.split("\\s*,\\s*"));
                            userDetails.setFollowing(followee);
                        }

                    }
                }
            }
        }catch (NullPointerException e) {
            e.printStackTrace();
        } catch (MySQLSyntaxErrorException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userDetails;

    }

    @Override
    public void close() throws Exception {
//        statement.close();
//        resultSet.close();
//        preparedStatement.close();
    }
}
