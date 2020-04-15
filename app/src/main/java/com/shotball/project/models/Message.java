package com.shotball.project.models;

import java.util.HashMap;
import java.util.Map;

public class Message {
    public String uid;
    public String msg;
    public int msgtype;
    public Object timestamp;
    public Map<String, Object> readUsers = new HashMap<>();
}
