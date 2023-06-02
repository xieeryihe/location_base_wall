package com.example.locationbasewall.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.locationbasewall.R;
import com.example.locationbasewall.home.HomeActivity;
import com.example.locationbasewall.utils.DataSender;
import com.example.locationbasewall.utils.LocalUserInfo;
import com.example.locationbasewall.utils.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;

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

            String targetUrl = "http://121.43.110.176:8000/api/user/login";
            RequestBody requestBody = new FormBody.Builder()
                    .add("userName",username)
                    .add("password",password)
                    .build();
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
                            MyToast.show(LoginActivity.this,msg);

                        } else {
                            // 登录成功
                            MyToast.show(LoginActivity.this, "登录成功");
                            // 提取data中的字段
                            String uid = data.getString("user_id");
                            String email = data.getString("email");
                            String phonenum = data.getString("phonenum");
                            String username = data.getString("username");
                            String pictureUrl = data.getString("picture");  // 注意，这个是图片网络路径

                            // 登录成功才要保存用户信息
                            LocalUserInfo localUserInfo = new LocalUserInfo(getApplicationContext());
                            localUserInfo.saveUserInfo(username,uid,email,phonenum,pictureUrl);

                            localUserInfo.showUserInfo();

//                            finish(); // 销毁当前活动（登录活动）

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

            // TODO 登录逻辑修好之后这块要删除
//            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//            startActivity(intent);
        });

        // 没有账号？跳转到注册页
        noAccountTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });



    }
}