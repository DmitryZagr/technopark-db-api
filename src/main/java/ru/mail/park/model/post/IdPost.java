package ru.mail.park.model.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by admin on 11.10.16.
 */
public class IdPost implements Serializable{
    private static final long serialVersionUID = -5527566248002296042L;

    Integer id;

    public IdPost(String json) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = (ObjectNode) mapper.readTree(json);
        this.id = root.get("post").asInt();
    }

    public Integer getid() {
        return id;
    }

    public void setid(Integer post) {
        this.id = post;
    }
}
