package com.example.locationbasewall.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.locationbasewall.R;
import com.example.locationbasewall.home.HomeActivity;
import com.example.locationbasewall.utils.DataSender;
import com.example.locationbasewall.utils.LocalUserInfo;
import com.example.locationbasewall.utils.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class RegisterActivity extends AppCompatActivity {
    private EditText registerUsernameEditText;
    private EditText registerPasswordEditText;
    private EditText registerEmailEditText;
    private EditText registerPhonenumEditView;
    private Button registerButton;

    private SharedPreferences sharedPreferences; // 暂存用户的数据
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerUsernameEditText = findViewById(R.id.registerUsernameEditText);
        registerPasswordEditText = findViewById(R.id.registerPasswordEditView);
        registerEmailEditText = findViewById(R.id.registerEmailEditText);
        registerPhonenumEditView = findViewById(R.id.registerPhonenumEditView);
        registerButton = findViewById(R.id.registerButton);

        // 在这里可以使用获取到的元素进行进一步的操作
        // 例如，添加点击事件监听器等等

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = registerUsernameEditText.getText().toString();
                String password = registerPasswordEditText.getText().toString();
                String email = registerEmailEditText.getText().toString();
                String phonenum = registerPhonenumEditView.getText().toString();

                // 检查用户名是否符合要求
                if (!isValidUsername(username)) {
                    MyToast.show(getApplicationContext(),"用户名不合法，只能有字母、数字、下划线、汉字");
                    return;
                }
                // 检查密码是否符合要求
                if (!isValidPassword(password)){
                    MyToast.show(getApplicationContext(),"密码不合法，只能有字母和数字");
                    return;
                }
                // 检查邮箱是否符合要求
                if (!isValidEmail(email)) {
                    MyToast.show(getApplicationContext(),"邮箱不合法");
                    return;
                }

                // 检查手机号是否符合要求
                if (!isValidPhonenum(phonenum)) {
                    MyToast.show(getApplicationContext(),"手机号不合法");
                    return;
                }

                // 发送数据到服务器
                String targetUrl = "http://121.43.110.176:8000/api/user/register";

                RequestBody requestBody = new FormBody.Builder()
                        .add("username",username)
                        .add("password",password)
                        .add("email",email)
                        .add("phonenum",phonenum)
                        .build();
                DataSender.sendDataToServer(requestBody, targetUrl, new DataSender.DataSenderCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        // 网络请求成功
                        try {
                            int code = jsonObject.getInt("code");
                            String errorMsg = jsonObject.getString("error_msg");
                            JSONObject data = jsonObject.getJSONObject("data");

                            // 提取data中的字段，只需要提取uid即可
                            String uid = data.getString("id");
                            String username = data.getString("username");

                            if (code != 0){
                                // 登录失败
                                String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                                MyToast.show(RegisterActivity.this,msg);

                            } else {
                                MyToast.show(RegisterActivity.this, "注册成功");
                                // 注册成功后自动登录
                                // 登录成功才要保存用户信息
                                LocalUserInfo localUserInfo = new LocalUserInfo(getApplicationContext());
                                localUserInfo.saveUserInfo(username,uid,email,phonenum,"");

                                localUserInfo.showUserInfo();

                                // 启动新活动
                                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            MyToast.show(RegisterActivity.this, "JSON错误");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        System.out.println(errorMessage);
                        MyToast.show(RegisterActivity.this, "网络请求错误");
                    }
                });

            }
        });


    }
    // 检查用户名格式是否正确
    // 数字，字母，下划线，汉字
    private boolean isValidUsername(String username) {
        String pattern = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$";
        return Pattern.matches(pattern, username);
    }

    // 检查密码是否符合要求
    // 只包含字母和数字的正则表达式
    private boolean isValidPassword(String password) {
        String pattern = "^[a-zA-Z0-9]+$";
        return Pattern.matches(pattern, password);
    }

    // 检查邮箱格式是否正确
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // 检查手机号格式是否正确
    private boolean isValidPhonenum(String phonenum) {
        return android.util.Patterns.PHONE.matcher(phonenum).matches();
    }


}