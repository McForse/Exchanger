package com.shotball.project.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Filters {

    private int distance = 500; // meters

    private Map<Integer, Boolean> categories = new HashMap<>();

    public Filters() {}

    public static Filters getDefault() {
        Filters filter = new Filters();
        filter.setDistance(500);
        filter.categories = new HashMap<>();
        filter.categories.put(Categories.All.getValue(), true);

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

    public boolean hasCategory(int id) {
        return (id < categories.size()) ? categories.get(id) : false;
    }

    public Map<Integer, Boolean> getCategories() {
        return categories;
    }

    public void setCategories(Map<Integer, Boolean> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public String toString() {
        return "Distance=" + distance + "; Category:" + categories;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return distance == ((Filters) obj).getDistance() && ((Filters) obj).getCategories() == null || categories.equals(((Filters) obj).getCategories());
    }
}
