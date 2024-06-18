package com.hanzjefferson.mopsi.models;

public class Response<T> {
    public int code;
    public T data;
    public String message;

    public boolean isSuccess(){
        return code >= 200 && code < 300;
    }
}
