package com.example.locationbasewall.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.locationbasewall.R;
import com.example.locationbasewall.utils.Comment;
import com.example.locationbasewall.utils.DataSender;
import com.example.locationbasewall.utils.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private ArrayList<Comment> mCommentList;
    private CommentAdapter.OnItemClickListener onItemClickListener;
    private Context mContext;
    String mCurrentUid;
    public interface OnItemClickListener {
        void onItemClick(Comment comment);
    }

    public CommentAdapter(Context context, String currentUid, ArrayList<Comment> CommentList, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.mCurrentUid = currentUid;
        this.mCommentList = CommentList;
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
        Comment comment = mCommentList.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView commentUserImageView;
        private TextView commentUsernameTextView;
        private TextView commentIPTextView;
        private TextView commentContentTextView;
        private ImageView commentMediaImageView;
        private Button commentDeleteButton;

        private CommentAdapter.OnItemClickListener onItemClickListener;

        public CommentViewHolder(@NonNull View itemView, CommentAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);
            commentUserImageView = itemView.findViewById(R.id.commentUserImageView);
            commentUsernameTextView = itemView.findViewById(R.id.commentUsernameTextView);
            commentIPTextView = itemView.findViewById(R.id.commentIPTextView);
            commentContentTextView = itemView.findViewById(R.id.commentContentTextView);
            commentMediaImageView = itemView.findViewById(R.id.commentMediaImageView);
            commentDeleteButton = itemView.findViewById(R.id.commentDeleteButton);

            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);

        }

        public void bind(Comment comment) {
            if (mCurrentUid.equals(comment.getUser_id())){
                commentDeleteButton.setVisibility(View.VISIBLE);
            }else {
                commentDeleteButton.setVisibility(View.GONE);
            }

            String user_picture = comment.getUser_picture();
            String mediaUrl = comment.getMedia_url();
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
                                commentUserImageView.setImageBitmap(bitmap);
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


            if (mediaUrl != null && !mediaUrl.equals("")){
                Handler handler = new Handler(Looper.getMainLooper());
                commentMediaImageView.setVisibility(View.VISIBLE);
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        System.out.println("加载评论图片");
                        System.out.println(mediaUrl);
                        int targetWidth = commentMediaImageView.getWidth();
                        int targetHeight = commentMediaImageView.getHeight();
                        RequestOptions requestOptions = new RequestOptions()
                                .centerCrop() // 根据需要进行裁剪或缩放
                                .override(targetWidth, targetHeight); // 设置图片大小为ImageView的大小

                        Glide.with(mContext)
                                .load(mediaUrl)
                                .apply(requestOptions)
                                .into(commentMediaImageView);
                    }
                });
            }else {
                commentMediaImageView.setVisibility(View.GONE);
            }

            // 设置删除逻辑
            commentDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String user_id = comment.getUser_id();
                    String comment_id = comment.getId();
                    String targetUrl = "http://121.43.110.176:8000/api/comment/delete";
                    MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                    Context context = mContext;

                    builder.addFormDataPart("user_id", user_id);
                    builder.addFormDataPart("comment_id", comment_id);
                    RequestBody requestBody = builder.build();

                    DataSender.sendDataToServer(requestBody, targetUrl, new DataSender.DataSenderCallback() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onSuccess(JSONObject jsonObject) {
                            // 网络请求成功
                            try {
                                int code = jsonObject.getInt("code");
                                String errorMsg = jsonObject.getString("error_msg");
                                if (code != 0){

                                    String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                                    MyToast.show(context,msg);
                                } else {
                                    MyToast.show(context, "删除评论成功");
                                    mCommentList.remove(comment);
                                    notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                MyToast.show(context, "JSON错误");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            System.out.println(errorMessage);
                            MyToast.show(context, "网络请求错误");
                        }
                    });
                }
            });

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Comment comment = mCommentList.get(position);
                onItemClickListener.onItemClick(comment);
            }
        }
    }
}
