package ru.mail.park.api.user;

/**
 * Created by admin on 08.10.16.
 */
public class UserCreateRequest {

    private String  username;
    private String  about;
    private String  name;
    private String  email;
    private boolean isAnonymous = false;

    public UserCreateRequest() {
    }

    public UserCreateRequest(String username, String about, String name, String email, boolean isAnonymous) {

        this.username = username;
        this.about = about;
        this.name = name;
        this.email = email;
        this.isAnonymous = isAnonymous;
    }

    public UserCreateRequest(String username, String about, String name, String email) {

        this.username = username;
        this.about = about;
        this.name = name;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getAbout() {
        return about;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }
}
