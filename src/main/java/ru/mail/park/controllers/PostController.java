package ru.mail.park.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.mail.park.api.status.*;
import ru.mail.park.model.Post;
import ru.mail.park.service.implementation.PostServiceImpl;
import ru.mail.park.service.interfaces.IPostService;

/**
 * Created by admin on 09.10.16.
 */
@RestController
public class PostController  {
    private final IPostService postService;

    @Autowired
    public PostController(PostServiceImpl postService) {
        this.postService = postService;
    }

    @RequestMapping(path = "/db/api/post/create/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity createUser(@RequestBody Post post) {
        return ResponseEntity.ok(postService.create(post));
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