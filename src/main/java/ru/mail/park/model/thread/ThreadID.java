package ru.mail.park.model.thread;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by admin on 13.10.16.
 */
public class ThreadID implements Serializable{
    private static final long serialVersionUID = -5527566248002296042L;

    private Integer thread;

    public ThreadID(String json) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = (ObjectNode) mapper.readTree(json);
        this.thread = root.get("thread").asInt();
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

}
