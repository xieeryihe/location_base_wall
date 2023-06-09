package com.example.locationbasewall.home;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.PostAdapter;
import com.example.locationbasewall.utils.DataGetter;

import java.util.Objects;

public class NearbyFragment extends Fragment {
    private TextView nearbyFragmentDistanceTextView;
    private RecyclerView nearbyFragmentRecyclerView;
    private SeekBar nearbyFragmentDistanceSeekBar;
    private Button nearbyFragmentConfirmButton;
    private SwipeRefreshLayout nearbyFragmentSwipeRefreshLayout;
    private PostAdapter mPostAdapter;

    private Activity mActivity;
    private Context mContext;

    private int page_num = 1;
    private int page_size = 10;
    private int mSeekbarNum = 0;  // 拖动条上的数据
    private int mDistance = 0;  // 实际距离参数

    // 线程安全的锁来修改page_num
    private final Object lock = new Object();

    public NearbyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);

        mContext = getContext();
        mActivity = requireActivity();

        nearbyFragmentDistanceTextView = view.findViewById(R.id.nearbyFragmentDistanceTextView);
        nearbyFragmentDistanceSeekBar = view.findViewById(R.id.nearbyFragmentDistanceSeekBar);
        nearbyFragmentConfirmButton = view.findViewById(R.id.nearbyFragmentConfirmButton);

        nearbyFragmentRecyclerView = view.findViewById(R.id.nearbyFragmentRecyclerView);
        nearbyFragmentSwipeRefreshLayout = view.findViewById(R.id.nearbyFragmentSwipeRefreshLayout);

        nearbyFragmentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mPostAdapter = new PostAdapter(mActivity, mContext);

        nearbyFragmentRecyclerView.setAdapter(mPostAdapter);
        // 加载初始页面
        reloadPage();

        // 拖动条获取数据
        nearbyFragmentDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 在这里获取选定的数字
                mSeekbarNum = progress;
                nearbyFragmentDistanceTextView.setText(String.valueOf(mSeekbarNum));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 当用户开始滑动SeekBar时调用
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 当用户停止滑动SeekBar时调用
            }
        });

        // 上拉到底部的时候加载更多
        nearbyFragmentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 加锁，免得同时访问太多
                getMorePost();

            }
        });

        // 下拉刷新
        nearbyFragmentSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新
                reloadPage();
            }
        });

        // 点击确定按钮
        nearbyFragmentConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确定按钮被点击时，才设置距离参数
                mDistance = mSeekbarNum;
                reloadPage();
            }
        });

        return view;
    }

    private void reloadPage() {
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
                            "", page_num, page_size, mDistance);

                    // 恢复初始信息
                    if (post_num == page_size) {
                        page_num++;
                    }
                    // 当操作完成后，调用 setRefreshing(false) 来停止刷新动画
                    nearbyFragmentSwipeRefreshLayout.setRefreshing(false);
                }
            }, 1000); // 延迟x毫秒后停止刷新
        }
    }

    private void getMorePost() {
        // 加锁，免得同时访问太多
        synchronized (lock) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) nearbyFragmentRecyclerView.getLayoutManager();
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
                                "", page_num, page_size, mDistance);
                        if (post_num == page_size) {
                            page_num++;
                        }
                        // 停止加载更多动画
                        nearbyFragmentSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }

        }
    }
}
