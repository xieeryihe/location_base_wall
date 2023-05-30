package com.example.locationbasewall.data;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class DataSender {
    private static final String serverUrl = "aaa";
    public static void sendDataToServer(String data) {
        try {
            // 创建连接
            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 发送数据
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 数据发送成功
                // 可以根据需要处理响应数据
            } else {
                // 数据发送失败
                // 可以根据需要处理失败情况
            }

            // 断开连接
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


