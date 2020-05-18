package com.shotball.project.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.shotball.project.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.shotball.project.utils.HTTPRequests.post;

public class FCM {

    private static final String TAG = "FCM";

    public static void sendPush(String title, String message, String token, String image) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("title", title);
            jsonObject.put("message", message);
            jsonObject.put("token", token);
            jsonObject.put("imageUrl", image);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        post(Config.FCM_SERVER_URL + "/notification/token", jsonObject.toString(), new Callback() {
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
