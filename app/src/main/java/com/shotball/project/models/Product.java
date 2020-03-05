package com.shotball.project.models;

public class Product {
    public final String title;
    public final String imageurl;
    public final String description;

    public Product(String title, String imageurl, String description) {
        this.title = title;
        this.imageurl = imageurl;
        this.description = description;
    }
}
