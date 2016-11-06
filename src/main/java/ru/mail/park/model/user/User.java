package ru.mail.park.model.user;

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

    public long getId() {
        return id;
    }

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

    public boolean getisAnonymous() {
        return isAnonymous;
    }

    public void setisAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

//    @JsonIgnore
//    public boolean isEmpty() {
//        if(StringUtils.isEmpty(email) || StringUtils.isEmpty(username) ||
//                StringUtils.isEmpty(about) || StringUtils.isEmpty(name))
//            return true;
//        return false;
//    }
}
