package ru.mail.park.service.interfaces;

import org.springframework.stereotype.Component;
import ru.mail.park.model.post.Post;
import ru.mail.park.model.post.IdPost;
import ru.mail.park.model.post.VotePost;

/**
 * Created by admin on 08.10.16.
 */
@Component
public interface IPostService {
    String create(Post post);
    String removeOrRestore(IdPost post, boolean isDel);
    String vote(String votePost);
    String update(String update);
    String list(String forum, Long thread, String since, Long limit, String order);
}
