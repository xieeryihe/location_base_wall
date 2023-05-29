package com.example.locationbasewall.home;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.PostAdapter;
import com.example.locationbasewall.data.Post;

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
        postAdapter = new PostAdapter(postList); // 创建帖子适配器，并传入帖子数据列表
        recyclerView.setAdapter(postAdapter);

        // 添加示例帖子数据到帖子数据列表
        postList.add(new Post("帖子标题4", "帖子内容4"));
        postList.add(new Post("帖子标题5", "帖子内容5"));
        postList.add(new Post("帖子标题6", "帖子内容6"));
        // ...

        // 通知适配器数据集发生变化
        postAdapter.notifyDataSetChanged();

        return view;
    }
}