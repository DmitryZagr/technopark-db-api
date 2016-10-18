package ru.mail.park.model.post;

/**
 * Created by admin on 12.10.16.
 */
public class VotePost extends Post {

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
}
