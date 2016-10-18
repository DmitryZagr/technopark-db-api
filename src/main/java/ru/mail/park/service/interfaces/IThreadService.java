package ru.mail.park.service.interfaces;

import org.springframework.stereotype.Component;
import ru.mail.park.model.post.VotePost;
import ru.mail.park.model.thread.ThreadID;
import ru.mail.park.model.thread.ThreadSubscribe;
import ru.mail.park.model.thread.Thread;

/**
 * Created by admin on 08.10.16.
 */
@Component
public interface IThreadService {
    String close(ThreadID tread); //+
    String create(Thread thread); //+
    String details(Integer thread, String related);//+
    String list(String user, String forum, String since, Integer limit, String order);//+
    String listPosts(Integer thread, String since, Integer limit, String sort, String order);
    String open(ThreadID thread); //+
    String remove(ThreadID thread);//+
    String restore(ThreadID thread);//+
    String subscribeUnSub(ThreadSubscribe threadSubscribe, boolean subs); //+
    String update(String json); //+
    String vote (String votePost); //+
}
