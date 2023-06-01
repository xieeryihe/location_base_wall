package com.example.locationbasewall.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.locationbasewall.R;

public class RegisterActivity extends AppCompatActivity {
    private EditText registerUsernameEditText;
    private EditText registerEmailEditText;
    private EditText registerPhonenumEditView;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerUsernameEditText = findViewById(R.id.registerUsernameEditText);
        registerEmailEditText = findViewById(R.id.registerEmailEditText);
        registerPhonenumEditView = findViewById(R.id.registerPhonenumEditView);
        registerButton = findViewById(R.id.registerButton);

        // 在这里可以使用获取到的元素进行进一步的操作
        // 例如，添加点击事件监听器等等



    }
}