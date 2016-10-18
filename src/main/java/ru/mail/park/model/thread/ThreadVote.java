package ru.mail.park.model.thread;

import java.io.Serializable;

/**
 * Created by admin on 14.10.16.
 */
public class ThreadVote implements Serializable {
    //    Requried
    private Integer id;
    private String forum;
    private String title;
    private boolean isClosed;
    private String user;
    private String date;
    private String message;
    private String slug;
    private int likes;
    private int dislikes;
    private int posts;
    private int points;

    //    Optional
    private Boolean isDeleted = null;

    public ThreadVote() {}

//    public ThreadVote(long id, String forum, String title,
//                      boolean isClosed, String user, String date,
//                      String message, String slug, long likes,
//                      long dislikes, boolean isDeleted, long posts) {
//        this.id = id;
//        this.forum = forum;
//        this.title = title;
//        this.isClosed = isClosed;
//        this.user = user;
//        this.date = date;
//        this.message = message;
//        this.slug = slug;
//        this.likes = likes;
//        this.dislikes = dislikes;
//        this.isDeleted = isDeleted;
//        this.posts = posts;
//    }

    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints() {
        this.points = likes - dislikes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public boolean getisClosed() {
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

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public Boolean getisDeleted() {
        return isDeleted;
    }

    public void setisDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
