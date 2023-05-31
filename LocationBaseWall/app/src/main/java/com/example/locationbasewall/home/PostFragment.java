package com.example.locationbasewall.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.locationbasewall.R;
import com.example.locationbasewall.data.DataSender;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class PostFragment extends Fragment {
    private ImageView postMediaImageView;
    private EditText postTitleEditText;
    private EditText postContentEditText;
    private Button postButton;

    private Uri mediaUri;
    private int content_type = 0;

    private ActivityResultLauncher<Intent> galleryLauncher;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

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
            String data;

            // TODO 还没写获取地理位置信息的内容

            // Check post mode based on media selection
            if (mediaUri != null) {
                // Rich text mode
                content_type = 1;
                // 获取媒体数据
                byte[] mediaData = isVideoFile(mediaUri) ? getVideoData(mediaUri) : getImageData(mediaUri);
                // 整理成需要的数据格式
                data = formatPostData(content_type, title, text, 1.2, 3.4, mediaData);

            } else {
                // Plain text mode
                content_type = 0;
                // 整理成需要的数据格式
                data = formatPostData(content_type, title, text, 1.2, 3.4, null);

            }
            DataSender.sendDataToServer(data);
        });


        return view;
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

}