package com.example.locationbasewall.home;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.example.locationbasewall.utils.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileFragment extends Fragment {
    private ImageView profileShowImageView;
    private ImageView profileEditImageView;
    private TextView profileIdTextView;
    private TextView textViewUsername;
    private TextView textViewEmail;
    private TextView textViewPhone;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;
    private Button profileEditButton;
    private Button profileSaveButton;
    private Button profileCancelButton;

    private Button profileLogoutButton;

    private Uri mediaUri;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private static final String MIME_TYPE_IMAGE = "image/*";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileShowImageView = view.findViewById(R.id.profileShowImageView);
        profileEditImageView = view.findViewById(R.id.profileEditImageView);
        profileIdTextView = view.findViewById(R.id.profileIdTextView);

        textViewUsername = view.findViewById(R.id.textViewUsername);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewPhone = view.findViewById(R.id.textViewPhone);
        editTextName = view.findViewById(R.id.editTextUsername);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        profileEditButton = view.findViewById(R.id.profileEditButton);
        profileSaveButton = view.findViewById(R.id.profileSaveButton);
        profileCancelButton = view.findViewById(R.id.profileCancelButton);
        profileLogoutButton = view.findViewById(R.id.profileLogoutButton);

        profileShowImageView.setClickable(false);

        LocalUserInfo localUserInfo = new LocalUserInfo(requireContext());

        profileIdTextView.setText(String.format("ID: %s", localUserInfo.getId()));
        textViewUsername.setText(String.format("用户名: %s", localUserInfo.getUsername()));
        textViewEmail.setText(String.format("邮箱：%s", localUserInfo.getEmail()));
        textViewPhone.setText(String.format("手机号: %s", localUserInfo.getPhonenum()));


        String user_picture = localUserInfo.getPicture();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(user_picture) // 替换为您的图片链接
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
                            profileShowImageView.setImageBitmap(bitmap);
                            profileEditImageView.setImageBitmap(bitmap);
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

        // 图库选择图片
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result != null) {  // 处理选择的图片
                        Intent data = result.getData();
                        if (data != null) {
                            mediaUri = data.getData();
                            if (mediaUri != null) {
                                Bitmap thumbnail = getImageThumbnail(mediaUri);
                                if (thumbnail != null) {
                                    profileEditImageView.setImageBitmap(thumbnail);
                                    profileShowImageView.setImageBitmap(thumbnail);
                                }
                            }
                        }
                    }
                });

        profileEditImageView.setOnClickListener(v -> openGallery());

        /*下面是按钮点击事件的处理逻辑*/
        // “编辑”按钮
        profileEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击编辑按钮后
                textViewUsername.setVisibility(View.GONE);
                textViewEmail.setVisibility(View.GONE);
                textViewPhone.setVisibility(View.GONE);

                editTextName.setVisibility(View.VISIBLE);
                editTextEmail.setVisibility(View.VISIBLE);
                editTextPhone.setVisibility(View.VISIBLE);

                profileShowImageView.setVisibility(View.GONE);
                profileEditImageView.setVisibility(View.VISIBLE);

                profileEditButton.setVisibility(View.GONE);
                profileSaveButton.setVisibility(View.VISIBLE);
                profileCancelButton.setVisibility(View.VISIBLE);
            }
        });

        //“保存”按钮
        profileSaveButton.setOnClickListener(v -> {
            // 点击“保存”按钮后，获取信息，提交
            String usernameEditText = editTextName.getText().toString();
            String emailEditText = editTextEmail.getText().toString();
            String phonenumEditText = editTextPhone.getText().toString();

            String user_id = localUserInfo.getId();
            String username = usernameEditText.isEmpty() ? localUserInfo.getUsername() : usernameEditText;
            String email = emailEditText.isEmpty() ? localUserInfo.getEmail() : emailEditText;
            String phonenum = phonenumEditText.isEmpty() ? localUserInfo.getPhonenum() : phonenumEditText;

            String targetUrl = "http://121.43.110.176:8000/api/user/edit";

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            String mediaUriStorage = getImagePathFromUri(getContext(), mediaUri);
            File file = new File(mediaUriStorage);
            // 添加图片部分
            String fieldName = "picture";  // 字段名
            String fileName = file.getName();  // 文件名

            MultipartBody.Part multipartBodyPart = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                try {
                    multipartBodyPart = buildMultipartBodyPart(getContext(), mediaUri, fieldName, fileName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (multipartBodyPart != null) {
                builder.addPart(multipartBodyPart);
            }

            builder.addFormDataPart("username", username);
            builder.addFormDataPart("user_id", user_id);
            builder.addFormDataPart("phonenum", phonenum);
            builder.addFormDataPart("email",email);


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
                            // 提交失败
                            String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                            MyToast.show(getContext(),msg);


                        } else {
                            // 提交成功
                            JSONObject data = jsonObject.getJSONObject("data");
                            String image_url = data.getString("user_picture");

                            // 刷新当前用户头像信息
                            if (mediaUri != null) {
                                Bitmap thumbnail = getImageThumbnail(mediaUri);
                                if (thumbnail != null) {
                                    profileShowImageView.setImageBitmap(thumbnail);
                                }
                            }
                            MyToast.show(getContext(), "提交成功");
                            localUserInfo.saveUserInfo(username,user_id,email,phonenum,image_url);

                            System.out.println("头像url是");
                            System.out.println(image_url);
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

            textViewUsername.setVisibility(View.VISIBLE);
            textViewEmail.setVisibility(View.VISIBLE);
            textViewPhone.setVisibility(View.VISIBLE);

            editTextName.setVisibility(View.GONE);
            editTextEmail.setVisibility(View.GONE);
            editTextPhone.setVisibility(View.GONE);

            profileShowImageView.setVisibility(View.VISIBLE);
            profileEditImageView.setVisibility(View.GONE);

            profileEditButton.setVisibility(View.VISIBLE);
            profileSaveButton.setVisibility(View.GONE);
            profileCancelButton.setVisibility(View.GONE);

        });
        //“取消”按钮
        profileCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewUsername.setVisibility(View.VISIBLE);
                textViewEmail.setVisibility(View.VISIBLE);
                textViewPhone.setVisibility(View.VISIBLE);

                editTextName.setText("");
                editTextEmail.setText("");
                editTextPhone.setText("");

                editTextName.setVisibility(View.GONE);
                editTextEmail.setVisibility(View.GONE);
                editTextPhone.setVisibility(View.GONE);

                profileShowImageView.setVisibility(View.VISIBLE);
                profileEditImageView.setVisibility(View.GONE);

                profileEditButton.setVisibility(View.VISIBLE);
                profileSaveButton.setVisibility(View.GONE);
                profileCancelButton.setVisibility(View.GONE);
            }
        });

        // 当点击“退出登录”按钮时，清除登录信息，并退回到登录页面。
        profileLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLoginInfo();
                Activity profileActivity = getActivity();
                // 跳转到登录页面
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                if (profileActivity != null) {
                    profileActivity.finish();
                }
            }
        });

        return view;
    }

    private void clearLoginInfo() {
        // 获取SharedPreferences对象
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        // 清除登录信息
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        galleryLauncher.launch(intent);
    }
    private Bitmap getImageThumbnail(Uri imageUri) {
        try {
            Bitmap thumbnail = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            int targetWidth = profileShowImageView.getWidth();
            int targetHeight = profileShowImageView.getHeight();
            return scaleAndCropBitmap(thumbnail, targetWidth, targetHeight);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
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