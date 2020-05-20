package com.shotball.project.models;

import java.util.HashMap;
import java.util.Map;

public class Message {

    public String uid;
    public String msg;
    public int type;
    public Object timestamp;
    public Map<String, Object> readUsers = new HashMap<>();

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getReadUsers() {
        return readUsers;
    }

    public void setReadUsers(Map<String, Object> readUsers) {
        this.readUsers = readUsers;
    }
}
