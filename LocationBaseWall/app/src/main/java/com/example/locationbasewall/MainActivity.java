package com.example.locationbasewall;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocationClient;
import com.example.locationbasewall.home.HomeActivity;
import com.example.locationbasewall.login.LoginActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("-----position","check permission");
        System.out.println("--------------------...------------------");
        AMapLocationClient.updatePrivacyAgree(getApplicationContext(),true);
        AMapLocationClient.updatePrivacyShow(getApplicationContext(),true,true);

        LinearLayout mainLayout = findViewById(R.id.layout_main);
        mainLayout.setOnClickListener(v -> {
            // 检查登录状态
            if (isLoggedIn()) {
                // 如果已登录，则直接进入应用界面
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            } else {
                // 如果未登录，跳转到登录页面
                finish(); // 销毁当前活动（欢迎页）
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean isLoggedIn() {
        // 从SharedPreferences中读取登录状态
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLogin", false);
    }

}