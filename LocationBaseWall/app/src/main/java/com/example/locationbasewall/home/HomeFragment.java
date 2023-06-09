package com.example.locationbasewall.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.PostAdapter;
import com.example.locationbasewall.utils.DataGetter;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private RecyclerView homeFragmentRecyclerView;
    private EditText homeFragmentSearchEditText;
    private Button homeFragmentSearchButton;
    private SwipeRefreshLayout homeFragmentSwipeRefreshLayout;
    private PostAdapter mPostAdapter;

    private Activity mActivity;
    private Context mContext;

    private int page_num = 1;
    private int page_size = 10;
    private String mKey_words="";

    // 线程安全的锁来修改page_num
    private final Object lock = new Object();

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mContext = getContext();
        mActivity = requireActivity();

        homeFragmentSearchEditText = view.findViewById(R.id.homeFragmentSearchEditText);
        homeFragmentSearchButton = view.findViewById(R.id.homeFragmentSearchButton);

        homeFragmentRecyclerView = view.findViewById(R.id.homeFragmentRecyclerView);
        homeFragmentSwipeRefreshLayout = view.findViewById(R.id.homeFragmentSwipeRefreshLayout);

        homeFragmentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mPostAdapter = new PostAdapter(mActivity, mContext);

        homeFragmentRecyclerView.setAdapter(mPostAdapter);
        // 加载初始页面
        reloadPage();


        // 上拉到底部的时候加载更多
        homeFragmentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 加锁，免得同时访问太多
                getMorePost();

            }
        });
        // 下拉刷新
        homeFragmentSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新
                reloadPage();
            }
        });

        homeFragmentSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKey_words = homeFragmentSearchEditText.getText().toString();
                reloadPage();
            }
        });

        return view;
    }
    private void reloadPage(){
        synchronized (lock) {
            // 在这里执行下拉刷新的操作，比如发送网络请求
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 每次刷新最新内容，都会重置帖子列表
                    mPostAdapter.getPostList().clear();
                    page_num = 1;  // 恢复页码

                    int post_num = DataGetter.getLocationAndPostOverviewData(
                            Objects.requireNonNull(mActivity), mPostAdapter,
                            mKey_words, page_num,page_size,-1);

                    // 恢复初始信息
                    if (post_num == page_size){
                        page_num++;
                    }
                    // 当操作完成后，调用 setRefreshing(false) 来停止刷新动画
                    homeFragmentSwipeRefreshLayout.setRefreshing(false);
                }
            }, 1000); // 延迟x毫秒后停止刷新
        }
    }

    private void getMorePost(){
        // 加锁，免得同时访问太多
        synchronized (lock) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) homeFragmentRecyclerView.getLayoutManager();
            int totalItemCount = Objects.requireNonNull(layoutManager).getItemCount();
            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
            if (lastVisibleItemPosition == totalItemCount - 1) {
                // 用户滑动到列表底部，加载下一页
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 加载更多数据完成后更新RecyclerView的适配器等操作
                        int post_num = DataGetter.getLocationAndPostOverviewData(
                                Objects.requireNonNull(mActivity), mPostAdapter,
                                mKey_words, page_num,page_size,-1);
                        if (post_num == page_size){
                            page_num++;
                        }
                        // 停止加载更多动画
                        homeFragmentSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }

        }
    }
}
