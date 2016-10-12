package ru.mail.park.model.post;

/**
 * Created by admin on 12.10.16.
 */
public class VotePost extends Post {

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
}
