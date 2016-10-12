package ru.mail.park.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
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

    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseBody
    public String resolveException() {
        return ru.mail.park.api.status.ResponseStatus.getMessage(
                ru.mail.park.api.status.ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ru.mail.park.api.status.ResponseStatus.FORMAT_JSON
        );
    }

}
