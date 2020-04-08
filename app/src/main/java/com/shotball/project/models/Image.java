package com.shotball.project.models;

import android.net.Uri;

public class Image {

    public Uri image;
    public boolean addButton;

    public Image(Uri image) {
        this.image = image;
    }

    public Image(boolean addButton) {
        this.addButton = addButton;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

}
