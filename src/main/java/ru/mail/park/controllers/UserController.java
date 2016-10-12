package ru.mail.park.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.user.User;
import ru.mail.park.service.implementation.UserServiceImpl;
import ru.mail.park.service.interfaces.IUserService;


/**
 * Created by admin on 08.10.16.
 */
@RestController
public class UserController {

    private final IUserService userService;

    @Autowired
    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/db/api/user/create/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity createUser(@RequestBody User user) {
        String status;
        return ResponseEntity.ok(userService.create(user));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseBody
    public String resolveException() {
        return ResponseStatus.getMessage(
                ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ResponseStatus.FORMAT_JSON
        );
    }
}
