package ru.mail.park.model;

/**
 * Created by admin on 08.10.16.
 */
public class Forum {

    private String name;
    private String short_name;
    private String user;

    public Forum(String name, String short_name, String user) {
        this.name       = name;
        this.short_name = short_name;
        this.user       = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
