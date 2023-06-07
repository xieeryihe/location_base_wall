package com.example.locationbasewall.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.example.locationbasewall.adapter.PostAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DataGetter {
    private final OkHttpClient client;
    static int ret;

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

    // 返回获取的帖子数量
    public static int getLocationAndPostOverviewData
            (Activity activity,PostAdapter postAdapter,
             int page_num, int page_size, int distance){

        Context context = activity.getApplicationContext();
        Location location = new Location(context);
        location.getCurrentLocation(new Location.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude, String province, String city, String address) {
                System.out.println(city);
                System.out.println(address);

                @SuppressLint("DefaultLocale")
                String targetUrl = String.format("" +
                                "http://121.43.110.176:8000/api/post?" +
                                "page_num=%d&page_size=%d&location_x=%.2f&location_y=%.2f&distance=%d",
                        page_num, page_size, longitude, latitude, distance);

                DataGetter.getDataFromServer(targetUrl, new DataGetter.DataGetterCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        try {
                            int code = jsonObject.getInt("code");
                            String errorMsg = jsonObject.getString("error_msg");
                            if (code != 0 && code != 1 && code != 2 && code != 4) {
                                // 获取数据失败
                                String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                                MyToast.show(context,msg);
                            } else {
                                JSONObject data = jsonObject.getJSONObject("data");
                                ret = processPostOverviewData(data,postAdapter.getPostList());

                                activity.runOnUiThread(new Runnable() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void run() {
                                        // 更新UI组件的代码
                                        postAdapter.notifyDataSetChanged();
                                    }
                                });

                            }
                        } catch (JSONException e) {
                            MyToast.show(context, "JSON错误");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        System.out.println(errorMessage);
                        MyToast.show(context, "网络请求错误");
                    }
                });

            }
            @Override
            public void onLocationFailed(String errorMsg) {
                System.out.println("Failed to get location: " + errorMsg);

            }
        });
        return ret;
    }

    public static int processPostOverviewData(JSONObject data, ArrayList<Post> postList) {

        try {

            JSONArray jsonArray = data.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                // 获取条目的各个字段值
                String id = item.getString("id");
                String user_id = item.getString("user_id");
                String username = item.getString("username");
                String user_picture = item.getString("user_picture");
                String title = item.getString("title");
                String text = item.getString("text");
                String date = item.getString("date");
                double location_x = item.getDouble("location_x");
                double location_y = item.getDouble("location_y");
                String ip_address = item.getString("ip_address");
                System.out.println(user_picture);
                Post post = new Post(id, user_id, username,user_picture, title, text, -1, "",date, location_x, location_y, ip_address);
                postList.add(post);
            }
            return jsonArray.length();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

}

