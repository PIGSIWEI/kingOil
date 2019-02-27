package com.ruoyu.pigroad.myapplication.Util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by PIGROAD on 2018/9/17
 * Email:920015363@qq.com
 */
public class MyOrientationListener implements SensorEventListener {

    private SensorManager mSensorManager;
    private Context mContext;

    private Sensor accelerometer; // 加速度传感器
    private Sensor magnetic; // 地磁场传感器
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    private float lastX;

    public MyOrientationListener(Context context) {
        this.mContext = context;
    }

    public void start() {
        // 实例化传感器管理者
        mSensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        // 初始化加速度传感器
        accelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 初始化地磁场传感器
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // 注册监听
        mSensorManager.registerListener(this, accelerometer,
                Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, magnetic,
                Sensor.TYPE_MAGNETIC_FIELD);

        // if(mSensorManager != null){
        // //获得方向传感器
        // mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        // }
        // if(mSensor != null){
        // mSensorManager.registerListener(this,
        // mSensor,SensorManager.SENSOR_DELAY_UI);
        // }

    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // if(event.sensor.getType() == Sensor.TYPE_ORIENTATION);{
        // float x = event.values[SensorManager.DATA_X];
        //
        // if(Math.abs(x - lastX) > 1.0){
        // if(mOnOrientationListener != null){
        // mOnOrientationListener.onOrientationChanged(x);
        // }
        // }
        //
        // lastX = x;
        // }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values;
        }

        float x = calculateOrientation();

        if (mOnOrientationListener != null) {
            mOnOrientationListener.onOrientationChanged(x);
        }

        lastX = x;
    }

    /**
     * 计算角度
     */
    private float calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];

        mSensorManager.getRotationMatrix(R, null, accelerometerValues,
                magneticFieldValues);
        mSensorManager.getOrientation(R, values);

        //换算成角度值
        values[0] = (float) Math.toDegrees(values[0]);

        return values[0];
    }

    //对外数据接口
    public OnOrientationListener mOnOrientationListener;
    public void setOnOrientationListener(
            OnOrientationListener mOnOrientationListener) {
        this.mOnOrientationListener = mOnOrientationListener;
    }
    public interface OnOrientationListener {
        void onOrientationChanged(float x);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
