package ru.mail.park.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import ru.mail.park.util.MyJsonUtils;

import java.io.IOException;

import static com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundOperation.ANONYMOUS.required;

/**
 * Created by admin on 09.10.16.
 */
@RestController
public class PostController {
    private final IPostService postService;
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public PostController(PostServiceImpl postService) {
        this.postService = postService;
    }

    @RequestMapping(path = "/db/api/post/create/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity createPost(HttpEntity<String> httpEntity) throws IOException {
        String json = httpEntity.getBody();
//        json = MyJsonUtils.replaceOneQuoteTwoQuotes(json);
        ObjectNode root = null;
        Post post = new Post();
        root = (ObjectNode) mapper.readTree(json);
        post.setDate(root.get("date").asText());
        post.setThread(root.get("thread").asInt());
        post.setMessage(root.get("message").asText());
        post.setUser(root.get("user").asText());
        post.setForum(root.get("forum").asText());
        if(root.has("parent")) post.setParent((Integer) root.get("parent").numberValue());
        if(root.has("isApproved")) post.setApproved(root.get("isApproved").asBoolean());
        if(root.has("isHighlighted"))post.setHighlighted(root.get("isHighlighted").asBoolean());
        if(root.has("isEdited"))post.setEdited(root.get("isEdited").asBoolean());
        if(root.has("isSpam"))post.setSpam(root.get("isSpam").asBoolean());
        if(root.has("isDeleted"))post.setDeleted(root.get("isDeleted").asBoolean());

        return ResponseEntity.ok(postService.create(post));
    }

    @RequestMapping(path = "/db/api/post/remove/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity removePost(HttpEntity<String> httpEntity) throws IOException {
        return ResponseEntity.ok(postService.removeOrRestore(
                (new IdPost(httpEntity.getBody())), true));
    }

    @RequestMapping(path = "/db/api/post/restore/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity restorePost(HttpEntity<String> httpEntity) throws IOException {
        return ResponseEntity.ok(postService.removeOrRestore(
                (new IdPost(httpEntity.getBody())), false));
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

    @RequestMapping(path = "/db/api/post/details/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity detailsPost(@RequestParam(name = "post", required = true) Integer post,
                                   @RequestParam(name = "related", required = false) String related) {
        return ResponseEntity.ok(postService.details(post, related));
    }


    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseBody
    public String resolveException() {
        return ru.mail.park.api.status.ResponseStatus.getMessage(
                ru.mail.park.api.status.ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ru.mail.park.api.status.ResponseStatus.FORMAT_JSON
        );
    }

    @ExceptionHandler({NullPointerException.class})
    @ResponseBody
    public String resolveNUllException() {
        return ru.mail.park.api.status.ResponseStatus.getMessage(
                ru.mail.park.api.status.ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ru.mail.park.api.status.ResponseStatus.FORMAT_JSON
        );
    }

    @ExceptionHandler({IOException.class})
    @ResponseBody
    public String resolveIOException() {
        return ru.mail.park.api.status.ResponseStatus.getMessage(
                ru.mail.park.api.status.ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ru.mail.park.api.status.ResponseStatus.FORMAT_JSON
        );
    }
}