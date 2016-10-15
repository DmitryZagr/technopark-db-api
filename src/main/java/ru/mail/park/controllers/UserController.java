package ru.mail.park.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.user.User;
import ru.mail.park.service.implementation.UserServiceImpl;
import ru.mail.park.service.interfaces.IUserService;
import ru.mail.park.util.MyJsonUtils;

import java.io.IOException;


/**
 * Created by admin on 08.10.16.
 */
@RestController
public class UserController {
    private ObjectMapper mapper = new ObjectMapper();

    private final IUserService userService;

    @Autowired
    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @RequestMapping(path = "/db/api/user/create/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity createUser(HttpEntity<String> httpEntity) {
        String json = httpEntity.getBody();
        json = MyJsonUtils.replaceOneQuoteTwoQuotes(json);
        ObjectNode root = null;
        User user = new User();
        try {
            root = (ObjectNode) mapper.readTree(json);
            user.setUsername(root.get("username").asText());
            user.setAbout(root.get("about").asText());
            user.setName(root.get("name").asText());
            user.setEmail(root.get("email").asText());
            if(json.contains("isAnonymous"))
                user.setisAnonymous(root.get("isAnonymous").asBoolean());
        } catch (NullPointerException e) {
            return ResponseEntity.ok(ResponseStatus.getMessage(
                    ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                    ResponseStatus.FORMAT_JSON));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            @RequestParam(name = "user",  required = true) String user) {
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
            @RequestParam(name = "user",  required = true) String user,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "order", required = false) String order,
            @RequestParam(name = "since_id", required = false) Integer since_id) {
        return ResponseEntity.ok(userService.listFollowers(user, limit, order, since_id));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class})
    @ResponseBody
    public String resolveException() {
        return ResponseStatus.getMessage(
                ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ResponseStatus.FORMAT_JSON
        );
    }
}
