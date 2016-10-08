package ru.mail.park.model;

import java.util.Calendar;

/**
 * Created by admin on 08.10.16.
 */
public class Post {

//    Requried
    Calendar date;
    Integer thread = null;
    String message;
    String user;
    String forum;

//    Optional
    Integer parent        = null;
    boolean isApproved    = false;
    boolean isHighlighted = false;
    boolean isEdited      = false;
    boolean isSpam        = false;
    boolean isDeleted     = false;

    public Post() {
    }

    public Post(Calendar date, int thread, String message, String user, String forum) {
        this.date = date;
        this.thread = thread;
        this.message = message;
        this.user = user;
        this.forum = forum;
    }

    public Calendar getDate() {

        return date;
    }

    public void setDate(Calendar date) {
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
}
