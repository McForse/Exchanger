package com.shotball.project.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.shotball.project.Config;
import com.shotball.project.models.Notification;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.shotball.project.utils.HTTPRequests.post;

public class FCM {

    private static final String TAG = "FCM";

    public static void sendPush(Notification notification) {
        Gson gson = new Gson();
        send(gson.toJson(notification));
    }

    private static void send(String jsonObject) {
        post(Config.FCM_SERVER_URL + "/notification/token", jsonObject, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    Log.d(TAG, responseStr);
                } else {
                    Log.d(TAG, "onResponse: unsuccessful");
                }
            }
        });
    }

}
