package com.ruoyu.pigroad.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Bean.NearByStoreBean;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.Ui.CardActivity;
import com.ruoyu.pigroad.myapplication.Ui.PersonalActivity;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.Util.MyOrientationListener;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;


public class MainActivity extends FragmentActivity implements SensorEventListener {

    private MapView mMapView;
    private SensorManager mSensorManager;
    private BaiduMap mBaiduMap;
    private MyLocationData locData;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;  //定位logo
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private String login_token;
    private String phone = "";
    private String headimgurl = "";
    private String car_type_id = "";

    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    // UI相关
    RadioGroup.OnCheckedChangeListener radioButtonListener;
    private List<NearByStoreBean> datalist = new ArrayList<>();

    private float mCurrentX;
    private ActivityManager activityManager;
    private MyOrientationListener myOrientationListener;

    Button requestLocButton;
    boolean isFirstLoc = true; // 是否首次定位
    @BindView(R.id.btn_get_nearby)
    Button btn_get_nearby;

    @BindView(R.id.iv_user)
    ImageView iv_user;
    @BindView(R.id.btn_location)
    Button btn_location;

    @BindView(R.id.iv_search)
    ImageView iv_search;

    boolean useDefaultIcon = false;//是否使用默认图标
    // 存储当前我的定位信息
    public BDLocation currlocation;
    //当前选择的位置坐标
    public double mLatitude;
    public double mLongitude;
    private boolean isFirstIn = true;
    //展示路径规划
    private boolean isShowRoutePlan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ActivityManager activityManager = ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    private void init() {

        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                mCurrentMode, true, null));
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.overlook(0);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        // 地图初始化
        mLocClient.setLocOption(option);
        mLocClient.start();
//        //线程 开始
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                //获取附件油站
//                startActivity(new Intent(MainActivity.this,CardActivity.class));
//                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//            }
//        }).start();

        btn_get_nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
                loadingDialog.setLoadingText("正在获取附近油站，请稍等")
                        .setInterceptBack(true)
                        .setLoadSpeed(LoadingDialog.Speed.SPEED_TWO)
                        .closeSuccessAnim()
                        .show();
                CountDownTimer countDownTimer = new CountDownTimer(3000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        loadingDialog.loadSuccess();
                    }
                };
                countDownTimer.start();
                loadingDialog.setOnFinishListener(new LoadingDialog.OnFinshListener() {
                    @Override
                    public void onFinish() {
                        //获取附件油站
                        startActivity(new Intent(MainActivity.this, CardActivity.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                });
            }
        });

        getInfo();

        //个人中心
        iv_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PersonalActivity.class);
                intent.putExtra("phone", phone);
                intent.putExtra("headimgurl", headimgurl);
                intent.putExtra("car_type_id", car_type_id);
                startActivity(intent);
            }
        });



        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerToMyLocation();
            }
        });

    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 获取个人信息
     */
    private void getInfo() {
        OkGo.<String>post(API_URL + "?request=private.user.get_user_info_app&platform=app&token=" + login_token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                JSONObject data = jsonObject.getJSONObject("data");
                                phone = data.getString("phone");
                                headimgurl = data.getString("headimgurl");
                                car_type_id = data.getString("car_name");
                            } else if (code == 999) {
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4 = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //获取移动后屏幕中间经纬度
    protected void updateMapState(MapStatus status) {
        LatLng mCenterLatLng = status.target;
        //获取经纬度
        mLatitude = mCenterLatLng.latitude;
        mLongitude = mCenterLatLng.longitude;
    }

    /**
     * 定位监听类
     *
     * @author Administrator
     */
    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            if (location == null) {
                return;
            }
            currlocation = location;
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }

    /**
     * 定位到我的位置
     */
    private void centerToMyLocation() {
        LatLng ll = new LatLng(currlocation.getLatitude(), currlocation.getLongitude());
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        mBaiduMap.animateMapStatus(update);
    }


}
