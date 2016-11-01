package ru.mail.park.model.thread;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by admin on 12.10.16.
 */
public class ThreadSubscribe implements Serializable {
    private static final long serialVersionUID = -5527566248002296042L;
    private ObjectMapper mapper = new ObjectMapper();

    private int thread;
    private String user;

    public ThreadSubscribe(String json ) throws IOException {
        ObjectNode root = (ObjectNode) mapper.readTree(json);
        setThread(root.get("thread").asInt());
        setUser(root.get("user").asText());
    }


    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
