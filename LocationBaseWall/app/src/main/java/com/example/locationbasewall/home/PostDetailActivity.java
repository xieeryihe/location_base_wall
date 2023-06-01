package com.example.locationbasewall.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.CommentAdapter;
import com.example.locationbasewall.utils.Comment;
import com.example.locationbasewall.utils.Location;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView contentTextView;
    private EditText postDetailCommentEditText;
    private Button postDetailCommentButton;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        titleTextView = findViewById(R.id.postDetailTitleTextView);
        contentTextView = findViewById(R.id.postDetailContentTextView);
        postDetailCommentEditText = findViewById(R.id.postDetailCommentEditText);
        postDetailCommentButton = findViewById(R.id.postDetailCommentButton);
        titleTextView.setText("标题");
        contentTextView.setText("111\n222\n333");
        List<Comment> commentList = new ArrayList<>();
        commentList.add(new Comment("User1", "Location1", "Comment 1", 1.234, 5.678, "https://example.com/avatar1.png"));
        commentList.add(new Comment("User2", "Location2", "Comment 2", 2.345, 6.789, "https://example.com/avatar2.png"));
        commentList.add(new Comment("User3", "Location3", "Comment 3", 3.456, 7.890, "https://example.com/avatar3.png"));

        RecyclerView recyclerView = findViewById(R.id.commentsRecycleView);
        CommentAdapter commentAdapter = new CommentAdapter(commentList, this);
        recyclerView.setAdapter(commentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


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
                double  latitudeData;
                // 获取位置信息
                Location location = new Location(PostDetailActivity.this);
                location.getCurrentLocation(new Location.LocationCallback() {
                    @Override
                    public void onLocationReceived(double latitude, double longitude, String province, String city, String address) {
                        System.out.println("Latitude: " + latitude + ", Longitude: " + longitude);
                        System.out.println(address);

                        /* 由于地理位置的获取是异步的，所以发送数据的逻辑写到回调函数里 */

                        // 获取评论信息
                        String comment = postDetailCommentEditText.getText().toString();
                        // 发送数据到服务器



                        // 正常返回后，再显示在用户界面

                        commentList.add(new Comment("User4", "Location4", "Comment 4", 3.456, 7.890, "https://example.com/avatar3.png"));

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
                recyclerView.scrollToPosition(commentList.size() - 1);

            }
        });

    }
}
