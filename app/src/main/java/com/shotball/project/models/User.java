package com.shotball.project.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {
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
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("image", image);

        return result;
    }
}
