package ru.mail.park.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
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
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ThreadController(ThreadServiceImpl threadService){
        this.threadService = threadService;
    }

    @RequestMapping(path = "/db/api/thread/create/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity createThread(@RequestBody Thread thread) {
        return ResponseEntity.ok(threadService.create(thread));
    }

    @RequestMapping(path = "/db/api/thread/update/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity updateThread(HttpEntity<String> httpEntity) {
        String json = httpEntity.getBody();
        Thread thread = new Thread();
        try {
            JsonNode root = mapper.readTree(json);
            thread.setId(root.get("thread").asInt());
            thread.setSlug(root.get("slug").asText());
            thread.setMessage(root.get("message").asText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(threadService.update(thread));
    }

    @RequestMapping(path = "/db/api/thread/subscribe/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity SubscribeThread(@RequestBody ThreadSubscribe thread) {
        return ResponseEntity.ok(threadService.subscribeUnSub(thread, true));
    }

    @RequestMapping(path = "/db/api/thread/unsubscribe/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity unSubscribeThread(@RequestBody ThreadSubscribe thread) {
        return ResponseEntity.ok(threadService.subscribeUnSub(thread, false));
    }

    @RequestMapping(path = "/db/api/thread/open/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity openThread(@RequestBody ThreadID thread) {
        return ResponseEntity.ok(threadService.open(thread));
    }

    @RequestMapping(path = "/db/api/thread/close/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity closeThread(@RequestBody ThreadID thread) {
        return ResponseEntity.ok(threadService.close(thread));
    }

    @RequestMapping(path = "/db/api/thread/remove/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity removeThread(@RequestBody ThreadID thread) {
        return ResponseEntity.ok(threadService.remove(thread));
    }

    @RequestMapping(path = "/db/api/thread/restore/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity restoreThread(@RequestBody ThreadID thread) {
        return ResponseEntity.ok(threadService.restore(thread));
    }

    @RequestMapping(path = "/db/api/thread/vote/", method = RequestMethod.POST,
            produces = "application/json")
    public ResponseEntity voteThread(HttpEntity<String> httpEntity) {
        return ResponseEntity.ok(threadService.vote(httpEntity.getBody()));
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
