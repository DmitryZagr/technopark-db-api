package ru.mail.park.model.post;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by admin on 08.10.16.
 */
public class Post implements Serializable{

    private static final long serialVersionUID = -5527566248002296042L;

//    Requried
    int   post ;
    String date;
    int   thread ;
    String message;
    String user;
    String forum;

//    Optional
    Integer parent;
    Boolean isApproved    = null;
    Boolean isHighlighted = null;
    Boolean isEdited      = null;
    Boolean isSpam        = null;
    Boolean isDeleted     = null;

    public Post() {
    }

//    public Post(String date, int thread, String message, String user, String forum) {
//        this.date = date;
//        this.thread = thread;
//        this.message = message;
//        this.user = user;
//        this.forum = forum;
//    }

    public int getpost() {
        return post;
    }

    public void setpost(int idPost) {
        this.post = idPost;
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
        return "" +
                "\"id\":" + post +
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
