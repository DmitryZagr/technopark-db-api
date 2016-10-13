package ru.mail.park.model.thread;

import java.io.Serializable;

/**
 * Created by admin on 14.10.16.
 */
public class ThreadVote implements Serializable {
    //    Requried
    private long id;
    private String forum;
    private String title;
    private boolean isClosed;
    private String user;
    private String date;
    private String message;
    private String slug;
    private long likes;
    private long dislikes;
    private long posts;
    private long points;

    //    Optional
    private boolean isDeleted = false;

    public ThreadVote(){}

    public ThreadVote(long id, String forum, String title,
                      boolean isClosed, String user, String date,
                      String message, String slug, long likes,
                      long dislikes, boolean isDeleted, long posts) {
        this.id = id;
        this.forum = forum;
        this.title = title;
        this.isClosed = isClosed;
        this.user = user;
        this.date = date;
        this.message = message;
        this.slug = slug;
        this.likes = likes;
        this.dislikes = dislikes;
        this.isDeleted = isDeleted;
        this.posts = posts;
    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints() {
        this.points = likes - dislikes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
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

    public void setUser(String user) {
        this.user = user;
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

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public long getDislikes() {
        return dislikes;
    }

    public void setDislikes(long dislikes) {
        this.dislikes = dislikes;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
