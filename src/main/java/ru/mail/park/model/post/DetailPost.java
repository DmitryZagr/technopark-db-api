package ru.mail.park.model.post;

/**
 * Created by admin on 16.10.16.
 */
public class DetailPost<U, T, F > {

    private U user;
    private T thread;
    private F forum;

    //    Requried
    Integer   id;
    String date;
    String message;

    //    Optional
    Integer parent = null;
    Boolean isApproved    = null;
    Boolean isHighlighted = null;
    Boolean isEdited      = null;
    Boolean isSpam        = null;
    Boolean isDeleted     = null;
    private int likes;
    private int dislikes;
    private int points;

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

    public int getPoints() {
        return points;
    }

    public void setPoints() {
        this.points = likes - dislikes;
    }

    public String getDate() {

        return date;
    }

    public Integer getid() {
        return id;
    }

    public void setid(Integer id) {
        this.id = id;
    }

    public void setThread(T thread) {
        this.thread = thread;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public T getThread() {
        return thread;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public U getUser() {
        return user;
    }

    public void setUser(U user) {
        this.user = user;
    }

    public F getForum() {
        return forum;
    }

    public void setForum(F forum) {
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

}
