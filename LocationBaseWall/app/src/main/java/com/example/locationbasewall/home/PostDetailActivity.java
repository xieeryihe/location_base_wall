package com.example.locationbasewall.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.CommentAdapter;
import com.example.locationbasewall.utils.Comment;
import com.example.locationbasewall.utils.DataGetter;
import com.example.locationbasewall.utils.DataSender;
import com.example.locationbasewall.utils.LocalUserInfo;
import com.example.locationbasewall.utils.Location;
import com.example.locationbasewall.utils.MyToast;
import com.example.locationbasewall.utils.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostDetailActivity extends AppCompatActivity {
    private ImageView postDetailUserImageView;
    private TextView postDetailUsernameTextView;
    private TextView postDetailIPTextView;

    private Button postDetailDeletePostButton;  // 删除帖子按钮
    private Button postDetailEditPostButton;  // 修改帖子按钮
    private Button postDetailSavePostButton;  // 保存帖子按钮
    private Button postDetailCancelPostButton;  // 取消修改按钮
    private TextView postDetailTitleTextView;  // 显示和修改标题的视图
    private EditText postDetailTitleEditView;
    private TextView postDetailContentTextView;  // 显示和修改内容的视图
    private EditText postDetailContentEditView;

    private EditText postDetailCommentEditText;
    private Button postDetailCommentButton;

    private FrameLayout postDetailEditImageFrameLayout;

    private ImageView postDetailShowImageView;  // 显示和修改图片的视图
    private ImageView postDetailEditImageView;
    private ImageView postDetailEditCloseIcon;

    private RecyclerView commentsRecycleView;
    private ArrayList<Comment> commentList; // 评论列表（复用了Post的构造）
    private CommentAdapter commentAdapter;

    private Post mPost;
    private Uri mediaUri;
    private int mEditContentType = 0;  // 用于记录修改帖子后的文本类型
    private Context mContext;


    private ActivityResultLauncher<Intent> galleryLauncher;


    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        mContext = getApplicationContext();

        postDetailUserImageView = findViewById(R.id.postDetailUserImageView);
        postDetailUsernameTextView = findViewById(R.id.postDetailUsernameTextView);
        postDetailIPTextView = findViewById(R.id.postDetailIPTextView);

        postDetailDeletePostButton = findViewById(R.id.postDetailDeletePostButton);
        postDetailEditPostButton = findViewById(R.id.postDetailEditPostButton);
        postDetailSavePostButton = findViewById(R.id.postDetailSavePostButton);
        postDetailCancelPostButton = findViewById(R.id.postDetailCancelPostButton);

        postDetailTitleTextView = findViewById(R.id.postDetailTitleTextView);
        postDetailTitleEditView = findViewById(R.id.postDetailTitleEditText);
        postDetailContentTextView = findViewById(R.id.postDetailContentTextView);
        postDetailContentEditView = findViewById(R.id.postDetailContentEditText);

        postDetailEditImageFrameLayout = findViewById(R.id.postDetailEditImageFrameLayout);
        postDetailShowImageView = findViewById(R.id.postDetailShowImageView);
        postDetailEditImageView = findViewById(R.id.postDetailEditImageView);
        postDetailEditCloseIcon = findViewById(R.id.postDetailEditCloseIcon);

        postDetailCommentEditText = findViewById(R.id.postDetailCommentEditText);
        postDetailCommentButton = findViewById(R.id.postDetailCommentButton);

        commentsRecycleView = findViewById(R.id.commentsRecycleView);
        // 要加布局管理器
        commentsRecycleView.setLayoutManager(new LinearLayoutManager(PostDetailActivity.this));


        LocalUserInfo localUserInfo = new LocalUserInfo(getApplicationContext());
        String user_id = localUserInfo.getId();
        String post_id = getIntent().getStringExtra("post_id");
        String publisher_id = getIntent().getStringExtra("publisher_id");

        if (user_id.equals(publisher_id)){
            postDetailDeletePostButton.setVisibility(View.VISIBLE);
            postDetailEditPostButton.setVisibility(View.VISIBLE);
        }else {
            postDetailDeletePostButton.setVisibility(View.GONE);
            postDetailEditPostButton.setVisibility(View.GONE);
        }


        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            mediaUri = data.getData();
                            if (mediaUri != null) {
                                if (isVideoFile(mediaUri)) {
                                    // 是视频文件，获取视频的缩略图
                                    Bitmap thumbnail = getVideoThumbnail(mediaUri);
                                    if (thumbnail != null) {
                                        postDetailEditImageView.setImageBitmap(thumbnail);
                                    }
                                } else {
                                    // 是图像文件，直接显示图像
                                    // postMediaImageView.setImageURI(mediaUri);
                                    Bitmap thumbnail = getImageThumbnail(mediaUri);
                                    if (thumbnail != null) {
                                        postDetailEditImageView.setImageBitmap(thumbnail);
                                    }
                                }

                            }
                        }
                    }
                });

        postDetailEditImageView.setOnClickListener(v -> openGallery());

        postDetailEditCloseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaUri = null;
                MyToast.show(mContext, "已清除原图片");
                @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(R.drawable.default_img);
                postDetailEditImageView.setImageDrawable(drawable);  // 恢复默认图片
            }
        });


        // 1. 获取帖子详情部分
        String targetUrl = "http://121.43.110.176:8000/api/post/" + "?post_id=" + post_id;
        DataGetter.getDataFromServer(targetUrl, new DataGetter.DataGetterCallback() {
            final Context context = getApplicationContext();
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    int code = jsonObject.getInt("code");
                    String errorMsg = jsonObject.getString("error_msg");
                    if (code != 0){
                        // 获取数据失败
                        String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                        MyToast.show(context,msg);
                    } else {
                        JSONObject data = jsonObject.getJSONObject("data");
                        processAndSetPost(data);
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


        // 2. 获取评论部分
        int page_num = 1;
        int page_size = 10;
        targetUrl = String.format("" +
                        "http://121.43.110.176:8000/api/comment?" +
                        "page_num=%d&page_size=%d&post_id=%d",
                page_num, page_size, Integer.valueOf(post_id));

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
                        commentList = new ArrayList<>(); // 初始化帖子数据列表

                        processComments(data,commentList);
                        commentAdapter = new CommentAdapter(commentList, new CommentAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(Comment comment) {
                                // 评论的点击事件，同样是唤起评论键盘那些
                            }
                        });

                        PostDetailActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 更新UI组件的代码
                                commentsRecycleView.setAdapter(commentAdapter);
                                commentAdapter.notifyDataSetChanged();
                            }
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

        // 3. 处理按钮逻辑

        // 3.1 点击“编辑”按钮
        postDetailEditPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditPage();
            }
        });


        // 3.2点击“删帖”按钮
        postDetailDeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String targetUrl = String.format("" +
                        "http://121.43.110.176:8000/api/post/delete?user_id=%s&post_id=%s",user_id,post_id);
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                Context context = getApplicationContext();

                builder.addFormDataPart("user_id", user_id);
                builder.addFormDataPart("post_id", post_id);
                RequestBody requestBody = builder.build();

                DataSender.sendDataToServer(requestBody, targetUrl, new DataSender.DataSenderCallback() {
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
                                MyToast.show(context, "删除成功");
                                // 跳转到主页
                                Intent intent = new Intent(PostDetailActivity.this, HomeActivity.class);
                                startActivity(intent);
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

        // 3.3 点击“保存”按钮，提交修改帖子的数据
        postDetailSavePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = postDetailTitleEditView.getText().toString();
                String text = postDetailContentTextView.getText().toString();
                if (mediaUri != null) {
                    // Rich text mode
                    mEditContentType = 1;
                } else {
                    // Plain text mode
                    mEditContentType = 0;
                }

                LocalUserInfo localUserInfo = new LocalUserInfo(mContext);
                String user_id = localUserInfo.getId();

                String targetUrl = String.format( "http://121.43.110.176:8000/api/post/?post_id=%s",post_id);
                System.out.println("请求链接");
                System.out.println(targetUrl);

                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                if(mEditContentType == 1){
                    // 富文本
                    String mediaUriStorage = getImagePathFromUri(mContext, mediaUri);
                    File file = new File(mediaUriStorage);
                    // 添加图片部分
                    String fieldName = "media";  // 字段名
                    String fileName = file.getName();  // 文件名

                    MultipartBody.Part multipartBodyPart = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        try {
                            multipartBodyPart = buildMultipartBodyPart(mContext, mediaUri, fieldName, fileName);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // 将 multipartBodyPart 添加到 MultipartBody.Builder 中
                    if (multipartBodyPart != null) {
                        builder.addPart(multipartBodyPart);
                    }
                }else {
                    System.out.println("修改为纯文本内容");
                }

                // 添加其他字段
                builder.addFormDataPart("user_id", user_id);
                builder.addFormDataPart("title", title);
                builder.addFormDataPart("text", text);
                builder.addFormDataPart("content_type",String.valueOf(mEditContentType));
                RequestBody requestBody = builder.build();

                OkHttpClient client = new OkHttpClient();
                // 创建请求对象
                Request request = new Request.Builder()
                        .url(targetUrl)
                        .header("Content-Type", "multipart/form-data")
                        .put(requestBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        MyToast.show(mContext, "网络请求错误");
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            // 请求成功
                            String responseBody = Objects.requireNonNull(response.body()).string();
                            try {
                                JSONObject jsonObject = new JSONObject(responseBody);
                                int code = jsonObject.getInt("code");
                                String errorMsg = jsonObject.getString("error_msg");
                                if (code != 0){
                                    String msg = "error code:" + code + "\nerror_msg:" + errorMsg;
                                    MyToast.show(mContext,msg);
                                    System.out.println(msg);
                                } else {
                                    MyToast.show(mContext, "修改成功");
                                    setShowPage();
                                }
                            } catch (JSONException e) {
                                MyToast.show(mContext, "JSON错误");
                                e.printStackTrace();
                            }
                        } else {
                            MyToast.show(mContext, "网络请求失败");
                        }
                    }
                });
            }
        });

        // 3.4 点击“取消”按钮
        postDetailCancelPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowPage();
            }
        });


        // 4. 处理评论部分

        // 一般情况下，“发表”按钮不显示，点击编辑框之后才显现
        postDetailCommentEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // 唤起键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    postDetailCommentEditText.requestFocus(); // 聚焦光标
                    postDetailCommentButton.setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }

        });

        // 点击“评论”按钮
        postDetailCommentButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                /* 1. 整理并发送数据 */
                String text = postDetailCommentEditText.getText().toString();
                // 获取位置信息
                Location location = new Location(PostDetailActivity.this);
                location.getCurrentLocation(new Location.LocationCallback() {
                    @Override
                    public void onLocationReceived(double latitude, double longitude, String province, String city, String address) {
                        /* 由于地理位置的获取是异步的，所以发送数据的逻辑写到回调函数里 */

                        String targetUrl = "http://121.43.110.176:8000/api/comment";

                        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                        Context context = getApplicationContext();

                        builder.addFormDataPart("user_id", user_id);
                        builder.addFormDataPart("post_id", post_id);
                        builder.addFormDataPart("text", text);
                        builder.addFormDataPart("content_type",String.valueOf(mPost.getContentType()));
                        builder.addFormDataPart("ip_address",province);
                        RequestBody requestBody = builder.build();

                        DataSender.sendDataToServer(requestBody, targetUrl, new DataSender.DataSenderCallback() {
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
                                        // 发表成功
                                        MyToast.show(context, "评论成功");
                                        PostDetailActivity.this.runOnUiThread(new Runnable() {
                                            @SuppressLint("NotifyDataSetChanged")
                                            @Override
                                            public void run() {
                                                // 更新UI组件
                                                commentAdapter.notifyDataSetChanged();
                                                commentsRecycleView.setAdapter(commentAdapter);
                                            }
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

                    }

                    @Override
                    public void onLocationFailed(String errorMsg) {
                        System.out.println("Failed to get location: " + errorMsg);
                    }
                });

                //----------------------------------------------

                /* 2.处理UI */

                // 不显示“评论”按钮
                postDetailCommentButton.setVisibility(View.GONE);
                // 收起键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(postDetailCommentEditText.getWindowToken(), 0);
                // 重置评论框
                postDetailCommentEditText.setText("");

            }
        });


        // 5. 其余部分

        // 缩略图点击效果
        postDetailShowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



    }


    public static void processComments(JSONObject data, ArrayList<Comment> commentList) {
        try {
            JSONArray jsonArray = data.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                // 获取条目的各个字段值
                String id = item.getString("id");
                String content_type = item.getString("content_type");
                String user_id = item.getString("user_id");
                String username = item.getString("username");
                String user_picture = item.getString("user_picture");
                String text = item.getString("text");
                String media_url = item.getString("media_url");
                String date = item.getString("date");
                String ip_address = item.getString("ip_address");
                Comment comment = new Comment(id, user_id, username, user_picture, text, content_type, media_url,ip_address, date);
                commentList.add(comment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void processAndSetPost(JSONObject data){
        try {

            String id = data.getString("id");
            String uid = data.getString("user_id");
            String username = data.getString("username");
            String user_picture = data.getString("user_picture");
            String title = data.getString("title");
            String text = data.getString("text");
            String content_type = data.getString("content_type");
            String media_url = data.getString("media_url");
            String date = data.getString("date");
            double location_x = data.getDouble("location_x");
            double location_y = data.getDouble("location_y");
            String ip_address = data.getString("ip_address");

            mPost = new Post(id, uid, username, user_picture, title, text, Integer.parseInt(content_type), media_url,date,location_x,location_y,ip_address);
            // 1. 展示文本信息
            PostDetailActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 更新UI组件的代码
                    postDetailUsernameTextView.setText(username);
                    postDetailIPTextView.setText(ip_address);
                    postDetailTitleTextView.setText(title);
                    postDetailContentTextView.setText(text);
                }
            });

            // 2.获取图片信息


            // 2.1 获取头像图片
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 获取布局中ImageView的宽度和高度
                    int targetWidth = postDetailUserImageView.getWidth();
                    int targetHeight = postDetailUserImageView.getHeight();
                    RequestOptions requestOptions = new RequestOptions()
                            .centerCrop() // 根据需要进行裁剪或缩放
                            .override(targetWidth, targetHeight); // 设置图片大小为ImageView的大小

                    Glide.with(mContext)
                            .load(mPost.getImageUrl())
                            .apply(requestOptions)
                            .into(postDetailUserImageView);
                }
            });


            // 2.2 获取媒体数据
            if (content_type.equals("1")){
                // 文件后缀名

                if (isImageFile(media_url)) {
                    // 文件是图片
                    System.out.println("媒体资源为图片，url为：");
                    System.out.println(media_url);
                    // 在主线程上加载图片缩略图并显示到ImageView
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RequestOptions requestOptions = new RequestOptions()
                                    .centerCrop() // 根据需要进行裁剪或缩放
                                    .override(200, 200); // 设置缩略图的大小，这里是200x200像素

                            Glide.with(mContext)
                                    .load(mPost.getMediaUrl())
                                    .apply(requestOptions)
                                    .into(postDetailShowImageView);
                        }
                    });


                } else if (isVideoFile(media_url)) {
                    // 文件是视频
                    System.out.println("媒体资源为视频，url为：");
                    System.out.println(media_url);

                    // 在主线程上加载视频缩略图并显示到ImageView
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RequestOptions requestOptions = new RequestOptions()
                                    .frame(1000000) // 设置为一个足够大的帧时间，以获取视频的缩略图
                                    .centerCrop() // 根据需要进行裁剪或缩放
                                    .override(200, 200) // 设置缩略图的大小，这里是200x200像素
                                    .diskCacheStrategy(DiskCacheStrategy.DATA); // 仅使用数据缓存，不使用磁盘缓存完整视频数据

                            Glide.with(mContext)
                                    .asBitmap() // 显示为Bitmap对象
                                    .load(mPost.getMediaUrl())
                                    .apply(requestOptions)
                                    .into(postDetailShowImageView);
                        }
                    });

                } else {
                    // 文件类型未知
                    MyToast.show(getApplicationContext(),"文件类型未知");
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 设置界面为浏览帖子详情页的UI
    private void setShowPage(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                postDetailEditPostButton.setVisibility(View.VISIBLE);
                postDetailDeletePostButton.setVisibility(View.VISIBLE);
                postDetailSavePostButton.setVisibility(View.GONE);
                postDetailCancelPostButton.setVisibility(View.GONE);

                postDetailTitleTextView.setVisibility(View.VISIBLE);
                postDetailTitleEditView.setVisibility(View.GONE);
                postDetailTitleEditView.setText("");

                postDetailContentTextView.setVisibility(View.VISIBLE);
                postDetailContentEditView.setVisibility(View.GONE);
                postDetailContentEditView.setText("");

                postDetailShowImageView.setVisibility(View.VISIBLE);
                // postDetailEditImageView.setVisibility(View.GONE);
                postDetailEditImageFrameLayout.setVisibility(View.GONE);
            }
        });

    }

    // 设置界面为编辑帖子的UI
    private void setEditPage(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                postDetailEditPostButton.setVisibility(View.GONE);
                postDetailDeletePostButton.setVisibility(View.GONE);
                postDetailSavePostButton.setVisibility(View.VISIBLE);
                postDetailCancelPostButton.setVisibility(View.VISIBLE);

                postDetailTitleTextView.setVisibility(View.GONE);
                postDetailTitleEditView.setVisibility(View.VISIBLE);
                postDetailTitleEditView.setText(mPost.getTitle());

                postDetailContentTextView.setVisibility(View.GONE);
                postDetailContentEditView.setVisibility(View.VISIBLE);
                postDetailContentEditView.setText(mPost.getText());

                postDetailShowImageView.setVisibility(View.GONE);
                // postDetailEditImageView.setVisibility(View.VISIBLE);
                postDetailEditImageFrameLayout.setVisibility(View.VISIBLE);
            }
        });

    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/* video/*");
        galleryLauncher.launch(intent);
    }

    private boolean isVideoFile(Uri uri) {
        String mimeType = this.getContentResolver().getType(uri);
        return mimeType != null && mimeType.startsWith("video/");
    }

    public boolean isImageFile(String filePath) {
        String extension = getFileExtension(filePath);
        return extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg") ||
                extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif");
    }

    public boolean isVideoFile(String filePath) {
        String extension = getFileExtension(filePath);
        return extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("avi") ||
                extension.equalsIgnoreCase("mov") || extension.equalsIgnoreCase("wmv");
    }


    public String getFileExtension(String filePath) {
        String extension = "";
        int dotIndex = filePath.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < filePath.length() - 1) {
            extension = filePath.substring(dotIndex + 1).toLowerCase();
        }
        return extension;
    }

    private Bitmap getVideoThumbnail(Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, videoUri);
            Bitmap thumbnail = retriever.getFrameAtTime();
            if (thumbnail != null) {
                // 缩放和裁剪缩略图以适应 ImageView 的大小
                int targetWidth = postDetailEditImageView.getWidth();
                int targetHeight = postDetailEditImageView.getHeight();
                return scaleAndCropBitmap(thumbnail, targetWidth, targetHeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException | IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private Bitmap getImageThumbnail(Uri imageUri) {
        try {
            Bitmap thumbnail = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            int targetWidth = postDetailEditImageView.getWidth();
            int targetHeight = postDetailEditImageView.getHeight();
            return scaleAndCropBitmap(thumbnail, targetWidth, targetHeight);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 用于缩略图的缩放，以适应显示的大小
    private Bitmap scaleAndCropBitmap(Bitmap originalBitmap, int targetWidth, int targetHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float scaleX = (float) targetWidth / width;
        float scaleY = (float) targetHeight / height;
        float scale = Math.max(scaleX, scaleY);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        Bitmap scaledBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true);

        int startX = (scaledBitmap.getWidth() - targetWidth) / 2;
        int startY = (scaledBitmap.getHeight() - targetHeight) / 2;
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, startX, startY, targetWidth, targetHeight);

        originalBitmap.recycle();
        scaledBitmap.recycle();

        return croppedBitmap;
    }

    private String getImagePathFromUri(Context context, Uri uri) {
        String imagePath = null;
        if (uri != null) {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    if (columnIndex != -1) {
                        imagePath = cursor.getString(columnIndex);
                    }
                }
                cursor.close();
            }
        }
        return imagePath;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private RequestBody getFileRequestBody(Context context, Uri uri) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        String mimeType = contentResolver.getType(uri);
        InputStream inputStream = contentResolver.openInputStream(uri);

        byte[] fileBytes = readBytes(inputStream);

        return RequestBody.create(MediaType.get(mimeType), fileBytes);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, bytesRead);
        }
        return byteBuffer.toByteArray();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private MultipartBody.Part buildMultipartBodyPart(Context context, Uri uri, String fieldName, String fileName) throws IOException {
        RequestBody fileBody = getFileRequestBody(context, uri);
        return MultipartBody.Part.createFormData(fieldName, fileName, fileBody);
    }
}
