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
    public int category;
    public int exchange_category;
    public String user;
    public boolean available;
    public String g;
    public ArrayList<Double> l;
    public int likeCount;
    public Map<String, Boolean> likes = new HashMap<>();

    @Exclude
    public boolean progress = false;

    @Exclude
    public int distance;

    public Product() { }

    public Product(boolean progress) {
        this.progress = progress;
    }

    public Product(String title, ArrayList<String> images, String description, String user) {
        this.title = title;
        this.images = images;
        this.description = description;
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getExchange_category() {
        return exchange_category;
    }

    public void setExchange_category(int exchange_category) {
        this.exchange_category = exchange_category;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Exclude
    public double getLatitude() {
        return l.get(0);
    }

    @Exclude
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
        result.put("category", category);
        result.put("exchange_category", exchange_category);
        result.put("user", user);
        result.put("available", available);
        result.put("g", g);
        result.put("l", l);
        result.put("likeCount", likeCount);
        result.put("likes", likes);

        return result;
    }
}
