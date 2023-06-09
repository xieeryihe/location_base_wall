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
import android.widget.LinearLayout;
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

public class SubcommentAdapter extends RecyclerView.Adapter<SubcommentAdapter.SubcommentViewHolder> {
    private ArrayList<Comment> mSubcommentList;
    private SubcommentAdapter.OnItemClickListener onItemClickListener;
    private Context mContext;
    String mCurrentUid;

    public interface OnItemClickListener {
        void onItemClick(Comment comment);
    }

    public SubcommentAdapter(Context context, String currentUid, ArrayList<Comment> subcommentList) {
        this.mContext = context;
        this.mCurrentUid = currentUid;
        this.mSubcommentList = subcommentList;
    }

    @NonNull
    @Override
    public SubcommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subcomment, parent, false);
        return new SubcommentViewHolder(view, this.onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SubcommentViewHolder holder, int position) {
        Comment comment = mSubcommentList.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return mSubcommentList.size();
    }

    public class SubcommentViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout item_subcomment;
        private ImageView subcommentUserImageView;
        private TextView subcommentUsernameTextView;
        private TextView subcommentIPTextView;
        private TextView subcommentContentTextView;
        private ImageView subcommentMediaImageView;
        private Button subcommentDeleteButton;

        public SubcommentViewHolder(@NonNull View itemView, SubcommentAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);
            item_subcomment = itemView.findViewById(R.id.item_subcomment);
            subcommentUserImageView = itemView.findViewById(R.id.subcommentUserImageView);
            subcommentUsernameTextView = itemView.findViewById(R.id.subcommentUsernameTextView);
            subcommentIPTextView = itemView.findViewById(R.id.subcommentIPTextView);
            subcommentContentTextView = itemView.findViewById(R.id.subcommentContentTextView);
            subcommentMediaImageView = itemView.findViewById(R.id.subcommentMediaImageView);
            subcommentDeleteButton = itemView.findViewById(R.id.subcommentDeleteButton);
        }

        public void bind(Comment comment) {
            if (mCurrentUid.equals(comment.getUser_id())) {
                subcommentDeleteButton.setVisibility(View.VISIBLE);
            } else {
                subcommentDeleteButton.setVisibility(View.GONE);
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
                                subcommentUserImageView.setImageBitmap(bitmap);
                                subcommentUsernameTextView.setText(comment.getUsername());
                                subcommentIPTextView.setText(comment.getIp_address());
                                subcommentContentTextView.setText(comment.getText());
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

            if (mediaUrl != null && !mediaUrl.equals("")) {
                Handler handler = new Handler(Looper.getMainLooper());
                subcommentMediaImageView.setVisibility(View.VISIBLE);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("加载评论图片");
                        System.out.println(mediaUrl);
                        int targetWidth = subcommentMediaImageView.getWidth();
                        int targetHeight = subcommentMediaImageView.getHeight();
                        RequestOptions requestOptions = new RequestOptions()
                                .centerCrop() // 根据需要进行裁剪或缩放
                                .override(targetWidth, targetHeight); // 设置图片大小为ImageView的大小

                        Glide.with(mContext)
                                .load(mediaUrl)
                                .apply(requestOptions)
                                .into(subcommentMediaImageView);
                    }
                });
            } else {
                subcommentMediaImageView.setVisibility(View.GONE);
            }

            // 设置删除逻辑
            subcommentDeleteButton.setOnClickListener(new View.OnClickListener() {
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
                                if (code != 0) {
                                    String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                                    MyToast.show(context, msg);
                                } else {
                                    MyToast.show(context, "删除评论成功");
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
    }
}
