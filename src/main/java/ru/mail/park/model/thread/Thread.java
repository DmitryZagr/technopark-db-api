package ru.mail.park.model.thread;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.mail.park.util.MyJsonUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by admin on 08.10.16.
 */
public class Thread implements Serializable{

    private static final long serialVersionUID = -5527566248002296042L;
    private ObjectMapper mapper = new ObjectMapper();

    //    Requried
    private int id;
    private String forum;
    private String title;
    private Boolean isClosed;
    private String user;
    private String date;
    private String message;
    private String slug;

    //    Optional
    private Boolean isDeleted = null;

    public Thread() {
    }

    public Thread(String json) throws IOException {
        json = MyJsonUtils.replaceOneQuoteTwoQuotes(json);
        ObjectNode root = (ObjectNode) mapper.readTree(json);
        setForum(root.get("forum").asText());
        setTitle(root.get("title").asText());
        setClosed(root.get("isClosed").asBoolean());
        setUser(root.get("user").asText());
        setDate(root.get("date").asText());
        setMessage(root.get("message").asText());
        setSlug(root.get("slug").asText());
        if(root.has("isDeleted")) setisDeleted(root.get("isDeleted").asBoolean());
    }

//    public String getForum() {
//
//        return forum;
//    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public String getForum() {
        return forum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getisClosed() {
        return isClosed;
    }

    public void setClosed(Boolean closed) {
        isClosed = closed;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String email) {
        this.user = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Boolean getisDeleted() {
        return isDeleted;
    }

    public void setisDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

}

