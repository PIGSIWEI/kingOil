package com.ruoyu.pigroad.myapplication;


import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

import com.baidu.idl.facesdk.FaceTracker;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.ruoyu.pigroad.myapplication.Config.APIService;
import com.ruoyu.pigroad.myapplication.Config.Config;
import com.ruoyu.pigroad.myapplication.Util.AccessToken;
import com.ruoyu.pigroad.myapplication.Util.FaceError;
import com.ruoyu.pigroad.myapplication.Util.OnResultListener;
import com.ruoyu.pigroad.myapplication.facesdk.FaceEnvironment;
import com.ruoyu.pigroad.myapplication.facesdk.FaceSDKManager;

/**
 * Created by PIGROAD on 2018/9/3
 * Email:920015363@qq.com
 */
public class MapApplication extends MultiDexApplication {

    public static final float VALUE_BRIGHTNESS = 40.0F;
    public static final float VALUE_BLURNESS = 0.7F;
    public static final float VALUE_OCCLUSION = 0.6F;
    public static final int VALUE_HEAD_PITCH = 15;
    public static final int VALUE_HEAD_YAW = 15;
    public static final int VALUE_HEAD_ROLL = 15;
    public static final int VALUE_CROP_FACE_SIZE = 400;
    public static final int VALUE_MIN_FACE_SIZE = 120;
    public static final float VALUE_NOT_FACE_THRESHOLD = 0.6F;


    @Override
    public void onCreate() {
        super.onCreate();
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);



    }


}
