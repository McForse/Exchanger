package com.shotball.project.models;

public class Notification {

    public String title;
    public String message;
    public String imageUrl;
    public String username;
    public String topic;
    public String token;

    public Notification() {

    }

    public Notification(String title, String message, String token) {
        this.title = title;
        this.message = message;
        this.token = token;
    }

    public Notification(String title, String message, String imageUrl, String username, String token) {
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.username = username;
        this.token = token;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
