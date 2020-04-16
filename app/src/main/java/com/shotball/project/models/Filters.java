package com.shotball.project.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Filters {

    private int distance = 500; // meters

    private Map<Integer, Boolean> category = new HashMap<>();

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
        return !category.isEmpty();
    }

    public Map<Integer, Boolean>  getCategory() {
        return category;
    }

    public void setCategory(Map<Integer, Boolean>  category) {
        this.category = category;
    }

    @NonNull
    @Override
    public String toString() {
        return "Distance=" + distance + "; Category:" + category;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return distance == ((Filters) obj).getDistance() && category.equals(((Filters) obj).getCategory());
    }
}
