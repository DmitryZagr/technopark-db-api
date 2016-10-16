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
    Long   idPost = 0l;
    String date;
    String message;

    //    Optional
    long parent;
    boolean isApproved    = false;
    boolean isHighlighted = false;
    boolean isEdited      = false;
    boolean isSpam        = false;
    boolean isDeleted     = false;
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

    public Long getIdPost() {
        return idPost;
    }

    public void setIdPost(long idPost) {
        this.idPost = idPost;
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

}
