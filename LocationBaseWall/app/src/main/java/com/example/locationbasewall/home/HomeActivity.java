package com.example.locationbasewall.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.locationbasewall.R;
import com.example.locationbasewall.utils.LocalUserInfo;
import com.example.locationbasewall.utils.MyToast;
import com.google.android.material.bottomnavigation.BottomNavigationView;



public class HomeActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        LocalUserInfo localUserInfo = new LocalUserInfo(getApplicationContext());
//        localUserInfo.clearUserInfo();

        fragmentManager = getSupportFragmentManager();

        // 设置默认的 HomeFragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.homeContentFragment, new HomeFragment());
        fragmentTransaction.commit();


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // TODO 切换Fragment的时候，原来的数据就被清除了，尤其是PostFragment的信息，不想被清除
            // TODO 当然，PostFragment中可以加一个清除数据的按钮（后面再说）
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedFragment = getSupportFragmentManager().findFragmentByTag("home");
                    if (selectedFragment == null) {
                        selectedFragment = new HomeFragment();
                    }
                    break;
                case R.id.navigation_nearby:
                    selectedFragment = getSupportFragmentManager().findFragmentByTag("nearby");
                    if (selectedFragment == null) {
                        selectedFragment = new NearbyFragment();
                    }
                    break;
                case R.id.navigation_post:
                    selectedFragment = getSupportFragmentManager().findFragmentByTag("publish");
                    if (selectedFragment == null) {
                        selectedFragment = new PublishFragment();
                    }
                    break;
                case R.id.navigation_profile:
                    selectedFragment = getSupportFragmentManager().findFragmentByTag("profile");
                    if (selectedFragment == null) {
                        selectedFragment = new ProfileFragment();
                    }
                    break;
            }

            if (selectedFragment != null) {
                FragmentTransaction fragmentTransaction1 = fragmentManager.beginTransaction();
                fragmentTransaction1.replace(R.id.homeContentFragment, selectedFragment);
                fragmentTransaction1.commit();
            }

            return true;
        });
    }

    private boolean doubleBackToExitPressedOnce = false;

    // 在HomeActivity上，按一次“返回”，会弹一个通知“再按一次返回到桌面”，2s内再按返回，就到桌面
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            // super.onBackPressed();
            moveTaskToBack(true); // 将当前任务移到后台
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "再按一次返回到桌面", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

}