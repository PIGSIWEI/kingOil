package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.INaviInfoCallback;
import com.amap.api.navi.model.AMapNaviLocation;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.iflytek.cloud.thirdparty.L;
import com.iflytek.cloud.thirdparty.S;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Adapter.OilDetailAdapter;
import com.ruoyu.pigroad.myapplication.Bean.OilDetailBean;
import com.ruoyu.pigroad.myapplication.GaodeMap.IndexActivity;
import com.ruoyu.pigroad.myapplication.MainActivity2;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.Toastutil;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/19
 * Email:920015363@qq.com
 */
public class MainOilDetailActivity extends AppCompatActivity implements INaviInfoCallback {

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.iv_oil_img)
    ImageView iv_oil_img;
    @BindView(R.id.tv_titile)
    TextView tv_title;
    @BindView(R.id.tv_oil_address)
    TextView tv_oil_address;
    @BindView(R.id.tv_oil_name)
    TextView tv_oil_name;
    @BindView(R.id.tv_oil_adress2)
    TextView tv_oil_adress2;
    @BindView(R.id.tv_oil_distance)
    TextView tv_oil_distance;
    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;
    @BindView(R.id.btn_pay)
    Button btn_pay;
    @BindView(R.id.ll_daohang)
    LinearLayout ll_daohang;

    private String store_id;

    private OilDetailAdapter adapter;
    private List<OilDetailBean> datas =new ArrayList<>();
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    private double latitude=0;
    private double longitude=0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_oil_detail_layout);
        ButterKnife.bind(this);
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {
        mLocationClient = new LocationClient(MainOilDetailActivity.this);
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_title.setText(getIntent().getStringExtra("oil_name"));
        tv_oil_name.setText(getIntent().getStringExtra("oil_name"));

        Glide.with(this).load(getIntent().getStringExtra("oil_img")).into(iv_oil_img);

        tv_oil_address.setText(getIntent().getStringExtra("oil_adress"));
        tv_oil_adress2.setText(getIntent().getStringExtra("oil_adress"));

        tv_oil_distance.setText("距离"+getIntent().getStringExtra("oil_distance")+"公里");

        store_id=getIntent().getStringExtra("store_id");

        adapter=new OilDetailAdapter(this,datas);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);

        recycler_view.setAdapter(adapter);
        recycler_view.setLayoutManager(linearLayoutManager);
        recycler_view.setHasFixedSize(true);
        recycler_view.setNestedScrollingEnabled(false);

        getData();

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainOilDetailActivity.this,OilActivity2.class);
                intent.putExtra("oil_title",getIntent().getStringExtra("oil_name"));
                intent.putExtra("oil_img",getIntent().getStringExtra("oil_img"));
                intent.putExtra("oil_id",store_id);
                startActivity(intent);
                finish();
            }
        });
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认GCJ02
        //GCJ02：国测局坐标；
        //BD09ll：百度经纬度坐标；
        //BD09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标

        option.setScanSpan(1000);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0

        mLocationClient.start();
        //mLocationClient为第二步初始化过的LocationClient对象
        //调用LocationClient的start()方法，便可发起定位请求
        ll_daohang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOilInfo();
                        }
        });

    }

    private void getData(){
        OkGo.<String>post(API_URL+"?request=private.gas_station.get.store.all.oil.type&platform=app&store_id="+store_id)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code ==0){
                                JSONArray data=jsonObject.getJSONArray("data");
                                for (int i=0;i<data.length();i++){
                                    JSONObject temp=data.getJSONObject(i);
                                    OilDetailBean bean=new OilDetailBean();
                                    bean.setCountry_oil_price(temp.getString("country_oil_price"));
                                    bean.setMerchant_oil_price(temp.getString("merchant_oil_price"));
                                    bean.setOil_name(temp.getString("oil_name"));
                                    datas.add(bean);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onArriveDestination(boolean b) {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onStopSpeaking() {

    }

    @Override
    public void onReCalculateRoute(int i) {

    }

    @Override
    public void onExitPage(int i) {

    }

    @Override
    public void onStrategyChanged(int i) {

    }

    @Override
    public View getCustomNaviBottomView() {
        return null;
    }

    @Override
    public View getCustomNaviView() {
        return null;
    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            latitude = location.getLatitude();    //获取纬度信息
            longitude = location.getLongitude();    //获取经度信息

            getData();

            float radius = location.getRadius();    //获取定位精度，默认值为0.0f

            String coorType = location.getCoorType();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = location.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
        }
    }

    private void checkOilInfo(){
        OkGo.<String>post(API_URL+"?request=public.service.get.the.store.navigation&platform=app&sid="+store_id)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject= new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                double oil_lat=jsonObject.getDouble("lat");
                                double oil_lng=jsonObject.getDouble("lng");
                                final LatLng now_location=new LatLng(latitude,longitude);
                                final LatLng oil_location=new LatLng(oil_lat,oil_lng);
                                final ZLoadingDialog dialog = new ZLoadingDialog(MainOilDetailActivity.this);
                                dialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                                        .setLoadingColor(Color.parseColor("#32cd32"))//颜色
                                        .setHintText("加载中")
                                        .setCanceledOnTouchOutside(false)
                                        .setHintTextSize(16f)
                                        .setCancelable(false)
                                        .show();
                                new Handler().postDelayed(new Runnable(){
                                    public void run(){
                                        //execute the task
                                        dialog.dismiss();
                                        AmapNaviParams params = new AmapNaviParams(new Poi("现在的位置", now_location, ""), null, new Poi(getIntent().getStringExtra("oil_name"), oil_location, ""), AmapNaviType.DRIVER);
                                        params.setUseInnerVoice(true);
                                        AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params, MainOilDetailActivity.this);

                                    }
                                },2000);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


}
