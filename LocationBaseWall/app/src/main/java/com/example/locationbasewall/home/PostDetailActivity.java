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

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView contentTextView;
    private EditText postDetailCommentEditText;
    private Button postDetailCommentButton;
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
                postDetailCommentButton.setVisibility(View.GONE);

                // 收起键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(postDetailCommentEditText.getWindowToken(), 0);

                // TODO 发送评论数据


                // 重置评论框
                postDetailCommentEditText.setText("");
            }
        });

        // TODO 获取位置信息



    }
}
