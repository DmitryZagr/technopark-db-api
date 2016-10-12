package ru.mail.park.service.interfaces;


import org.springframework.stereotype.Component;
import ru.mail.park.model.forum.Forum;

/**
 * Created by admin on 08.10.16.
 */
@Component
public interface IForumService {
    String create(Forum forum);
//    String remove();
}
