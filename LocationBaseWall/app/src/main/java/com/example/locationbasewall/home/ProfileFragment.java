package com.example.locationbasewall.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.fragment.app.Fragment;

import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.PostAdapter;
import com.example.locationbasewall.login.LoginActivity;
import com.example.locationbasewall.utils.DataGetter;
import com.example.locationbasewall.utils.LocalUserInfo;
import com.example.locationbasewall.utils.MyToast;
import com.example.locationbasewall.utils.Post;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileFragment extends Fragment {
    private ImageView profilePorTraitImageView;
    private TextView profileIdTextView;
    private TextView textViewUsername;
    private TextView textViewEmail;
    private TextView textViewPhone;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;
    private Button buttonEdit;

    private Button profileLogoutButton;

    private boolean isEditMode = false;

    private ActivityResultLauncher<String> imagePickerLauncher;

    private static final String MIME_TYPE_IMAGE = "image/*";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profilePorTraitImageView = view.findViewById(R.id.profilePortraitImageView);
        profileIdTextView = view.findViewById(R.id.profileIdTextView);

        textViewUsername = view.findViewById(R.id.textViewUsername);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewPhone = view.findViewById(R.id.textViewPhone);
        editTextName = view.findViewById(R.id.editTextUsername);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        buttonEdit = view.findViewById(R.id.buttonEditProfile);
        profileLogoutButton = view.findViewById(R.id.profileLogoutButton);

        LocalUserInfo localUserInfo = new LocalUserInfo(requireContext());

        profileIdTextView.setText("ID:" + localUserInfo.getId());
        textViewUsername.setText("用户名:" + localUserInfo.getUsername());
        textViewEmail.setText("邮箱：" + localUserInfo.getEmail());
        textViewPhone.setText("手机号:" + localUserInfo.getPhonenum());


        String user_picture = localUserInfo.getPicture();
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
                            profilePorTraitImageView.setImageBitmap(bitmap);
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

        buttonEdit.setOnClickListener(v -> {
            if (isEditMode) {
                // 编辑模式时按钮文字为“提交”，点击按钮，回到浏览模式

                // 1. 发送数据
                String username = editTextName.getText().toString();
                String email = editTextEmail.getText().toString();
                String phonenum = editTextPhone.getText().toString();



                // 2. 修改UI
                textViewUsername.setVisibility(View.VISIBLE);
                textViewEmail.setVisibility(View.VISIBLE);
                textViewPhone.setVisibility(View.VISIBLE);

                editTextName.setVisibility(View.GONE);
                editTextEmail.setVisibility(View.GONE);
                editTextPhone.setVisibility(View.GONE);

                buttonEdit.setText("编辑");

                isEditMode = false;

            } else {
                // 当处于非编辑模式时点击编辑按钮
                textViewUsername.setVisibility(View.GONE);
                textViewEmail.setVisibility(View.GONE);
                textViewPhone.setVisibility(View.GONE);

                editTextName.setVisibility(View.VISIBLE);
                editTextEmail.setVisibility(View.VISIBLE);
                editTextPhone.setVisibility(View.VISIBLE);

                buttonEdit.setText("保存");

                isEditMode = true;
            }
        });

        // 初始化 ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    // 处理选择的图片
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), result);
                        // 将选择的图片设置给 ImageView
                        profilePorTraitImageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        profilePorTraitImageView.setOnClickListener(v -> {
            // 打开相册选择图像
            if (isEditMode){
                imagePickerLauncher.launch(MIME_TYPE_IMAGE);
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

    //---------------------



}