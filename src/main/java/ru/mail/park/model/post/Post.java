package ru.mail.park.model.post;

import java.io.Serializable;

/**
 * Created by admin on 08.10.16.
 */
public class Post implements Serializable{

    private static final long serialVersionUID = -5527566248002296042L;

//    Requried
    Integer   id ;
    String date;
    int   thread ;
    String message;
    String user;
    String forum;

//    Optional
    Integer parent = null;
    Boolean isApproved    = null;
    Boolean isHighlighted = null;
    Boolean isEdited      = null;
    Boolean isSpam        = null;
    Boolean isDeleted     = null;

    public Integer getid() {
        return id;
    }

    public void setid(Integer idPost) {
        this.id = idPost;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Boolean getisApproved() {
        return isApproved;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }

    public Boolean getisHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(Boolean highlighted) {
        isHighlighted = highlighted;
    }

    public Boolean getisEdited() {
        return isEdited;
    }

    public void setEdited(Boolean edited) {
        isEdited = edited;
    }

    public Boolean getisSpam() {
        return isSpam;
    }

    public void setSpam(Boolean spam) {
        isSpam = spam;
    }

    public Boolean getisDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public String toString() {
        return
                "\"id\":" + id +
                ", \"date\": \"" + date + '\"' +
                ", \"thread\":" + thread +
                ", \"message\": \"" + message + '\"' +
                ", \"user\": \"" + user + '\"' +
                ", \"forum\": \"" + forum + '\"' +
                ", \"parent\":" + parent +
                ", \"isApproved\":" + isApproved +
                ", \"isHighlighted\":" + isHighlighted +
                ", \"isEdited\":" + isEdited +
                ", \"isSpam\":" + isSpam +
                ", \"isDeleted\":" + isDeleted
                ;
    }
}
