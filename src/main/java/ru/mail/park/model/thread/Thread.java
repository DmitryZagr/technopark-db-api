package ru.mail.park.model.thread;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by admin on 08.10.16.
 */
public class Thread implements Serializable{

    private static final long serialVersionUID = -5527566248002296042L;

    //    Requried
    private long id;
    private String forum;
    private String title;
    private boolean isClosed;
    private String user;
    private String date;
    private String message;
    private String slug;

//    Optional
    private boolean isDeleted = false;

    public Thread() {
    }

    public Thread(String forum, String title, boolean isClosed,
                  String user, String date,
                  String message, String slug) {
        this.forum = forum;
        this.title = title;
        this.isClosed = isClosed;
        this.user = user;
        this.date = date;
        this.message = message;
        this.slug = slug;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}

