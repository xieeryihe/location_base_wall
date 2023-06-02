package com.example.locationbasewall.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.CommentAdapter;
import com.example.locationbasewall.utils.Comment;
import com.example.locationbasewall.utils.DataGetter;
import com.example.locationbasewall.utils.Location;
import com.example.locationbasewall.utils.MyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PostDetailActivity extends AppCompatActivity {
    private TextView postDetailUsernameTextView;
    private TextView postDetailIPTextView;
    private TextView titleTextView;
    private TextView contentTextView;
    private EditText postDetailCommentEditText;
    private Button postDetailCommentButton;

    private ImageView postDetailMediaImageView;
    private VideoView postDetailMediaVideoView;

    private RecyclerView commentsRecycleView;


    private ArrayList<Comment> commentList; // 评论列表（复用了Post的构造）
    private CommentAdapter commentAdapter;


    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        postDetailUsernameTextView = findViewById(R.id.postDetailUsernameTextView);
        postDetailIPTextView = findViewById(R.id.postDetailIPTextView);
        titleTextView = findViewById(R.id.postDetailTitleTextView);
        contentTextView = findViewById(R.id.postDetailContentTextView);
        postDetailCommentEditText = findViewById(R.id.postDetailCommentEditText);
        postDetailCommentButton = findViewById(R.id.postDetailCommentButton);
        postDetailMediaImageView = findViewById(R.id.postDetailMediaImageView);
        postDetailMediaVideoView = findViewById(R.id.postDetailMediaVideoView);
        commentsRecycleView = findViewById(R.id.commentsRecycleView);


//        titleTextView.setText("标题");
//        contentTextView.setText("111\n222\n333");

        // 再发一次请求，获取帖子详情数据
        String post_id = getIntent().getStringExtra("post_id");
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
                        MyToast.show(context, "获取数据成功");
                        processAndSetPost(data);


//                        commentList = new ArrayList<>(); // 初始化帖子数据列表
//                        processComments(data,commentList);
//                        commentAdapter = new CommentAdapter(commentList, new CommentAdapter.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(Comment comment) {
//                                // 评论的点击事件，同样是唤起评论键盘那些
//                            }
//
//
//                        });
//                        PostDetailActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                // 更新UI组件的代码
//                                commentAdapter.notifyDataSetChanged();
//                                commentsRecycleView.setAdapter(commentAdapter);
//                            }
//                        });
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

        int page_num = 1;
        int page_size = 10;

        targetUrl = String.format("" +
                        "http://121.43.110.176:8000/api/comment?" +
                        "page_num=%d&page_size=%d&post_id=%d",
                page_num, page_size, Integer.valueOf(post_id));
        System.out.println(targetUrl);




        // 处理评论部分
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
                        JSONObject data = jsonObject.getJSONObject("items");
                        MyToast.show(context, "获取数据成功");

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

        postDetailCommentButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                /* 1. 整理并发送数据 */
                // 获取位置信息
                Location location = new Location(PostDetailActivity.this);
                location.getCurrentLocation(new Location.LocationCallback() {
                    @Override
                    public void onLocationReceived(double latitude, double longitude, String province, String city, String address) {
                        System.out.println(address);

                        /* 由于地理位置的获取是异步的，所以发送数据的逻辑写到回调函数里 */

                        // TODO 发表评论

                        // 获取评论信息
                        String comment = postDetailCommentEditText.getText().toString();
                        // 发送数据到服务器

                        // 正常返回后，再显示在用户界面

                        // commentList.add(new Comment("User4", "Location4", "Comment 4", 3.456, 7.890, "https://example.com/avatar3.png"));

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
                // 刷新适配器，用以显示评论
                commentAdapter.notifyItemInserted(commentList.size() - 1);
                // 滚动到最后一个评论
                commentsRecycleView.scrollToPosition(commentList.size() - 1);

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



            if (content_type.equals("1")){
                System.out.println("富文本");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
