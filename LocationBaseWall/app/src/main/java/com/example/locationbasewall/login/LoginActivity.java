package com.example.locationbasewall.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.locationbasewall.R;
import com.example.locationbasewall.data.DataSender;
import com.example.locationbasewall.home.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

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

            // 创建一个 JSON 对象并添加所需的属性
            JSONObject json = new JSONObject();
            JSONObject schema = new JSONObject();
            JSONObject properties = new JSONObject();

            try {
                properties.put("username", username);
                properties.put("password", password);
                schema.put("type", "object");
                schema.put("properties", properties);
                json.put("schema", schema);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String jsonString = json.toString();

//            String targetUrl = "http://121.43.110.176:8000/api/user/login";
//            DataSender.sendDataToServer(jsonString, targetUrl);


            DataSender.sendTest();

            // 创建并显示 Toast
            Toast.makeText(LoginActivity.this, "发送成功", Toast.LENGTH_SHORT).show();

            // 在点击事件监听器中


            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);

        });

        noAccountTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}