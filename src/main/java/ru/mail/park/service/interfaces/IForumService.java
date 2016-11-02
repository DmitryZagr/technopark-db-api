package ru.mail.park.service.interfaces;


import org.springframework.stereotype.Component;
import ru.mail.park.model.forum.Forum;

/**
 * Created by admin on 08.10.16.
 */
@Component
public interface IForumService {
    String create(Forum forum);//+
    String details(String forum, String related);//+
    String listPosts(String forum, String since, Integer limit,
                     String order, String related);
    String listThreads(String forum, String since,
                       Integer limit, String order, String related); //+
    String listUsers(String forum, Integer limit,
                     String order, Integer since_id);//+
}
