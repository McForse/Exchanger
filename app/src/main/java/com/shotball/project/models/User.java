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
    public int exhibited;
    public int exchanges;
    public String fcm;

    public User() {

    }

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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getExhibited() {
        return exhibited;
    }

    public void setExhibited(int exhibited) {
        this.exhibited = exhibited;
    }

    public int getExchanges() {
        return exchanges;
    }

    public void setExchanges(int exchanges) {
        this.exchanges = exchanges;
    }

    public String getFcm() {
        return fcm;
    }

    public void setFcm(String fcm) {
        this.fcm = fcm;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("image", image);
        result.put("fcm", fcm);

        return result;
    }
}
