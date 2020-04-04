package com.shotball.project.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product {
    @Exclude
    public String key;
    public String title;
    public ArrayList<String> images;
    public String description;
    public String user;
    public boolean available;
    public String g;
    public List<Double> l;
    public int likeCount;
    public Map<String, Boolean> likes = new HashMap<>();

    @Exclude
    public int distance;

    public Product() { }

    public Product(String title, ArrayList<String> images, String description, String user, boolean available, int distance) {
        this.title = title;
        this.images = images;
        this.description = description;
        this.user = user;
        this.available = available;
        this.distance = distance;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public double getLatitude() {
        return l.get(0);
    }

    public double getLongitude() {
        return l.get(1);
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("images", images);
        result.put("description", description);
        result.put("user", user);
        result.put("available", available);
        result.put("g", g);
        result.put("l", l);
        result.put("likeCount", likeCount);
        result.put("likes", likes);

        return result;
    }
}
