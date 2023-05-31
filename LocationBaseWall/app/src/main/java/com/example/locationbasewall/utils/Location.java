package com.example.locationbasewall.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;

public class Location {
    private Context mContext;
    private AMapLocationClient locationClient;

    public double mLatitude;
    public double mLongitude;

    public Location(Context context) {
        this.mContext = context;
    }


    public void getCurrentLocation() {


        if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("AMap","Location permissions not granted");
            return;
        }
        try {
            locationClient = new AMapLocationClient(mContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setOnceLocation(true);
        locationClient.setLocationOption(locationOption);
        locationClient.startLocation();
        locationClient.setLocationListener(aMapLocation -> {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                this.mLatitude = aMapLocation.getLatitude();
                System.out.println("latitude------------------" + this.mLatitude);
                this.mLongitude = aMapLocation.getLongitude();
            } else {
                String errorMsg = "Failed to get location. Error code: ";
                if (aMapLocation != null) {
                    errorMsg += aMapLocation.getErrorCode() + ", error info: " + aMapLocation.getErrorInfo();
                } else {
                    errorMsg += "unknown";
                }
                Log.e("AMap",errorMsg);
            }

            locationClient.stopLocation();
            locationClient.onDestroy();
        });
    }
}
