package ru.mail.park.model.forum;

import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.Serializable;

/**
 * Created by admin on 08.10.16.
 */
public class Forum implements Serializable{

    private static final long serialVersionUID = -5527566248002296042L;

    private int id;
    private String name;
    private String short_name;
    private String user;

    public Forum(String name, String short_name, String user) {
        this.name       = name;
        this.short_name = short_name;
        this.user       = user;
    }

    public Forum() {}

//    @ModelAttribute("forum")
//    public Forum getForum(){
//        return new Forum();
//    }

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

    @Override
    public String toString() {
        return "\"id\":" + id +
                ", \"name\":" + "\"" + name + "\""  +
                ", \"short_name\":" + "\"" + short_name + "\"" +
                ", \"user\":" +  "\"" + user + "\"";
    }

}
