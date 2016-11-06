package ru.mail.park.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        return ResponseEntity.ok(userService.create(user));
    }

    @RequestMapping(path = "/db/api/user/follow/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity followUser(HttpEntity<String> httpEntity) {
        return ResponseEntity.ok(userService.follow(httpEntity.getBody()));
    }

    @RequestMapping(path = "/db/api/user/unfollow/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity unfollowUser(HttpEntity<String> httpEntity) {
        return ResponseEntity.ok(userService.unFollow(httpEntity.getBody()));
    }

    @RequestMapping(path = "/db/api/user/details/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity detailUser(
            @RequestParam(name = "user") String user) {
        return ResponseEntity.ok(userService.details(user));
    }

    @RequestMapping(path = "/db/api/user/updateProfile/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity updateUserProfile(HttpEntity<String> httpEntity) {
        return ResponseEntity.ok(userService.updateProfile(httpEntity.getBody()));
    }

    @RequestMapping(path = "/db/api/user/listFollowers/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listFollowers (
            @RequestParam(name = "user") String user,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "order", required = false) String order,
            @RequestParam(name = "since_id", required = false) Integer since_id) {
        return ResponseEntity.ok(userService.listFollowers(user, limit, order, since_id));
    }

    @RequestMapping(path = "/db/api/user/listFollowing/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listFollowing (
            @RequestParam(name = "user") String user,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "order", required = false) String order,
            @RequestParam(name = "since_id", required = false) Integer since_id) {
        return ResponseEntity.ok(userService.listFollowing(user, limit, order, since_id));
    }

    @RequestMapping(path = "/db/api/user/listPosts/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listPosts (
            @RequestParam(name = "user") String user,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "order", required = false) String order,
            @RequestParam(name = "since", required = false) String since) {
        return ResponseEntity.ok(userService.listPosts(user, since, limit, order));
    }

}

