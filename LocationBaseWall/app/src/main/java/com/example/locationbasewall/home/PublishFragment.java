package com.example.locationbasewall.home;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.locationbasewall.R;
import com.example.locationbasewall.login.LoginActivity;
import com.example.locationbasewall.utils.DataSender;
import com.example.locationbasewall.utils.LocalUserInfo;
import com.example.locationbasewall.utils.Location;
import com.example.locationbasewall.utils.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PublishFragment extends Fragment {
    private ImageView postMediaImageView;
    private EditText postTitleEditText;
    private EditText postContentEditText;
    private Button postButton;

    private Uri mediaUri;
    private int mContentType = 0;

    private ActivityResultLauncher<Intent> galleryLauncher;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publish, container, false);

        postMediaImageView = view.findViewById(R.id.postMediaImageView);
        postTitleEditText = view.findViewById(R.id.postTitleEditText);
        postContentEditText = view.findViewById(R.id.postContentEditText);
        postButton = view.findViewById(R.id.postButton);


        // Initialize the ActivityResultLauncher
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
                                        postMediaImageView.setImageBitmap(thumbnail);
                                    }
                                } else {
                                    // 是图像文件，直接显示图像
                                    // postMediaImageView.setImageURI(mediaUri);
                                    Bitmap thumbnail = getImageThumbnail(mediaUri);
                                    if (thumbnail != null) {
                                        postMediaImageView.setImageBitmap(thumbnail);
                                    }
                                }
                            }
                        }
                    }
                });

        postMediaImageView.setOnClickListener(v -> openGallery());


        postButton.setOnClickListener(v -> {
            String title = postTitleEditText.getText().toString();
            String text = postContentEditText.getText().toString();



            byte[] mediaData;
            // Check post mode based on media selection
            if (mediaUri != null) {
                // Rich text mode
                mContentType = 1;
                // 获取媒体数据
                mediaData = isVideoFile(mediaUri) ? getVideoData(mediaUri) : getImageData(mediaUri);
                // 整理成需要的数据格式

            } else {
                // Plain text mode
                mContentType = 0;
                mediaData = null;
            }

            Location location = new Location(getContext());
            location.getCurrentLocation(new Location.LocationCallback() {
                @Override
                public void onLocationReceived(double latitude, double longitude, String province, String city, String address) {
                    System.out.println("Latitude: " + latitude + ", Longitude: " + longitude);
                    System.out.println(address);

                    LocalUserInfo localUserInfo = new LocalUserInfo(requireContext());
                    String uid = localUserInfo.getId();


                    // 获取地理位置之后才能发送完整数据
                    String targetUrl = "http://121.43.110.176:8000/api/post";

                    MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                    String mediaUriStorage = getImagePathFromUri(getContext(), mediaUri);

                    System.out.println("------------------media");
                    System.out.println(mediaUri);
                    System.out.println(mediaUriStorage);
                    File file = new File(mediaUriStorage);
                    // 添加图片部分
                    String fieldName = "image";  // 字段名
                    String fileName = file.getName();  // 文件名

                    MultipartBody.Part multipartBodyPart = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        try {
                            multipartBodyPart = buildMultipartBodyPart(getContext(), mediaUri, fieldName, fileName);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // 将 multipartBodyPart 添加到 MultipartBody.Builder 中
                    if (multipartBodyPart != null) {
                        builder.addPart(multipartBodyPart);
                    }

                    // 添加其他字段
                    builder.addFormDataPart("uid", uid);

                    RequestBody requestBody = builder.build();

                    DataSender.sendDataToServer(requestBody, targetUrl, new DataSender.DataSenderCallback() {
                        @Override
                        public void onSuccess(JSONObject jsonObject) {
                            // 网络请求成功
                            try {
                                int code = jsonObject.getInt("code");
                                String errorMsg = jsonObject.getString("error_msg");
                                JSONObject data = jsonObject.getJSONObject("data");

                                if (code != 0){
                                    // 登录失败
                                    String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                                    MyToast.show(getContext(),msg);

                                } else {
                                    // 登录成功
                                    MyToast.show(getContext(), "登录成功");
                                    // 提取data中的字段
                                    String uid = data.getString("uid");
                                    String email = data.getString("email");
                                    String phonenum = data.getString("phonenum");
                                    String username = data.getString("username");
                                    String pictureUrl = data.getString("picture");  // 注意，这个是图片网络路径

                                    // 登录成功才要保存用户信息
                                    LocalUserInfo localUserInfo = new LocalUserInfo(requireContext());
                                    localUserInfo.saveUserInfo(username,uid,email,phonenum,pictureUrl);

                                    localUserInfo.showUserInfo();

//                            finish(); // 销毁当前活动（登录活动）

                                    // 启动新活动
                                    Intent intent = new Intent(getContext(), HomeActivity.class);
                                    startActivity(intent);
                                }
                            } catch (JSONException e) {
                                MyToast.show(getContext(), "JSON错误");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            System.out.println(errorMessage);
                            MyToast.show(getContext(), "网络请求错误");
                        }
                    });
                }

                @Override
                public void onLocationFailed(String errorMsg) {
                    System.out.println("Failed to get location: " + errorMsg);

                }
            });

            // 发布帖子的数据
//            String targetUrl = "http://121.43.110.176:8000/api/user/login";
//            DataSender.sendDataToServer(data, targetUrl);
        });

        return view;
    }

    // 获取图像数据
    private byte[] getImageData(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 获取视频数据
    private byte[] getVideoData(Uri videoUri) {
        try {
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(videoUri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



    // 生成所需的目标数据
    // TODO 具体需要的数据有哪些还没确定
    public static String formatPostData(int contentType, String title, String text, double locationX, double locationY, byte[] mediaData) {
        try {
            // Create the JSON object for the request body
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("content_type", contentType);
            bodyJson.put("title", title);
            bodyJson.put("text", text);
            bodyJson.put("location_x", locationX);
            bodyJson.put("location_y", locationY);
            bodyJson.put("mediaData", mediaData);

            JSONObject postJson = new JSONObject();
            postJson.put("post", bodyJson);

            return postJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/* video/*");
        galleryLauncher.launch(intent);
    }

    private boolean isVideoFile(Uri uri) {
        String mimeType = requireActivity().getContentResolver().getType(uri);
        return mimeType != null && mimeType.startsWith("video/");
    }


    private Bitmap getVideoThumbnail(Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(getActivity(), videoUri);
            Bitmap thumbnail = retriever.getFrameAtTime();
            if (thumbnail != null) {
                // 缩放和裁剪缩略图以适应 ImageView 的大小
                int targetWidth = postMediaImageView.getWidth();
                int targetHeight = postMediaImageView.getHeight();
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
            Bitmap thumbnail = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            int targetWidth = postMediaImageView.getWidth();
            int targetHeight = postMediaImageView.getHeight();
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