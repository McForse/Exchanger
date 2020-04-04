package com.shotball.project.models;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Filters {

    private int distance = 500; // meters

    private String category = "";

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
