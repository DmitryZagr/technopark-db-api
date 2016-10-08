package ru.mail.park.model;

/**
 * Created by admin on 08.10.16.
 */
public class User {

//    Requried
    private String username;
    private String about;
    private String name;
    private String email;

//    Optional
    private boolean isAnonymous = true;

    public User(String username, String about, String name,
                                String email, boolean isAnonymous) {
        this.username    = username;
        this.about       = about;
        this.name        = name;
        this.email       = email;
        this.isAnonymous = isAnonymous;
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
