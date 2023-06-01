package com.example.locationbasewall.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.locationbasewall.R;
import com.example.locationbasewall.home.HomeActivity;
import com.example.locationbasewall.utils.DataSender;
import com.example.locationbasewall.utils.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView noAccountTextView;

    private SharedPreferences sharedPreferences; // 暂存用户的数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        noAccountTextView = findViewById(R.id.noAccountTextView);

        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // 创建一个 JSON 对象并添加所需的属性
            JSONObject json = new JSONObject();

            try {
                json.put("username", username);
                json.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String jsonString = json.toString();
//            System.out.println("------------------------------");
//            System.out.println(jsonString);

            String targetUrl = "http://121.43.110.176:8000/api/user/login/";
            DataSender.sendDataToServer(jsonString, targetUrl, new DataSender.DataSenderCallback() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    // 在这里处理成功的响应数据
                    // 您可以使用 jsonObject 进行检查操作
                    try {

                        int code = jsonObject.getInt("code");
                        String errorMsg = jsonObject.getString("error_msg");
                        JSONObject data = jsonObject.getJSONObject("data");

                        // 提取data中的字段
                        String uid = data.getString("uid");
                        String username = data.getString("username");
                        String picture = data.getString("picture");

                        if (code != 0){
                            // 登录失败
                            String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                            MyToast.show(LoginActivity.this,msg);

                        } else {
                            MyToast.show(LoginActivity.this, "登录成功");
                            // 登录成功才要保存用户信息
                            saveUserInfo(username);

                            finish(); // 销毁当前活动（登录活动）

                            // 启动新活动
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        MyToast.show(LoginActivity.this, "JSON错误");

                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(String errorMessage) {
                    System.out.println(errorMessage);
                    MyToast.show(LoginActivity.this, "网络请求错误");

                }

            });

        });

        // 没有账号？跳转到注册页
        noAccountTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }
    private void saveUserInfo(String username) {
        // 使用SharedPreferences.Editor来写入数据
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogin", true);
        editor.putString("username", username);
        editor.apply();
    }
}