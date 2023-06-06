package com.example.locationbasewall.adapter;

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
import com.example.locationbasewall.utils.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private ArrayList<Post> postList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    public PostAdapter(ArrayList<Post> postList, OnItemClickListener onItemClickListener) {
        this.postList = postList;
        this.onItemClickListener = onItemClickListener;
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
        private TextView titleTextView;
        private TextView contentTextView;
        private OnItemClickListener onItemClickListener;

        public PostViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            postOverviewImageView = itemView.findViewById(R.id.postOverviewImageView);
            postOverviewUsernameTextView = itemView.findViewById(R.id.postOverviewUsernameTextView);
            postOverviewIPTextView = itemView.findViewById(R.id.postOverviewIPTextView);
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
                                postOverviewIPTextView.setText(String.format("%s %s", post.getAddress(), post.getDate()));
                                titleTextView.setText(post.getTitle());
                                contentTextView.setText(post.getText());
                            }
                        });

                    }
                }
                @Override
                public void onFailure(Call call, IOException e) {
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
