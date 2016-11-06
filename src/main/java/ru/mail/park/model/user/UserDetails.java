package ru.mail.park.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by admin on 08.10.16.
 */
public class UserDetails implements Serializable {

    private static final long serialVersionUID = -5527566248002296042L;

    //    Requried
    private Long id;
    private String username;
    private String about;
    private String name;
    private String email;

    //    Optional
    private boolean isAnonymous = false;

    protected ArrayList<String> followers = new ArrayList<>();
    protected ArrayList<String> following = new ArrayList<>();
    protected ArrayList<Integer> subscriptions = new ArrayList<>();

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public ArrayList<Integer> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(ArrayList<Integer> subscriptions) {
        this.subscriptions = subscriptions;
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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getisAnonymous() {
        return isAnonymous;
    }

    public void setisAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return StringUtils.isEmpty(email) || StringUtils.isEmpty(username) ||
                StringUtils.isEmpty(about) || StringUtils.isEmpty(name);
    }
}
