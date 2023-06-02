package com.example.locationbasewall.utils;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.*;

public class DataGetter {
    private final OkHttpClient client;

    public DataGetter() {
        client = new OkHttpClient();
    }
    public interface DataGetterCallback {
        void onSuccess(JSONObject jsonObject);
        void onFailure(String errorMessage);
    }

    public static void getDataFromServer(String url, final DataGetter.DataGetterCallback callback) {

        OkHttpClient client = new OkHttpClient();
        // 创建请求对象
        Request request = new Request.Builder()
                .url(url)
                .build();

        // 执行异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                System.out.println("访问：" + url + "\nresponse.code : " + response.code());

                if (response.isSuccessful()) {
                    // 请求成功
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        // 调用回调函数，传递响应数据
                        callback.onSuccess(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onFailure("Failed to parse response data");
                    }
                } else {
                    // 请求失败
                    callback.onFailure("Request failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                callback.onFailure("Request failed");
            }
        });
    }

}

