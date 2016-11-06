package ru.mail.park.service.interfaces;

import org.springframework.stereotype.Component;
import ru.mail.park.model.user.User;

/**
 * Created by admin on 08.10.16.
 */
@Component
public interface IUserService {
    String create(User ucr); //+
    String details(String email);//+
    String follow(String followerFollowee); //+
    String listFollowers(String user, Integer limit, String order, Integer id);//+
    String listFollowing(String user, Integer limit, String order, Integer id);//+
    String listPosts(String user, String since, Integer limit, String order);//+
    String unFollow(String followerFollowee);//+
    String updateProfile(String json); //+
}
