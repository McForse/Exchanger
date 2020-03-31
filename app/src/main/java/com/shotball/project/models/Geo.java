package com.shotball.project.models;

public class Geo {
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

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
