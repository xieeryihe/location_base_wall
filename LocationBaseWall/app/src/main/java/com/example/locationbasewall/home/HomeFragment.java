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
public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList; // 帖子数据列表

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        postList = new ArrayList<>(); // 初始化帖子数据列表
        postAdapter = new PostAdapter(postList, new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Post post) {
                // 处理点击事件，跳转到详情页或执行其他操作
                // 在此处启动 PostDetailActivity，并传递帖子数据
                Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(postAdapter);

        // 添加示例帖子数据到帖子数据列表
        postList.add(new Post("1", "user123", "帖子标题1", "帖子内容1", 1, 12.345, 67.890, null, "New York"));
        postList.add(new Post("2", "user456", "帖子标题2", "帖子内容2", 2, 34.567, 89.012, null, "Paris"));
        postList.add(new Post("3", "user789", "帖子标题3", "帖子内容3", 1, 56.789, 90.123, null, "默认地址"));
        // ...

        // 通知适配器数据集发生变化
        postAdapter.notifyDataSetChanged();

        return view;
    }
}
