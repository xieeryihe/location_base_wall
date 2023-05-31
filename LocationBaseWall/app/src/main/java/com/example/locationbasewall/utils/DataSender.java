package com.example.locationbasewall.utils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class DataSender {
    private static String serverUrl = "http://121.43.110.176:8000/";

    public static void sendTest() {
        Thread thread = new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://121.43.110.176:8000/hello/")
                    .build();

            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    // 请求成功，可以获取响应数据
                    String responseData = response.body().string();
                    // 在这里处理响应数据
                } else {
                    // 请求失败
                    // 在这里处理错误情况
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }


    public static void sendDataToServer(String data, String url) {


        // 创建一个 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient();

        // 设置请求体的媒体类型为 application/json
        MediaType mediaType = MediaType.parse("application/json");

        // 创建请求体
        RequestBody requestBody = RequestBody.create(mediaType, data);

        // 创建 POST 请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // 发送请求并处理响应
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 请求成功
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                } else {
                    // 请求失败
                    System.out.println("Request failed: " + response.code());
                }
            }
        });
    }
}


