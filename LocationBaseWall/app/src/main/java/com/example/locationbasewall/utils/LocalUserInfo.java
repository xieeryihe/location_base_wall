package com.example.locationbasewall.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class LocalUserInfo {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public LocalUserInfo(Context context) {
        sharedPreferences = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUserInfo(String username, String id, String email, String phonenum, String pictureUrl) {
        editor.putBoolean("isLogin",true);

        editor.putString("username", username);
        editor.putString("id", id);
        editor.putString("KEY_EMAIL", email);
        editor.putString("phonenum", phonenum);
        editor.putString("pictureUrl", pictureUrl);
        editor.apply();
    }

    public String getUsername() {
        return sharedPreferences.getString("username", "");
    }

    public String getId() {
        return sharedPreferences.getString("id", "");
    }

    public String getEmail() {
        return sharedPreferences.getString("email", "");
    }

    public String getPhonenum() {
        return sharedPreferences.getString("phonenum", "");
    }

    public String getPicture() {
        return sharedPreferences.getString("pictureUrl", "");
    }

    public void clearUserInfo() {
        editor.clear();
        editor.apply();
    }

    public void showUserInfo() {
        String username = getUsername();
        String id = getId();
        String email = getEmail();
        String phonenum = getPhonenum();
        String picture = getPicture();

        System.out.println("Username: " + username);
        System.out.println("id: " + id);
        System.out.println("Email: " + email);
        System.out.println("Phone Number: " + phonenum);
        System.out.println("Picture: " + picture);
    }
}
