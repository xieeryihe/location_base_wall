package com.example.locationbasewall.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;

public class Location {
    private final Context mContext;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;

    public double mLatitude;
    public double mLongitude;
    public String address;

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
            mLocationClient = new AMapLocationClient(mContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mLocationOption = new AMapLocationClientOption();
        //开启高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(20000);
        //关闭缓存机制，高精度定位会产生缓存。
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationOption.setOnceLocation(true);

        // 给Client设置Option
        mLocationClient.setLocationOption(mLocationOption);


        mLocationClient.startLocation();
        mLocationClient.setLocationListener(aMapLocation -> {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                this.mLatitude = aMapLocation.getLatitude();
                this.mLongitude = aMapLocation.getLongitude();

                this.address = aMapLocation.getAddress();
                System.out.println("latitude:" + this.mLatitude);
                System.out.println("longitude:" + this.mLongitude);
                System.out.println(address);


            } else {
                String errorMsg = "Failed to get location. Error code: ";
                if (aMapLocation != null) {
                    errorMsg += aMapLocation.getErrorCode() + ", error info: " + aMapLocation.getErrorInfo();
                } else {
                    errorMsg += "unknown";
                }
                Log.e("AMap",errorMsg);
            }

            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        });
    }

}
