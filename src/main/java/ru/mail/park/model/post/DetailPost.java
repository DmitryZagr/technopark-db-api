package ru.mail.park.model.post;

/**
 * Created by admin on 16.10.16.
 */
public class DetailPost<U, T, F > {

    private U user;
    private T thread;
    private F forum;

    private static final long serialVersionUID = -5527566248002296042L;

    //    Requried
    int   post;
    String date;
    String message;

    //    Optional
    Integer parent;
    Boolean isApproved    = false;
    Boolean isHighlighted = false;
    Boolean isEdited      = false;
    Boolean isSpam        = false;
    Boolean isDeleted     = false;
    private int like;
    private int dislike;
    private int points;

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getDislike() {
        return dislike;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints() {
        this.points = like - dislike;
    }

    public DetailPost() {
    }

    public String getDate() {

        return date;
    }

    public int getpost() {
        return post;
    }

    public void setpost(int idPost) {
        this.post = idPost;
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

    public boolean getisEdited() {
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

    public boolean getisDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

}
