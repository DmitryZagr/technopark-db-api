package ru.mail.park.model.forum;

import java.io.Serializable;

/**
 * Created by admin on 16.10.16.
 */
public class ForumDetails<U> implements Serializable {
    private static final long serialVersionUID = -5527566248002296042L;

    private int id;
    private String name;
    private String shortName;
    private U user;

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

    public String getShort_name() {
        return shortName;
    }

    public void setShort_name(String shortName) {
        this.shortName = shortName;
    }

    public U getUser() {
        return user;
    }

    public void setUser(U user) {
        this.user = user;
    }
}
