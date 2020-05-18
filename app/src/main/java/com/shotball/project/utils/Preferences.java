package com.shotball.project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

public class Preferences {

    public static void saveLocation(Context context, Location location) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("latitude", (float) location.getLatitude());
        editor.putFloat("longitude", (float) location.getLongitude());
        editor.apply();
    }

    public static Location getLocation(Context context) {
        Location location = new Location("My location");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        location.setLatitude(prefs.getFloat("latitude", 0));
        location.setLongitude(prefs.getFloat("longitude", 0));
        return location;
    }

}
