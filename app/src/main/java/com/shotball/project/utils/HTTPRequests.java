package com.shotball.project.utils;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HTTPRequests {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client = new OkHttpClient();

    public static Call post(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

}
