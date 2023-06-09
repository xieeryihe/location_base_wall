package com.example.locationbasewall.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.SubcommentAdapter;
import com.example.locationbasewall.utils.Comment;
import com.example.locationbasewall.utils.DataGetter;
import com.example.locationbasewall.utils.DataSender;
import com.example.locationbasewall.utils.LocalUserInfo;
import com.example.locationbasewall.utils.Location;
import com.example.locationbasewall.utils.Media;
import com.example.locationbasewall.utils.MyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class CommentDetailActivity extends AppCompatActivity {
    private Context mContext;
    private ImageView commentDetailUserImageView;
    private TextView commentDetailUsernameTextView;
    private TextView commentDetailIPTextView;
    private TextView commentDetailContentTextView;
    private ImageView commentDetailMediaImageView;
    private RecyclerView subcommentRecyclerView;

    //-- 接下来是评论部分

    private EditText commentDetailCommentEditText;
    private ImageView commentDetailCommentImageView;

    private Button commentDetailCommentButton;

    // 其他部分

    private Uri mCommentMediaUri = null;  // 评论界面，上传媒体资源用到的uri
    private ActivityResultLauncher<Intent> commentGalleryLauncher;  // 评论的launcher
    private ArrayList<Comment> subcommentList; // 评论列表
    private SubcommentAdapter subcommentAdapter;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_detail);

        mContext = getApplicationContext();


        commentDetailUserImageView = findViewById(R.id.commentDetailUserImageView);
        commentDetailUsernameTextView = findViewById(R.id.commentDetailUsernameTextView);
        commentDetailIPTextView = findViewById(R.id.commentDetailIPTextView);
        commentDetailContentTextView = findViewById(R.id.commentDetailContentTextView);
        commentDetailMediaImageView = findViewById(R.id.commentDetailMediaImageView);
        subcommentRecyclerView = findViewById(R.id.subcommentRecyclerView);
        subcommentRecyclerView.setLayoutManager(new LinearLayoutManager(CommentDetailActivity.this));

        commentDetailCommentEditText = findViewById(R.id.commentDetailCommentEditText);
        commentDetailCommentImageView = findViewById(R.id.commentDetailCommentImageView);  // 子评论不需要媒体内容，不过还是放着了
        commentDetailCommentButton = findViewById(R.id.commentDetailCommentButton);


        Comment comment = (Comment) getIntent().getSerializableExtra("parentComment");
        commentDetailUsernameTextView.setText(comment.getUsername());
        commentDetailIPTextView.setText(comment.getIp_address());
        commentDetailContentTextView.setText(comment.getText());

        String parentImageUrl = comment.getUser_picture();
        String parentMediaUrl = comment.getMedia_url();
        LocalUserInfo localUserInfo = new LocalUserInfo(mContext);
        String user_id = localUserInfo.getId();
        String comment_id = comment.getId();


        // 1. 父评论部分
        // 1.1 获取头像图片
        runOnUiThread(() -> {
            // 获取布局中ImageView的宽度和高度
            int targetWidth = commentDetailUserImageView.getWidth();
            int targetHeight = commentDetailUserImageView.getHeight();
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop() // 根据需要进行裁剪或缩放
                    .override(100, 100); // 设置图片大小为ImageView的大小

            Glide.with(mContext)
                    .load(parentImageUrl)
                    .apply(requestOptions)
                    .into(commentDetailUserImageView);
        });

        // 1.2 获取评论媒体缩略图

        if (comment.getContent_type().equals("1")){
            if (Media.isImageFile(parentMediaUrl)) {
                // 文件是图片
                runOnUiThread(() -> {
                    RequestOptions requestOptions = new RequestOptions()
                            .centerCrop() // 根据需要进行裁剪或缩放
                            .override(200, 200); // 设置缩略图的大小，这里是200x200像素

                    Glide.with(mContext)
                            .load(parentMediaUrl)
                            .apply(requestOptions)
                            .into(commentDetailMediaImageView);
                    Glide.with(mContext)
                            .load(parentMediaUrl)
                            .apply(requestOptions)
                            .into(commentDetailMediaImageView);
                });

            } else if (Media.isVideoFile(parentMediaUrl)) {
                // 文件是视频
                runOnUiThread(() -> {
                    RequestOptions requestOptions = new RequestOptions()
                            .frame(10000) // 设置为一个足够大的帧时间，以获取视频的缩略图
                            .centerCrop() // 根据需要进行裁剪或缩放
                            .override(200, 200) // 设置缩略图的大小，这里是200x200像素
                            .diskCacheStrategy(DiskCacheStrategy.ALL); // 仅使用数据缓存，不使用磁盘缓存完整视频数据

                    Glide.with(mContext)
                            .asBitmap() // 显示为Bitmap对象
                            .load(parentMediaUrl)
                            .apply(requestOptions)
                            .into(commentDetailMediaImageView);
                });
            } else {
                // 文件类型未知
                MyToast.show(mContext,"文件类型未知");
            }
        } else {
            commentDetailMediaImageView.setVisibility(View.GONE);
        }

        // 2. 子评论部分
        // 2. 获取评论数据部分
        int page_num = 1;
        int page_size = 10;
        @SuppressLint("DefaultLocale") String targetUrl = String.format("" +
                        "http://121.43.110.176:8000/api/subcomment?" +
                        "page_num=%d&page_size=%d&comment_id=%d",
                page_num, page_size, Integer.valueOf(comment.getId()));


        DataGetter.getDataFromServer(targetUrl, new DataGetter.DataGetterCallback() {
            final Context context = getApplicationContext();
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    int code = jsonObject.getInt("code");
                    String errorMsg = jsonObject.getString("error_msg");
                    if (code != 0 && code != 1 && code != 2){
                        // 获取数据失败
                        String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                        MyToast.show(context,msg);
                    } else {
                        JSONObject data = jsonObject.getJSONObject("data");
                        // MyToast.show(context, errorMsg);
                        subcommentList = new ArrayList<>(); // 初始化帖子数据列表
                        processComments(data, subcommentList);
                        subcommentAdapter = new SubcommentAdapter(mContext,user_id, subcommentList);
                        runOnUiThread(() -> {
                            // 更新UI组件的代码
                            subcommentRecyclerView.setAdapter(subcommentAdapter);
                            subcommentAdapter.notifyDataSetChanged();
                        });

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


        // 3. 评论部分

        // 一般情况下，“评论”按钮不显示，点击编辑框之后才显现
        commentDetailCommentEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // 唤起键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                commentDetailCommentEditText.requestFocus(); // 聚焦光标
                return true;
            }
            return false;
        });

        // 点击“评论”按钮，发送评论
        commentDetailCommentButton.setOnClickListener(v -> {
            /* 1. 整理并发送数据 */
            String text = commentDetailCommentEditText.getText().toString();

            // 获取位置信息
            Location location = new Location(CommentDetailActivity.this);
            location.getCurrentLocation(new Location.LocationCallback() {
                @Override
                public void onLocationReceived(double latitude, double longitude, String province, String city, String address) {
                    /* 由于地理位置的获取是异步的，所以发送数据的逻辑写到回调函数里 */

                    String targetUrl = "http://121.43.110.176:8000/api/subcomment";

                    MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                    builder.addFormDataPart("user_id", user_id);
                    builder.addFormDataPart("comment_id", comment_id);
                    builder.addFormDataPart("text", text);
                    builder.addFormDataPart("ip_address",province);
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
                                    MyToast.show(mContext,msg);

                                } else {
                                    // 发表成功
                                    MyToast.show(mContext, "评论成功");
                                    runOnUiThread(() -> {
                                        // 更新UI组件
                                        subcommentAdapter.notifyDataSetChanged();
                                        subcommentRecyclerView.setAdapter(subcommentAdapter);
                                        // 发表成功后，重置Uri
                                        mCommentMediaUri = null;
                                    });
                                }
                            } catch (JSONException e) {
                                MyToast.show(mContext, "JSON错误");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            System.out.println(errorMessage);
                            MyToast.show(mContext, "网络请求错误");
                        }
                    });

                }

                @Override
                public void onLocationFailed(String errorMsg) {
                    System.out.println("Failed to get location: " + errorMsg);
                }
            });

            //----------------------------------------------

            /* 2.处理UI */

            // 不显示“评论”按钮
            // 收起键盘
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(commentDetailCommentEditText.getWindowToken(), 0);
            // 重置评论框
            commentDetailCommentEditText.setText("");
            commentDetailCommentImageView.setVisibility(View.GONE);
            // 设置评论为空
        });



    }

    public static void processComments(JSONObject data, ArrayList<Comment> commentList) {
        try {
            JSONArray jsonArray = data.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                // 获取条目的各个字段值
                String id = item.getString("id");
                String user_id = item.getString("user_id");
                String username = item.getString("username");
                String user_picture = item.getString("user_picture");
                String text = item.getString("text");
                String date = item.getString("date");
                String ip_address = item.getString("ip_address");
                Comment comment = new Comment(id, user_id, username, user_picture, text, "0", null,ip_address, date);
                commentList.add(comment);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}