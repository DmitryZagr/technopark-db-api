package ru.mail.park.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.model.thread.Thread;
import ru.mail.park.model.thread.ThreadID;
import ru.mail.park.model.thread.ThreadSubscribe;
import ru.mail.park.service.implementation.ThreadServiceImpl;
import ru.mail.park.service.interfaces.IThreadService;

import java.io.IOException;

/**
 * Created by admin on 11.10.16.
 */
@RestController
public class ThreadController {
    private final IThreadService threadService;

    @Autowired
    public ThreadController(ThreadServiceImpl threadService){
        this.threadService = threadService;
    }

    @RequestMapping(path = "/db/api/thread/create/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity createThread(HttpEntity<String> httpEntity) throws IOException {
        return ResponseEntity.ok(threadService.create((new Thread(httpEntity.getBody()))));
    }

    @RequestMapping(path = "/db/api/thread/update/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity updateThread(HttpEntity<String> httpEntity) {
        final String json = httpEntity.getBody();
        return ResponseEntity.ok(threadService.update(json));
    }

    @RequestMapping(path = "/db/api/thread/subscribe/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity SubscribeThread(HttpEntity<String> httpEntity) throws IOException {
        return ResponseEntity.ok(threadService.subscribeUnSub(
                (new ThreadSubscribe(httpEntity.getBody())), true));
    }

    @RequestMapping(path = "/db/api/thread/unsubscribe/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity unSubscribeThread(HttpEntity<String> httpEntity) throws IOException {
        return ResponseEntity.ok(threadService.subscribeUnSub(
                (new ThreadSubscribe(httpEntity.getBody())), false));
    }

    @RequestMapping(path = "/db/api/thread/open/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity openThread(HttpEntity<String> httpEntity) throws IOException {
        return ResponseEntity.ok(threadService.open((new ThreadID(httpEntity.getBody()))));
    }

    @RequestMapping(path = "/db/api/thread/close/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity closeThread(HttpEntity<String> httpEntity) throws IOException {
        return ResponseEntity.ok(threadService.close((new ThreadID(httpEntity.getBody()))));
    }

    @RequestMapping(path = "/db/api/thread/remove/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity removeThread(HttpEntity<String> httpEntity) throws IOException {
        return ResponseEntity.ok(threadService.remove((new ThreadID(httpEntity.getBody()))));
    }

    @RequestMapping(path = "/db/api/thread/restore/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity restoreThread(HttpEntity<String> httpEntity) throws IOException {
        return ResponseEntity.ok(threadService.restore((new ThreadID(httpEntity.getBody()))));
    }

    @RequestMapping(path = "/db/api/thread/vote/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity voteThread(HttpEntity<String> httpEntity) {
        return ResponseEntity.ok(threadService.vote(httpEntity.getBody()));
    }

    @RequestMapping(path = "/db/api/thread/list/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listThread(@RequestParam(name = "user",required = false) String user,
                                   @RequestParam(name = "forum", required = false) String forum,
                                   @RequestParam(name = "since", required = false) String since,
                                   @RequestParam(name = "limit", required = false) Integer limit,
                                   @RequestParam(name = "order", required = false) String order) {
        return ResponseEntity.ok(threadService.list( user, forum, since, limit, order));
    }

    @RequestMapping(path = "/db/api/thread/listPosts/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity listPosts(@RequestParam(name = "thread") Integer thread,
                                    @RequestParam(name = "since", required = false) String since,
                                    @RequestParam(name = "limit", required = false) Integer limit,
                                    @RequestParam(name = "sort",  required = false) String sort,
                                    @RequestParam(name = "order", required = false) String order) {
        return ResponseEntity.ok(threadService.listPosts(thread, since, limit, sort, order));
    }

    @RequestMapping(path = "/db/api/thread/details/", method = RequestMethod.GET,
            produces = "application/json")
    public ResponseEntity detailsThread(@RequestParam(name = "thread") Integer thread,
                                   @RequestParam(name = "related", required = false) String related) {
        return ResponseEntity.ok(threadService.details(thread, related));
    }



    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseBody
    public String resolveException() {
        return ResponseStatus.getMessage(
                ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ResponseStatus.FORMAT_JSON
        );
    }

    @ExceptionHandler({NullPointerException.class})
    @ResponseBody
    public String resolveNUllException() {
        return ResponseStatus.getMessage(
                ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ResponseStatus.FORMAT_JSON
        );
    }

    @ExceptionHandler({IOException.class})
    @ResponseBody
    public String resolveIOException() {
        return ResponseStatus.getMessage(
                ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                ResponseStatus.FORMAT_JSON
        );
    }
}
