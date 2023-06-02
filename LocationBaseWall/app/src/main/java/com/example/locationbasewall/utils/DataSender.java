package com.example.locationbasewall.utils;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class DataSender {

    public interface DataSenderCallback {
        void onSuccess(JSONObject jsonObject);
        void onFailure(String errorMessage);
    }
    public static void sendDataToServer(String data, String url, final DataSenderCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // 设置请求体的媒体类型为JSON
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        // MediaType mediaType = MediaType.parse("multipart/form-data; application/x-www-form-urlencoded");

        // 创建请求体
        RequestBody requestBody = new FormBody.Builder()
                .add("username","1231231231")
                .add("password","admin")
                        .build();
        System.out.println(requestBody.toString());
        // 创建请求对象
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "multipart/form-data")
                .post(requestBody)
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
    public static void sendDataToServer(RequestBody requestBody, String url, final DataSenderCallback callback) {

        OkHttpClient client = new OkHttpClient();
        // 创建请求对象
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "multipart/form-data")
                .post(requestBody)
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


