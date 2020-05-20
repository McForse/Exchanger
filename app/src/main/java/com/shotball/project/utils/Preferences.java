package com.shotball.project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

public class Preferences {

    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    public static void saveLocation(Context context, Location location) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(LATITUDE, (float) location.getLatitude());
        editor.putFloat(LONGITUDE, (float) location.getLongitude());
        editor.apply();
    }

    public static Location getLocation(Context context) {
        Location location = new Location("My location");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        location.setLatitude(prefs.getFloat(LATITUDE, 0));
        location.setLongitude(prefs.getFloat(LONGITUDE, 0));
        return location;
    }

}
