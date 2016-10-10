package ru.mail.park.model;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by admin on 08.10.16.
 */
public class Post implements Serializable{

    private static final long serialVersionUID = -5527566248002296042L;

//    Requried
    long   idPost = 0;
    String date;
    long   thread ;
    String message;
    String user;
    String forum;

//    Optional
    long parent;
    boolean isApproved    = false;
    boolean isHighlighted = false;
    boolean isEdited      = false;
    boolean isSpam        = false;
    boolean isDeleted     = false;

    public Post() {
    }

    public Post(String date, int thread, String message, String user, String forum) {
        this.date = date;
        this.thread = thread;
        this.message = message;
        this.user = user;
        this.forum = forum;
    }

    public String getDate() {

        return date;
    }

    public long getIdPost() {
        return idPost;
    }

    public void setIdPost(long idPost) {
        this.idPost = idPost;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getThread() {
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

    public long getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public void setSpam(boolean spam) {
        isSpam = spam;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public String toString() {
        return "" +
                "\"id\":" + idPost +
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
