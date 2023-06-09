package com.example.locationbasewall.adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationbasewall.R;
import com.example.locationbasewall.home.PostDetailActivity;
import com.example.locationbasewall.utils.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private ArrayList<Post> postList;
    private OnItemClickListener onItemClickListener;
    private Activity activity;
    private Context context;


    public interface OnItemClickListener {
        void onItemClick(Post post);
    }
    public PostAdapter(Activity activity, Context context) {
        this.postList = new ArrayList<>();
        this.activity = activity;
        this.context = context;
        this.onItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(Post post) {
                Intent intent = new Intent(activity, PostDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // 用一个新的任务栈存储新的activity
                intent.putExtra("post_id", post.getId());
                intent.putExtra("publisher_id",post.getUid());
                startActivity(context, intent,null);
            }
        };
    }
    public PostAdapter(ArrayList<Post> postList, OnItemClickListener onItemClickListener) {
        this.postList = postList;
        this.onItemClickListener = onItemClickListener;
    }

    public void setPostList(ArrayList<Post> postList) {
        this.postList = postList;
    }
    public ArrayList<Post> getPostList(){
        return this.postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_overview, parent, false);
        return new PostViewHolder(view, this.onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView postOverviewImageView;
        private TextView postOverviewUsernameTextView;
        private TextView postOverviewIPTextView;
        private TextView postOverviewDateTextView;
        private TextView postOverviewDistanceTextView;

        private TextView titleTextView;
        private TextView contentTextView;
        private OnItemClickListener onItemClickListener;

        public PostViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            postOverviewImageView = itemView.findViewById(R.id.postOverviewImageView);
            postOverviewUsernameTextView = itemView.findViewById(R.id.postOverviewUsernameTextView);
            postOverviewIPTextView = itemView.findViewById(R.id.postOverviewIPTextView);
            postOverviewDateTextView = itemView.findViewById(R.id.postOverviewDateTextView);
            postOverviewDistanceTextView = itemView.findViewById(R.id.postOverviewDistanceTextView);
            titleTextView = itemView.findViewById(R.id.postOverviewTitleTextView);
            contentTextView = itemView.findViewById(R.id.postOverviewContentTextView);

            this.onItemClickListener = onItemClickListener;

            // 设置点击事件监听器
            itemView.setOnClickListener(this);
        }

        public void bind(Post post) {
            String user_picture = post.getImageUrl();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(user_picture) // 替换为您的图片链接
                    .build();
            client.newCall(request).enqueue(new Callback() {
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
                                postOverviewImageView.setImageBitmap(bitmap);
                                postOverviewUsernameTextView.setText(post.getUsername());
                                postOverviewIPTextView.setText(post.getAddress());
                                postOverviewDateTextView.setText(post.getDate());
                                postOverviewDistanceTextView.setText(String.format("%s km", post.getDistance()));

                                titleTextView.setText(post.getTitle());
                                contentTextView.setText(post.getText());
                            }
                        });

                    }
                }
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    // 请求失败处理
                    e.printStackTrace();
                }
            });

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Post post = postList.get(position);
                onItemClickListener.onItemClick(post);
            }
        }
    }
}
