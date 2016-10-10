package ru.mail.park.service.interfaces;

import org.springframework.stereotype.Component;
import ru.mail.park.model.Post;

/**
 * Created by admin on 08.10.16.
 */
@Component
public interface IPostService {
    String create(Post post);
}
