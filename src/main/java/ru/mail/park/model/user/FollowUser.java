package ru.mail.park.model.user;

import java.util.ArrayList;

/**
 * Created by admin on 13.10.16.
 */
public class FollowUser extends User{
    protected ArrayList<String> followers = new ArrayList<>();
    protected ArrayList<String> following = new ArrayList<>();
//    protected ArrayList<String>

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public void setFollowers(ArrayList<String> followers) {
        this.followers = followers;
    }

    public ArrayList<String> getFollowing() {
        return following;
    }

    public void setFollowing(ArrayList<String> following) {
        this.following = following;
    }

}
