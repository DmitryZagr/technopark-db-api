package ru.mail.park.model.forum;

import java.io.Serializable;

/**
 * Created by admin on 08.10.16.
 */
public class Forum implements Serializable{

    private static final long serialVersionUID = -5527566248002296042L;

    private int id;
    private String name;
    private String shortName;
    private String user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "\"id\":" + id +
                ", \"name\":" + '"' + name + '"' +
                ", \"shortName\":" + '"' + shortName + '"' +
                ", \"user\":" + '"' + user + '"';
    }

}
