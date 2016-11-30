package ru.mail.park.model.post;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by admin on 12.10.16.
 */
public class VotePost extends Post {

    private int likes;
    private int dislikes;
    private int points;

    @JsonIgnore
    private int root;

    public int getRoot() {
        return root;
    }

    public void setRoot(int root) {
        this.root = root;
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

    public int getPoints() {
        return points;
    }

    public void setPoints() {
        this.points = likes - dislikes;
    }
}
