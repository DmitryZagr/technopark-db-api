package ru.mail.park.service.implementation;

import org.springframework.stereotype.Component;
import ru.mail.park.model.User;
import ru.mail.park.service.interfaces.IUserService;

/**
 * Created by admin on 08.10.16.
 */
@Component
public class UserServiceImpl implements IUserService{
    @Override
    public int create(User ucr) {
        ucr.setAbout("about");
        ucr.setEmail("email");
        ucr.setId(1);
        ucr.setAnonymous(true);
        ucr.setName("Jonn");
        ucr.setUsername("user1");
        return 0;
    }
}
