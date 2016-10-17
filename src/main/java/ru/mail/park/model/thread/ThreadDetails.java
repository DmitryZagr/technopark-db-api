package ru.mail.park.model.thread;

/**
 * Created by admin on 16.10.16.
 */
public class ThreadDetails<U, F> {

    //    Requried
    private int id;
    private F forum;
    private String title;
    private Boolean isClosed;
    private U user;
    private String date = null;
    private String message;
    private String slug;
    private int likes;
    private int dislikes;
    private int posts;
    private int points;

    //    Optional
    private Boolean isDeleted = null;

    public ThreadDetails() {}

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public F getForum() {
        return forum;
    }

    public void setForum(F forum) {
        this.forum = forum;
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

    public U getUser() {
        return user;
    }

    public void setUser(U user) {
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

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
