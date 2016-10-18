package ru.mail.park.model.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.mail.park.util.MyJsonUtils;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by admin on 11.10.16.
 */
public class IdPost implements Serializable{
    private static final long serialVersionUID = -5527566248002296042L;
    private ObjectMapper mapper = new ObjectMapper();

    Integer id;

    public IdPost(String json) throws IOException {
        json = MyJsonUtils.replaceOneQuoteTwoQuotes(json);
        ObjectNode root = (ObjectNode) mapper.readTree(json);
        setid(root.get("post").asInt());
    }

    public Integer getid() {
        return id;
    }

    public void setid(Integer post) {
        this.id = post;
    }
}
