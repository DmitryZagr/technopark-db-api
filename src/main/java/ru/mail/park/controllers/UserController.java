package ru.mail.park.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.mail.park.model.User;
import ru.mail.park.service.implementation.UserServiceImpl;
import ru.mail.park.service.interfaces.IUserService;

import javax.servlet.http.HttpSession;

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
    public ResponseEntity createUser(@ModelAttribute("user") User user) {
        int code = userService.create(user);
        String respStr =
                "{" +
                    "\"code\":" + code + "," +
                    "\"response\":{" + user.toString() +
                "} }";
        return ResponseEntity.ok(respStr);
    }
}
