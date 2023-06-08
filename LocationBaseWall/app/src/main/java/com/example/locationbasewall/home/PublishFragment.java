package com.example.locationbasewall.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.locationbasewall.R;
import com.example.locationbasewall.utils.DataSender;
import com.example.locationbasewall.utils.LocalUserInfo;
import com.example.locationbasewall.utils.Location;
import com.example.locationbasewall.utils.Media;
import com.example.locationbasewall.utils.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PublishFragment extends Fragment {
    private ImageView postMediaImageView;
    private EditText postTitleEditText;
    private EditText postContentEditText;
    private Button postButton;

    private Uri mUploadMediaUri;
    private int mContentType = 0;
    private Context mContext;

    private ActivityResultLauncher<Intent> galleryLauncher;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publish, container, false);
        mContext = getContext();

        postMediaImageView = view.findViewById(R.id.postMediaImageView);
        postTitleEditText = view.findViewById(R.id.postTitleEditText);
        postContentEditText = view.findViewById(R.id.postContentEditText);
        postButton = view.findViewById(R.id.postButton);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            mUploadMediaUri = data.getData();
                            if (mUploadMediaUri != null) {
                                if (Media.isVideoFile(mContext, mUploadMediaUri)) {
                                    RequestOptions requestOptions = new RequestOptions()
                                            .frame(1000000) // 设置为一个足够大的帧时间，以获取视频的缩略图
                                            .centerCrop() // 根据需要进行裁剪或缩放
                                            .override(postMediaImageView.getWidth(), postMediaImageView.getHeight());

                                    Glide.with(mContext)
                                            .load(mUploadMediaUri)
                                            .apply(requestOptions)
                                            .into(postMediaImageView);
                                } else {
                                    RequestOptions requestOptions = new RequestOptions()
                                            .centerCrop()
                                            .override(postMediaImageView.getWidth(), postMediaImageView.getHeight());

                                    Glide.with(mContext)
                                            .load(mUploadMediaUri)
                                            .apply(requestOptions)
                                            .into(postMediaImageView);
                                }
                            }
                        }
                    }
                });

        // Initialize the ActivityResultLauncher
        postMediaImageView.setOnClickListener(v -> Media.openGallery(galleryLauncher));

        postButton.setOnClickListener(v -> {
            String title = postTitleEditText.getText().toString();
            String text = postContentEditText.getText().toString();

            // TODO mContentType 可以用来区分是否要删除原来的图片
            if (mUploadMediaUri != null) {
                mContentType = 1;

            } else {
                mContentType = 0;
            }

            Location location = new Location(mContext);
            location.getCurrentLocation(new Location.LocationCallback() {
                @Override
                public void onLocationReceived(double latitude, double longitude, String province, String city, String address) {
                    System.out.println(city);
                    System.out.println(address);

                    // 获取地理位置之后才能发送完整数据


                    LocalUserInfo localUserInfo = new LocalUserInfo(requireContext());
                    String user_id = localUserInfo.getId();

                    String targetUrl = "http://121.43.110.176:8000/api/post";

                    MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                    if(mContentType == 1){
                        // 富文本
                        String mediaUriStorage = Media.getImagePathFromUri(mContext, mUploadMediaUri);
                        File file = new File(mediaUriStorage);
                        // 添加图片部分
                        String fieldName = "media";  // 字段名
                        String fileName = file.getName();  // 文件名

                        MultipartBody.Part multipartBodyPart = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            try {
                                multipartBodyPart = Media.buildMultipartBodyPart(mContext, mUploadMediaUri, fieldName, fileName);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        // 将 multipartBodyPart 添加到 MultipartBody.Builder 中
                        if (multipartBodyPart != null) {
                            builder.addPart(multipartBodyPart);
                        }
                    }else {
                        System.out.println("纯文本------------");
                    }

                    // 添加其他字段
                    builder.addFormDataPart("user_id", user_id);
                    builder.addFormDataPart("title", title);
                    builder.addFormDataPart("text", text);
                    builder.addFormDataPart("content_type",String.valueOf(mContentType));
                    builder.addFormDataPart("location_x", String.valueOf(longitude));
                    builder.addFormDataPart("location_y", String.valueOf(latitude));
                    builder.addFormDataPart("ip_address",province);  // 这里给省就行了
                    RequestBody requestBody = builder.build();

                    DataSender.sendDataToServer(requestBody, targetUrl, new DataSender.DataSenderCallback() {
                        @Override
                        public void onSuccess(JSONObject jsonObject) {
                            // 网络请求成功
                            try {
                                int code = jsonObject.getInt("code");
                                String errorMsg = jsonObject.getString("error_msg");
                                System.out.println("our code :" + code);
                                if (code != 0){
                                    // 发表失败
                                    String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                                    MyToast.show(mContext,msg);

                                } else {
                                    // 发表成功
                                    MyToast.show(mContext, "发表成功");

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
        });

        return view;
    }
}