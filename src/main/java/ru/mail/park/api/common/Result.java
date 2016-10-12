package ru.mail.park.api.common;

/**
 * Created by admin on 11.10.16.
 */
public final class Result<V> {
    private final int code;
    private final V response;

    public Result(int code, V response) {
        this.code = code;
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public V getResponse() {
        return response;
    }
}