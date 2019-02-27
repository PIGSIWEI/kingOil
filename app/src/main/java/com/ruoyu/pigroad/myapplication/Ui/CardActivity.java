package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.iflytek.cloud.thirdparty.L;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Bean.MainOilBean;
import com.ruoyu.pigroad.myapplication.Bean.NearByStoreBean;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.Util.Toastutil;
import com.stone.card.library.CardAdapter;
import com.stone.card.library.CardItemView;
import com.stone.card.library.CardSlidePanel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/4
 * Email:920015363@qq.com
 */
public class CardActivity extends FragmentActivity {
    private ActivityManager activityManager;

    private double latitude=0;
    private double longitude=0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private CardSlidePanel.CardSwitchListener cardSwitchListener;
    private List<NearByStoreBean> datalist=new ArrayList<>();
    private CardSlidePanel slidePanel ;
    @BindView(R.id.iv_close)
    ImageView iv_close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_layout);
        ButterKnife.bind(this);
        activityManager= ActivityManager.getInstance();
        activityManager.addActivity(this);
        //声明LocationClient类
        //注册监听函数
        initView();
        getLngAndLatWithNetwork();
        getData();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        slidePanel = (CardSlidePanel) findViewById(R.id.image_slide_panel);

    }
    class ViewHolder {

        ImageView imageView;
        TextView userNameTv;
        TextView card_other_description;
        Button btn_this_oil;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.card_image_view);
            userNameTv = (TextView) view.findViewById(R.id.card_user_name);
            card_other_description = (TextView) view.findViewById(R.id.card_other_description);
            btn_this_oil=view.findViewById(R.id.btn_this_oil);

        }

        public void bindData(final NearByStoreBean itemData) {

            Glide.with(CardActivity.this).load(itemData.getPic_url()).into(imageView);
            userNameTv.setText(itemData.getName());
            card_other_description.setText(itemData.getAddress());

            //确定油站信息
            btn_this_oil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(CardActivity.this,OilActivity2.class);
                    intent.putExtra("oil_title",itemData.getName());
                    intent.putExtra("oil_img",itemData.getPic_url());
                    intent.putExtra("oil_id",itemData.getId());
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    /**
     * 获取数据
     */
    public void getData(){
        datalist.clear();
        Log.i(LOG_TIP,API_URL+"?request=public.service.get.all.store&platform=app&lat="+latitude+"&lng="+longitude);
        OkGo.<String>post(API_URL+"?request=public.service.get.all.store&platform=app&lat="+latitude+"&lng="+longitude)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                JSONObject data=jsonObject.getJSONObject("data");
                                Iterator iterator = data.keys();
                                while(iterator.hasNext()){
                                    String distance = (String) iterator.next();
                                    JSONObject distanceJson = data.getJSONObject(distance);
                                    NearByStoreBean bean=new NearByStoreBean();
                                    bean.setDistance(Integer.parseInt(distance));
                                    bean.setAddress(distanceJson.getString("address"));
                                    bean.setId(distanceJson.getString("id"));
                                    bean.setName(distanceJson.getString("name"));
                                    bean.setPic_url(distanceJson.getString("pic_url"));
                                    bean.setLat(distanceJson.getInt("lat"));
                                    bean.setLng(distanceJson.getInt("lng"));
                                    datalist.add(bean);
                                }
                                relist();
                                init();
                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(CardActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    public void init(){
        cardSwitchListener=new CardSlidePanel.CardSwitchListener() {
            @Override
            public void onShow(int index) {

            }

            @Override
            public void onCardVanish(int index, int type) {
                //滑动 到最后 事件
               if (index == datalist.size()-1){
                   finish();
                   overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
               }
            }
        };
        slidePanel.setCardSwitchListener(cardSwitchListener);
        // 2. 绑定Adapter
        slidePanel.setAdapter(new CardAdapter() {
            @Override
            public int getLayoutId() {
                return R.layout.card_item;
            }

            @Override
            public int getCount() {
                return datalist.size();
            }

            @Override
            public void bindView(View view, int index) {
                Object tag = view.getTag();
                ViewHolder viewHolder;
                if (null != tag) {
                    viewHolder = (ViewHolder) tag;
                } else {
                    viewHolder = new ViewHolder(view);
                    view.setTag(viewHolder);
                }

                viewHolder.bindData(datalist.get(index));
            }

            @Override
            public Object getItem(int index) {
                return datalist.get(index);
            }


        });

        slidePanel.getAdapter().notifyDataSetChanged();
    }

    private List<NearByStoreBean> relist(){

        // 按距离排序
        Collections.sort(datalist, new Comparator<NearByStoreBean>() {
            @Override
            public int compare(NearByStoreBean o1, NearByStoreBean o2) {
                int hits0 = o1.getDistance();
                int hits1 = o2.getDistance();
                if (hits1 < hits0) {
                    return 1;
                } else if (hits1 == hits0) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });

        return datalist;
    }


    //从网络获取经纬度
    public String getLngAndLatWithNetwork() {
        double latitude = 0.0;
        double longitude = 0.0;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        return longitude + "," + latitude;
    }

    LocationListener locationListener = new LocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {

        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {

        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
        }
    };

}
