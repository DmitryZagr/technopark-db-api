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
    String create(Thread thread); //+
    String update(Thread thread);

    String subscribeUnSub(ThreadSubscribe threadSubscribe, boolean subs); //+
    String open(ThreadID thread); //+
    String close(ThreadID tread); //+
    String remove(ThreadID thread);//+
    String restore(ThreadID thread);//+
    String vote (String votePost); //+
}
