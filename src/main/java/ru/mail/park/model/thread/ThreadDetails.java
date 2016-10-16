package ru.mail.park.model.thread;

/**
 * Created by admin on 16.10.16.
 */
public class ThreadDetails<U, F> {

    //    Requried
    private long id;
    private F forum;
    private String title;
    private boolean isClosed;
    private U user;
    private String date;
    private String message;
    private String slug;
    private long likes;
    private long dislikes;
    private long posts;
    private long points;

    //    Optional
    private boolean isDeleted = false;

    public ThreadDetails() {}

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

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
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
