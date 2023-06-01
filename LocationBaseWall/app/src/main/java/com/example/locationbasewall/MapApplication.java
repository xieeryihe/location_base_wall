package com.example.locationbasewall;

import android.app.Application;
import android.content.Context;

import com.amap.api.location.AMapLocationClient;

public class MapApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = this;
        //定位隐私政策同意
        System.out.println("--------------------check permission------------------");

        AMapLocationClient.updatePrivacyShow(context,true,true);
        AMapLocationClient.updatePrivacyAgree(context,true);
//        //地图隐私政策同意
//        MapsInitializer.updatePrivacyShow(context,true,true);
//        MapsInitializer.updatePrivacyAgree(context,true);
//        //搜索隐私政策同意
//        ServiceSettings.updatePrivacyShow(context,true,true);
//        ServiceSettings.updatePrivacyAgree(context,true);
    }
}


