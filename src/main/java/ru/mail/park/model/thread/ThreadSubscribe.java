package ru.mail.park.model.thread;

/**
 * Created by admin on 12.10.16.
 */
public class ThreadSubscribe {
    private long thread;
    private String user;

    public long getThread() {
        return thread;
    }

    public void setThread(long thread) {
        this.thread = thread;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
