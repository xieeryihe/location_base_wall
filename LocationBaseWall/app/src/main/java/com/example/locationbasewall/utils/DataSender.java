package com.example.locationbasewall.utils;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
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
        // MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        MediaType mediaType = MediaType.parse("multipart/form-data");

        // 创建请求体
        RequestBody requestBody = RequestBody.create(mediaType, data);
        System.out.println("-----------------------------request data");
        System.out.println(data);

        // 创建请求对象
        Request request = new Request.Builder()
                .url(url)
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
    public static void sendDataToServer2(String data, String url, final DataSenderCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL serverUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "multipart/form-data; application/json; charset=utf-8");
                    connection.setDoOutput(true);

                    // 发送请求数据
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(data.getBytes("UTF-8"));
                    outputStream.close();

                    // 获取响应数据
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response.toString());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }


                        // 请求成功
                        if (callback != null) {
                            callback.onSuccess(jsonObject);
                        }
                    } else {
                        // 请求失败
                        if (callback != null) {
                            callback.onFailure("HTTP Error: " + responseCode);
                        }
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    // 请求异常
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                }
            }
        }).start();
    }

}


