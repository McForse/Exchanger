package com.shotball.project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.login.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PreferencesUtil {
    public static void saveUsersList(Context context, ArrayList<UserModel> list){
        saveArrayList(context, list, "users");
    }

    public static ArrayList<UserModel> getUsersList(Context context){
        ArrayList<UserModel> mDatabase = getArrayList(context, "users");
        if (mDatabase == null) mDatabase = new ArrayList<>();

        return mDatabase;
    }

    private static <T> void saveArrayList(Context context, ArrayList<T> list, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    private static <T> ArrayList<T> getArrayList(Context context, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<UserModel>>() {}.getType();

        return gson.fromJson(json, type);
    }
}
