package com.example.locationbasewall.home;

import android.graphics.Bitmap;
import android.os.Bundle;
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

import java.io.IOException;

public class ProfileFragment extends Fragment {
    private ImageView profilePorTraitImageView;
    private TextView textViewUsername;
    private TextView textViewEmail;
    private TextView textViewPhone;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;
    private Button buttonEdit;

    private boolean isEditMode = false;

    private ActivityResultLauncher<String> imagePickerLauncher;

    private static final String MIME_TYPE_IMAGE = "image/*";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profilePorTraitImageView = view.findViewById(R.id.profilePortraitImageView);
        textViewUsername = view.findViewById(R.id.textViewUsername);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewPhone = view.findViewById(R.id.textViewPhone);
        editTextName = view.findViewById(R.id.editTextUsername);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        buttonEdit = view.findViewById(R.id.buttonEditProfile);

        buttonEdit.setOnClickListener(v -> {
            if (isEditMode) {
                // 当处于编辑模式时点击编辑按钮
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
        return view;
    }
}