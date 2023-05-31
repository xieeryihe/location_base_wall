package com.example.locationbasewall.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.CommentAdapter;
import com.example.locationbasewall.data.Comment;
import com.example.locationbasewall.data.Post;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView contentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        titleTextView = findViewById(R.id.postDetailTitleTextView);
        contentTextView = findViewById(R.id.postDetailContentTextView);

        // 获取传递的数据
        // 这块详细数据通过发送网络请求获取
//        Intent intent = getIntent();
//        if (intent != null) {
//            Post post = (Post) intent.getSerializableExtra("post");
//            if (post != null) {
//                // 设置标题和内容
//                titleTextView.setText(post.getTitle());
//                contentTextView.setText(post.getContent());
//            }
//        }
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


    }
}
