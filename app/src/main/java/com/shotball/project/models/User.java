package com.shotball.project.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {
    @Exclude
    private String uid;
    public String username;
    public String email;
    public String image;

    public User() { }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public User(String username, String email, String image) {
        this.username = username;
        this.email = email;
        this.image = image;
    }

    @Exclude
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("image", image);

        return result;
    }
}
