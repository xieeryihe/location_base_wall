package com.example.locationbasewall.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class Location {
    private final Context mContext;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;

    private double mLatitude;
    private double mLongitude;
    private String mAddress;

    public Location(Context context) {
        this.mContext = context;
    }

    public void getCurrentLocation(LocationCallback callback) {
        if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("AMap", "Location permissions not granted");
            return;
        }

        try {
            mLocationClient = new AMapLocationClient(mContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);  // 高精度定位
        mLocationOption.setOnceLocationLatest(true);  //
        mLocationOption.setNeedAddress(true);  //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setHttpTimeOut(20000);
        mLocationOption.setLocationCacheEnable(false);
        mLocationOption.setOnceLocation(true);
        mLocationClient.setLocationOption(mLocationOption);

        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                    mLatitude = aMapLocation.getLatitude();
                    mLongitude = aMapLocation.getLongitude();
                    mAddress = aMapLocation.getAddress();

                    callback.onLocationReceived(mLatitude, mLongitude, mAddress);
                } else {
                    String errorMsg = "Failed to get location. Error code: ";
                    if (aMapLocation != null) {
                        errorMsg += aMapLocation.getErrorCode() + ", error info: " + aMapLocation.getErrorInfo();
                    } else {
                        errorMsg += "unknown";
                    }
                    Log.e("AMap", errorMsg);
                    callback.onLocationFailed(errorMsg);
                }

                mLocationClient.stopLocation();
                mLocationClient.onDestroy();
            }
        });

        mLocationClient.startLocation();
    }

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude, String address);

        void onLocationFailed(String errorMsg);
    }
}
