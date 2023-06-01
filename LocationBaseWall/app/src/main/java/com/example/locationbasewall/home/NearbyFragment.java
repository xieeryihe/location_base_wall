package com.example.locationbasewall.home;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.PostAdapter;
import com.example.locationbasewall.utils.Post;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NearbyFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList; // 帖子数据列表

    public NearbyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        postList = new ArrayList<>(); // 初始化帖子数据列表
        postAdapter = new PostAdapter(postList, post -> {
            // 处理点击事件，跳转到详情页或执行其他操作
            // 在此处启动 PostDetailActivity，并传递帖子数据
            Intent intent = new Intent(getActivity(), PostDetailActivity.class);
            startActivity(intent);
        });
        recyclerView.setAdapter(postAdapter);

        postList.add(new Post("4", "user111", "帖子标题4", "帖子内容4", 2, 78.901, 23.456, null, "London"));
        postList.add(new Post("5", "user222", "帖子标题5", "帖子内容5", 1, 98.765, 43.210, null, "默认地址"));

        // 通知适配器数据集发生变化
        postAdapter.notifyDataSetChanged();

        return view;
    }
}