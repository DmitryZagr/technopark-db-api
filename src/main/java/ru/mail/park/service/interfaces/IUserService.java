package ru.mail.park.service.interfaces;

import org.springframework.stereotype.Component;
import ru.mail.park.model.User;

/**
 * Created by admin on 08.10.16.
 */
@Component
public interface IUserService {
    int create(User ucr);
}
