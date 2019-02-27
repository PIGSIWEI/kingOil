package com.ruoyu.pigroad.myapplication.Ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ruoyu.pigroad.myapplication.facesdk.FaceSDKManager;


/**
 * Created by PIGROAD on 2018/10/24
 * Email:920015363@qq.com
 */
public class FaceReActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init() {
        // 如果图片中的人脸小于200*200个像素，将不能检测出人脸，可以根据需求在100-400间调节大小
        FaceSDKManager.getInstance().getFaceTracker(this).set_min_face_size(200);
        FaceSDKManager.getInstance().getFaceTracker(this).set_isCheckQuality(true);
        // 该角度为商学，左右，偏头的角度的阀值，大于将无法检测出人脸，为了在1：n的时候分数高，注册尽量使用比较正的人脸，可自行条件角度
        FaceSDKManager.getInstance().getFaceTracker(this).set_eulur_angle_thr(45, 45, 45);
        FaceSDKManager.getInstance().getFaceTracker(this).set_isVerifyLive(true);
    }

}
