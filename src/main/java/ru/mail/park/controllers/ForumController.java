package ru.mail.park.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.mail.park.api.status.*;
import ru.mail.park.model.forum.Forum;
import ru.mail.park.service.implementation.ForumServiceImpl;
import ru.mail.park.service.interfaces.IForumService;
import ru.mail.park.util.MyJsonUtils;

import java.io.IOException;

/**
 * Created by admin on 08.10.16.
 */
@RestController
public class ForumController {

    private final IForumService forumService;
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ForumController(ForumServiceImpl forumService) {
        this.forumService = forumService;
    }


    @RequestMapping(path = "/db/api/forum/create/", method = RequestMethod.POST,
                                         produces = "application/json")
    public ResponseEntity create(HttpEntity<String> httpEntity) {
        String json = httpEntity.getBody();
        json = MyJsonUtils.replaceOneQuoteTwoQuotes(json);
        ObjectNode root = null;
        Forum forum = new Forum();
        try {
            root = (ObjectNode) mapper.readTree(json);
            forum.setName(root.get("name").asText());
            forum.setShort_name(root.get("short_name").asText());
            forum.setUser(root.get("user").asText());
        } catch (NullPointerException e) {
            return ResponseEntity.ok(ru.mail.park.api.status.ResponseStatus.getMessage(
                    ru.mail.park.api.status.ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                    ru.mail.park.api.status.ResponseStatus.FORMAT_JSON));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(forumService.create(forum));
    }

    @RequestMapping(path = "/db/api/forum/listUsers/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listFollowers (
            @RequestParam(name = "forum",  required = true) String forum,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "order", required = false) String order,
            @RequestParam(name = "since_id", required = false) Integer since_id) {
        return ResponseEntity.ok(forumService.listUsers(forum, limit, order, since_id));
    }

    @RequestMapping(path = "/db/api/forum/listThreads/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listThreads(@RequestParam(name = "forum", required = true) String forum,
                                      @RequestParam(name = "since", required = false) String since,
                                      @RequestParam(name = "limit", required = false) Integer limit,
                                      @RequestParam(name = "order", required = false) String order,
                                      @RequestParam(name = "related", required = false) String related) {
        return ResponseEntity.ok(forumService.listThreads(forum, since, limit, order, related));
    }

    @RequestMapping(path = "/db/api/forum/details/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity details (@RequestParam(name = "forum", required = true) String forum,
                                   @RequestParam(name = "related", required = false) String related) {
        return ResponseEntity.ok(forumService.details(forum, related));
    }

    @RequestMapping(path = "/db/api/forum/listPosts/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listPosts(@RequestParam(name = "forum", required = true) String forum,
                                      @RequestParam(name = "since", required = false) String since,
                                      @RequestParam(name = "limit", required = false) Integer limit,
                                      @RequestParam(name = "order", required = false) String order,
                                      @RequestParam(name = "related", required = false) String related) {
        return ResponseEntity.ok(forumService.listPosts(forum, since, limit, order, related));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseBody
    public String resolveException() {
        return ru.mail.park.api.status.ResponseStatus.getMessage(
                ru.mail.park.api.status.ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ru.mail.park.api.status.ResponseStatus.FORMAT_JSON
        );
    }


}
