package com.example.locationbasewall.home;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationbasewall.MainActivity;
import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.CommentAdapter;
import com.example.locationbasewall.login.LoginActivity;
import com.example.locationbasewall.utils.Comment;
import com.example.locationbasewall.utils.DataGetter;
import com.example.locationbasewall.utils.DataSender;
import com.example.locationbasewall.utils.LocalUserInfo;
import com.example.locationbasewall.utils.Location;
import com.example.locationbasewall.utils.MyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostDetailActivity extends AppCompatActivity {
    private ImageView postDetailUserImageView;
    private TextView postDetailUsernameTextView;
    private TextView postDetailIPTextView;

    private Button postDetailDeletePostButton;
    private TextView titleTextView;
    private TextView contentTextView;
    private EditText postDetailCommentEditText;
    private Button postDetailCommentButton;

    private ImageView postDetailMediaImageView;
    private VideoView postDetailMediaVideoView;

    private RecyclerView commentsRecycleView;

    private int mContentType;
    private ArrayList<Comment> commentList; // 评论列表（复用了Post的构造）
    private CommentAdapter commentAdapter;


    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        postDetailUserImageView = findViewById(R.id.postDetailUserImageView);
        postDetailUsernameTextView = findViewById(R.id.postDetailUsernameTextView);
        postDetailIPTextView = findViewById(R.id.postDetailIPTextView);
        postDetailDeletePostButton = findViewById(R.id.postDetailDeletePostButton);

        titleTextView = findViewById(R.id.postDetailTitleTextView);
        contentTextView = findViewById(R.id.postDetailContentTextView);
        postDetailCommentEditText = findViewById(R.id.postDetailCommentEditText);
        postDetailCommentButton = findViewById(R.id.postDetailCommentButton);
        postDetailMediaImageView = findViewById(R.id.postDetailMediaImageView);
        postDetailMediaVideoView = findViewById(R.id.postDetailMediaVideoView);

        commentsRecycleView = findViewById(R.id.commentsRecycleView);
        // 要加布局管理器
        commentsRecycleView.setLayoutManager(new LinearLayoutManager(PostDetailActivity.this));

        LocalUserInfo localUserInfo = new LocalUserInfo(getApplicationContext());
        String user_id = localUserInfo.getId();
        String post_id = getIntent().getStringExtra("post_id");
        String publisher_id = getIntent().getStringExtra("publisher_id");

        if (user_id.equals(publisher_id)){
            postDetailDeletePostButton.setVisibility(View.VISIBLE);
        }else {
            postDetailDeletePostButton.setVisibility(View.GONE);
        }


        // 1. 获取帖子详情部分
        String targetUrl = "http://121.43.110.176:8000/api/post/" + "?post_id=" + post_id;
        DataGetter.getDataFromServer(targetUrl, new DataGetter.DataGetterCallback() {
            final Context context = getApplicationContext();
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    int code = jsonObject.getInt("code");
                    String errorMsg = jsonObject.getString("error_msg");
                    System.out.println("our code :" + code);
                    if (code != 0){
                        // 获取数据失败
                        String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                        MyToast.show(context,msg);
                    } else {
                        JSONObject data = jsonObject.getJSONObject("data");
                        processAndSetPost(data);
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


        // 2. 获取评论部分
        int page_num = 1;
        int page_size = 10;
        targetUrl = String.format("" +
                        "http://121.43.110.176:8000/api/comment?" +
                        "page_num=%d&page_size=%d&post_id=%d",
                page_num, page_size, Integer.valueOf(post_id));
        System.out.println(targetUrl);

        DataGetter.getDataFromServer(targetUrl, new DataGetter.DataGetterCallback() {
            final Context context = getApplicationContext();
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    int code = jsonObject.getInt("code");
                    String errorMsg = jsonObject.getString("error_msg");
                    System.out.println("our code :" + code);
                    if (code != 0 && code != 1 && code != 2){
                        // 获取数据失败
                        String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                        MyToast.show(context,msg);
                    } else {
                        JSONObject data = jsonObject.getJSONObject("data");
                        MyToast.show(context, errorMsg);
                        commentList = new ArrayList<>(); // 初始化帖子数据列表

                        processComments(data,commentList);
                        commentAdapter = new CommentAdapter(commentList, new CommentAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(Comment comment) {
                                // 评论的点击事件，同样是唤起评论键盘那些
                            }
                        });
                        PostDetailActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 更新UI组件的代码
                                commentAdapter.notifyDataSetChanged();
                                commentsRecycleView.setAdapter(commentAdapter);
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


        // 一般情况下，“发表”按钮不显示，点击编辑框之后才显现
        postDetailCommentEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // 唤起键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    postDetailCommentEditText.requestFocus(); // 聚焦光标
                    postDetailCommentButton.setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }

        });

        // 点击“评论”按钮
        postDetailCommentButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                /* 1. 整理并发送数据 */
                String text = postDetailCommentEditText.getText().toString();
                // 获取位置信息
                Location location = new Location(PostDetailActivity.this);
                location.getCurrentLocation(new Location.LocationCallback() {
                    @Override
                    public void onLocationReceived(double latitude, double longitude, String province, String city, String address) {
                        /* 由于地理位置的获取是异步的，所以发送数据的逻辑写到回调函数里 */
                        System.out.println(address);

                        mContentType = 0;

                        String targetUrl = "http://121.43.110.176:8000/api/comment";

                        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                        Context context = getApplicationContext();

                        builder.addFormDataPart("user_id", user_id);
                        builder.addFormDataPart("post_id", post_id);
                        builder.addFormDataPart("text", text);
                        builder.addFormDataPart("content_type",String.valueOf(mContentType));
                        builder.addFormDataPart("ip_address",province);
                        RequestBody requestBody = builder.build();

                        DataSender.sendDataToServer(requestBody, targetUrl, new DataSender.DataSenderCallback() {
                            @Override
                            public void onSuccess(JSONObject jsonObject) {
                                // 网络请求成功
                                try {
                                    int code = jsonObject.getInt("code");
                                    String errorMsg = jsonObject.getString("error_msg");
                                    System.out.println("our code :" + code);
                                    if (code != 0){

                                        String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                                        MyToast.show(context,msg);

                                    } else {
                                        // 发表成功
                                        MyToast.show(context, "评论成功");
                                        PostDetailActivity.this.runOnUiThread(new Runnable() {
                                            @SuppressLint("NotifyDataSetChanged")
                                            @Override
                                            public void run() {
                                                // 更新UI组件
                                                commentAdapter.notifyDataSetChanged();
                                                commentsRecycleView.setAdapter(commentAdapter);
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

                //----------------------------------------------

                /* 2.处理UI */

                // 不显示“评论”按钮
                postDetailCommentButton.setVisibility(View.GONE);
                // 收起键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(postDetailCommentEditText.getWindowToken(), 0);
                // 重置评论框
                postDetailCommentEditText.setText("");

            }
        });

        // 点击“删除帖子”按钮
        postDetailDeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO 换成删除逻辑的

                String targetUrl = String.format("" +
                        "http://121.43.110.176:8000/api/post/delete?user_id=%s&post_id=%s",user_id,post_id);
                System.out.println("delete url:" + targetUrl);
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                Context context = getApplicationContext();

                builder.addFormDataPart("user_id", user_id);
                builder.addFormDataPart("post_id", post_id);
                RequestBody requestBody = builder.build();

                DataSender.sendDataToServer(requestBody, targetUrl, new DataSender.DataSenderCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        // 网络请求成功
                        try {
                            int code = jsonObject.getInt("code");
                            String errorMsg = jsonObject.getString("error_msg");
                            System.out.println("our code :" + code);
                            if (code != 0){

                                String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                                MyToast.show(context,msg);
                            } else {
                                MyToast.show(context, "删除成功");
                                // 跳转到主页
                                Intent intent = new Intent(PostDetailActivity.this, HomeActivity.class);
                                startActivity(intent);
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
        });

        // 点击图像显示大图
        postDetailMediaImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 点击显示大图

            }
        });

    }
    public static void processComments(JSONObject data, ArrayList<Comment> commentList) {
        try {
            JSONArray jsonArray = data.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                // 获取条目的各个字段值
                String id = item.getString("id");
                String content_type = item.getString("content_type");
                String user_id = item.getString("user_id");
                String username = item.getString("username");
                String user_picture = item.getString("user_picture");
                String text = item.getString("text");
                String media_url = item.getString("media_url");
                String date = item.getString("date");
                String ip_address = item.getString("ip_address");
                Comment comment = new Comment(id, user_id, username, user_picture, text, content_type, media_url,ip_address, date);
                commentList.add(comment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void processAndSetPost(JSONObject data){
        try {

            String id = data.getString("id");
            String uid = data.getString("user_id");
            String username = data.getString("username");
            String user_picture = data.getString("user_picture");
            String title = data.getString("title");
            String text = data.getString("text");
            String content_type = data.getString("content_type");
            String media_url = data.getString("media_url");
            String date = data.getString("date");
            double location_x = data.getDouble("location_x");
            double location_y = data.getDouble("location_y");
            String ip_address = data.getString("ip_address");

            // 1. 展示文本信息
            PostDetailActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 更新UI组件的代码
                    postDetailUsernameTextView.setText(username);
                    postDetailIPTextView.setText(ip_address);
                    titleTextView.setText(title);
                    contentTextView.setText(text);
                }
            });

            // 2.获取图片信息
            OkHttpClient client = new OkHttpClient();

            // 2.1 获取头像图片
            Request user_request = new Request.Builder()
                    .url(user_picture)
                    .build();
            client.newCall(user_request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // 获取响应数据
                    if (response.isSuccessful()) {
                        // 从响应中获取图片的字节数组
                        byte[] imageData = Objects.requireNonNull(response.body()).bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                postDetailUserImageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
                @Override
                public void onFailure(Call call, IOException e) {
                    // 请求失败处理
                    e.printStackTrace();
                }
            });


            // 2.2 获取媒体图像
            if (content_type.equals("1")){
                Request media_request = new Request.Builder()
                        .url(media_url)
                        .build();
                client.newCall(media_request).enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // 获取响应数据
                        if (response.isSuccessful()) {
                            // 从响应中获取图片的字节数组
                            byte[] imageData = Objects.requireNonNull(response.body()).bytes();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

                            // 更新UI只能放在主线程中
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    postDetailMediaImageView.setVisibility(View.VISIBLE);
                                    postDetailMediaImageView.setImageBitmap(bitmap);
                                }
                            });

                        }
                    }
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // 请求失败处理
                        e.printStackTrace();
                    }
                });
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
