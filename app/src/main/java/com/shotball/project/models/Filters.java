package com.shotball.project.models;

import android.text.TextUtils;

public class Filters {

    private int distance = 500; // meters

    private String category = null;

    public Filters() {}

    public static Filters getDefault() {
        Filters filter = new Filters();
        filter.setDistance(500);

        return filter;
    }

    public boolean hasDistance() {
        return (distance > 0);
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public boolean hasCategory() {
        return !(TextUtils.isEmpty(category));
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
