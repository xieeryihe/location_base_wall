package com.example.locationbasewall.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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
import com.example.locationbasewall.utils.Comment;
import com.example.locationbasewall.utils.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private ArrayList<Comment> commentList;
    private CommentAdapter.OnItemClickListener onItemClickListener;
    public interface OnItemClickListener {
        void onItemClick(Comment comment);
    }

    public CommentAdapter(ArrayList<Comment> commentList, OnItemClickListener onItemClickListener) {
        this.commentList = commentList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view, this.onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView commentImageView;
        private TextView commentUsernameTextView;
        private TextView commentIPTextView;
        private TextView commentContentTextView;

        private CommentAdapter.OnItemClickListener onItemClickListener;

        public CommentViewHolder(@NonNull View itemView, CommentAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);
            commentImageView = itemView.findViewById(R.id.commentImageView);
            commentUsernameTextView = itemView.findViewById(R.id.commentUsernameTextView);
            commentIPTextView = itemView.findViewById(R.id.commentIPTextView);
            commentContentTextView = itemView.findViewById(R.id.commentContentTextView);
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);

        }

        public void bind(Comment comment) {

            String user_picture = comment.getUser_picture();
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
                                commentImageView.setImageBitmap(bitmap);
                                commentUsernameTextView.setText(comment.getUsername());
                                commentIPTextView.setText(comment.getIp_address());
                                commentContentTextView.setText(comment.getText());
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
                Comment comment = commentList.get(position);
                onItemClickListener.onItemClick(comment);
            }
        }
    }
}
