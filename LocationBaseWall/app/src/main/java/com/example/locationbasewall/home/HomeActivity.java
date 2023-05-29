package com.example.locationbasewall.home;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.locationbasewall.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;



public class HomeActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fragmentManager = getSupportFragmentManager();

        // 设置默认的 HomeFragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.home_content, new HomeFragment());
        fragmentTransaction.commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.navigation_nearby:
                    selectedFragment = new NearbyFragment();
                    break;
                case R.id.navigation_post:
                    selectedFragment = new PostFragment();
                    break;
                case R.id.navigation_profile:
                    selectedFragment = new ProfileFragment();
                    break;

                // 添加其他项目的处理

            }

            if (selectedFragment != null) {
                FragmentTransaction fragmentTransaction1 = fragmentManager.beginTransaction();
                fragmentTransaction1.replace(R.id.home_content, selectedFragment);
                fragmentTransaction1.commit();
            }

            return true;
        });
    }
}