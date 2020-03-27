package com.shotball.project.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Product {
    @Exclude
    public String key;
    public String title;
    public String image;
    public String description;
    public String user;
    public boolean available;
    @Exclude
    public Geo geo;

    public int likeCount = 0;
    public Map<String, Boolean> likes = new HashMap<>();

    public Product() {
        geo = new Geo();
    }

    public Product(String title, String image, String description, String user, boolean available) {
        this.title = title;
        this.image = image;
        this.description = description;
        this.user = user;
        this.available = available;
        geo = new Geo();
    }

    public Product(String title, String image, String description, String user, boolean available, Geo geo) {
        this.title = title;
        this.image = image;
        this.description = description;
        this.user = user;
        this.available = available;
        this.geo = geo;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setGeo(double latitude, double longitude) {
        this.geo.latitude = latitude;
        this.geo.longitude = longitude;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("image", image);
        result.put("description", description);
        result.put("user", user);
        result.put("likeCount", likeCount);
        result.put("likes", likes);

        return result;
    }
}

class Geo {
    public Double latitude;
    public Double longitude;

    public Geo() {
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public Geo(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
