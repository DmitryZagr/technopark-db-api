package ru.mail.park.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.model.post.Post;
import ru.mail.park.model.post.IdPost;
import ru.mail.park.model.post.VotePost;
import ru.mail.park.service.implementation.PostServiceImpl;
import ru.mail.park.service.interfaces.IPostService;

import static com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundOperation.ANONYMOUS.required;

/**
 * Created by admin on 09.10.16.
 */
@RestController
public class PostController {
    private final IPostService postService;

    @Autowired
    public PostController(PostServiceImpl postService) {
        this.postService = postService;
    }

    @RequestMapping(path = "/db/api/post/create/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity createPost(@RequestBody Post post) {
        return ResponseEntity.ok(postService.create(post));
    }

    @RequestMapping(path = "/db/api/post/remove/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity removePost(@RequestBody IdPost idpost) {
        return ResponseEntity.ok(postService.removeOrRestore(idpost, true));
    }

    @RequestMapping(path = "/db/api/post/restore/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity restorePost(@RequestBody IdPost idpost) {
        return ResponseEntity.ok(postService.removeOrRestore(idpost, false));
    }

    @RequestMapping(path = "/db/api/post/vote/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity votePost(HttpEntity<String> httpEntity) {
        return ResponseEntity.ok(postService.vote(httpEntity.getBody()));
    }

    @RequestMapping(path = "/db/api/post/update/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity updatePost(HttpEntity<String> httpEntity) {
        return ResponseEntity.ok(postService.update(httpEntity.getBody()));
    }

    @RequestMapping(path = "/db/api/post/list/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listPost(@RequestParam(name = "thread", required = false) Long thread,
                                   @RequestParam(name = "forum", required = false) String forum,
                                   @RequestParam(name = "since", required = false) String since,
                                   @RequestParam(name = "limit", required = false) Long limit,
                                   @RequestParam(name = "order", required = false) String order) {


        return ResponseEntity.ok(postService.list(forum, thread, since, limit, order));
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