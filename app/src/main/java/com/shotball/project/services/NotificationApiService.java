package com.shotball.project.services;

import com.google.gson.JsonObject;
import com.shotball.project.BuildConfig;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationApiService {
    @Headers({
            "Authorization: key=" + BuildConfig.FCM_SERVER_KEY,
            "Content-Type: application/json"
    })
    @POST("fcm/send")
    Call<JsonObject> sendNotification(@Body JsonObject payload);
}