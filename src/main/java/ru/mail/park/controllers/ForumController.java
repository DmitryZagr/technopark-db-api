package ru.mail.park.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.forum.Forum;
import ru.mail.park.service.implementation.ForumServiceImpl;
import ru.mail.park.service.interfaces.IForumService;

/**
 * Created by admin on 08.10.16.
 */
@RestController
public class ForumController {

    private final IForumService forumService;

    @Autowired
    public ForumController(ForumServiceImpl forumService) {
        this.forumService = forumService;
    }


    @RequestMapping(path = "/db/api/forum/create/", method = RequestMethod.POST,
                                         produces = "application/json")
    public ResponseEntity create(@RequestBody Forum forum) {
        return ResponseEntity.ok(forumService.create(forum));
    }

    @RequestMapping(path = "/db/api/forum/listUsers/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listFollowers (
            @RequestParam(name = "forum") String forum,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "order", required = false) String order,
            @RequestParam(name = "since_id", required = false) Integer since_id) {
        return ResponseEntity.ok(forumService.listUsers(forum, limit, order, since_id));
    }

    @RequestMapping(path = "/db/api/forum/listThreads/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listThreads(@RequestParam(name = "forum") String forum,
                                      @RequestParam(name = "since", required = false) String since,
                                      @RequestParam(name = "limit", required = false) Integer limit,
                                      @RequestParam(name = "order", required = false) String order,
                                      @RequestParam(name = "related", required = false) String related) {
        return ResponseEntity.ok(forumService.listThreads(forum, since, limit, order, related));
    }

    @RequestMapping(path = "/db/api/forum/details/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity details (@RequestParam(name = "forum") String forum,
                                   @RequestParam(name = "related", required = false) String related) {
        return ResponseEntity.ok(forumService.details(forum, related));
    }

    @RequestMapping(path = "/db/api/forum/listPosts/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listPosts(@RequestParam(name = "forum") String forum,
                                      @RequestParam(name = "since", required = false) String since,
                                      @RequestParam(name = "limit", required = false) Integer limit,
                                      @RequestParam(name = "order", required = false) String order,
                                      @RequestParam(name = "related", required = false) String related) {
        return ResponseEntity.ok(forumService.listPosts(forum, since, limit, order, related));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseBody
    public String resolveException() {
        return ResponseStatus.getMessage(
                ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ResponseStatus.FORMAT_JSON
        );
    }

    @ExceptionHandler({NoHandlerFoundException.class})
    @ResponseBody
    public String resolve404Exception() {
        return
                ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                        ResponseStatus.FORMAT_JSON
                );
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseBody
    public String resolve400Exception() {
        return
                ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                        ResponseStatus.FORMAT_JSON
                );
    }


    @ExceptionHandler({NumberFormatException.class})
    @ResponseBody
    public String resolveNumberFormatExceptionException() {
        return ResponseStatus.getMessage(
                ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ResponseStatus.FORMAT_JSON
        );
    }

}
