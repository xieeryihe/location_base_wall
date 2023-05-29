package com.example.locationbasewall.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.locationbasewall.R;

public class ProfileFragment extends Fragment {
    private TextView textViewUsername;
    private TextView textViewEmail;
    private TextView textViewPhone;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhone;
    private Button buttonEdit;

    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

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
        return view;
    }
}