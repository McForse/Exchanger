package com.shotball.project.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Product {
    @Exclude
    public String key;
    public String title;
    public ArrayList<String> images;
    public String description;
    public String user;
    public boolean available;
    @Exclude
    public Geo geo;

    @Exclude
    public int distance;

    public int likeCount = 0;
    public Map<String, Boolean> likes = new HashMap<>();

    public Product() {
        geo = new Geo();
    }

    public Product(String title, ArrayList<String> images, String description, String user, boolean available, int distance) {
        this.title = title;
        this.images = images;
        this.description = description;
        this.user = user;
        this.available = available;
        geo = new Geo();
        this.distance = distance;
    }

    public Product(String title, ArrayList<String> image, String description, String user, boolean available, int distance, Geo geo) {
        this.title = title;
        this.images = images;
        this.description = description;
        this.user = user;
        this.available = available;
        this.geo = geo;
        this.distance = distance;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setGeo(double latitude, double longitude) {
        this.geo.latitude = latitude;
        this.geo.longitude = longitude;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("images", images);
        result.put("description", description);
        result.put("user", user);
        result.put("likeCount", likeCount);
        result.put("likes", likes);

        return result;
    }
}
