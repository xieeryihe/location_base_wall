package com.example.locationbasewall.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.locationbasewall.R;
import com.example.locationbasewall.home.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView noAccountTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        noAccountTextView = findViewById(R.id.noAccountTextView);


        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // 在这里执行登录逻辑，可以与服务器进行交互验证用户名和密码
            // Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);

//                if (username.equals("admin") && password.equals("password")) {
//                    // 登录成功，跳转到主页或下一个页面
//                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
//                    // 进行页面跳转等操作
//                } else {
//                    // 登录失败，显示错误消息
//                    Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
//                }
        });

        noAccountTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}