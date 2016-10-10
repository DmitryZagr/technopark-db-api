package ru.mail.park.model;

import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.Serializable;

/**
 * Created by admin on 08.10.16.
 */
public class User implements Serializable{

    private static final long serialVersionUID = -5527566248002296042L;

//    Requried
    private long id;
    private String username;
    private String about;
    private String name;
    private String email;

//    Optional
    private boolean isAnonymous = false;

    public User() {
    }

    public User(String username, String about, String name,
                String email, boolean isAnonymous) {
        this.username    = username;
        this.about       = about;
        this.name        = name;
        this.email       = email;
        this.isAnonymous = isAnonymous;
    }

//    @ModelAttribute("user")
//    public User getUser(){
//        return new User();
//    }

    public long getId() {
        return id;
    }

//    @Override
//    public String toString() {
//        return "\"id\":" + id.intValue() + ',' +
//                "\"username\":\""    + username + "\"," +
//                "\"about\":\""       + about + "\","  +
//                "\"name\":\""        + name  + "\","  +
//                "\"email\":\":"      + email + "\","  +
//                "\"isAnonymous\":\"" + isAnonymous + "\"";
//    }

    public void setId(long id) {
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

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }
}
