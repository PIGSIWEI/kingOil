package com.ruoyu.pigroad.myapplication.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.github.florent37.fiftyshadesof.FiftyShadesOf;
import com.iflytek.cloud.thirdparty.V;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Adapter.MainOilAdapter;
import com.ruoyu.pigroad.myapplication.Bean.MainOilBean;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.MainActivity;
import com.ruoyu.pigroad.myapplication.MainActivity2;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.GlideImageLoader;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import org.json.JSONException;
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
 * Created by PIGROAD on 2018/9/18
 * Email:920015363@qq.com
 */
public class MainOilFragment extends Fragment {

    private Banner banner;
    private MainOilAdapter adapter;
    private List<MainOilBean> datas = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayout ll_null;

    private double latitude = 0;
    private double longitude = 0;

    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口
//原有BDLocationListener接口暂时同步保留。具体介绍请参考后文第四步的说明
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_oil_fragment, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //注册监听函数
        showContacts();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 初始化
     */
    private void init() {
        banner = getActivity().findViewById(R.id.banner);
        //设置banner样式
        banner.setImageLoader(new GlideImageLoader());
        List<String> images = new ArrayList<>();
        images.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537249757812&di=fbaec5259f776aa07fc4a326b0efe88c&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2Fe824b899a9014c08a8c553a5007b02087bf4f430.jpg");
        images.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537249788897&di=84d2855083d75137a287cc7ad12e3eac&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimage%2Fc0%253Dshijue1%252C0%252C0%252C294%252C40%2Fsign%3D993453a9788b4710da22f58faba7a988%2Fb812c8fcc3cec3fddf960cfddc88d43f86942789.jpg");
        banner.setImages(images);
        //设置banner动画效果
        banner.setBannerAnimation(Transformer.Accordion);
        //设置自动轮播，默认为true
        banner.isAutoPlay(true);
        //banner设置方法全部调用完毕时最后调用
        banner.start();

        recyclerView = getActivity().findViewById(R.id.recycler_view);
        ll_null = getActivity().findViewById(R.id.ll_null);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        adapter = new MainOilAdapter(datas, getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getLngAndLatWithNetwork();
        getData();
    }

    private void getData() {
        datas.clear();
        Log.i(LOG_TIP,API_URL + "?request=public.service.get.all.store&platform=app&lat="+latitude+"&lng="+longitude);
        OkGo.<String>post(API_URL + "?request=public.service.get.all.store&platform=app&lat="+latitude+"&lng="+longitude)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            Log.i(LOG_TIP, response.body());
                            JSONObject jsonObject = new JSONObject(response.body());
                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                JSONObject data = jsonObject.getJSONObject("data");
                                Iterator iterator = data.keys();
                                while (iterator.hasNext()) {
                                    String distance = (String) iterator.next();
                                    JSONObject distanceJson = data.getJSONObject(distance);
                                    MainOilBean bean = new MainOilBean();
                                    bean.setId(distanceJson.getInt("id"));
                                    bean.setDistance(Integer.parseInt(distance));
                                    bean.setAddress(distanceJson.getString("address"));
                                    bean.setName(distanceJson.getString("name"));
                                    bean.setPic_url(distanceJson.getString("pic_url"));
                                    bean.setLng(distanceJson.getDouble("lng"));
                                    bean.setLat(distanceJson.getDouble("lat"));
                                    datas.add(bean);
                                }
                                relist();
                                adapter.notifyDataSetChanged();
                            } else if (code == 999) {
                                //clear token
                                SharedPreferences preferences = getActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4 = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent4);
                                getActivity().finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private List<MainOilBean> relist(){

        // 按距离排序
        Collections.sort(datas, new Comparator<MainOilBean>() {
            @Override
            public int compare(MainOilBean o1, MainOilBean o2) {
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

        return datas;
    }

    public void showContacts(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity().getApplicationContext(),"没有权限,请手动开启定位权限",Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 100);
        }else{
            init();
        }
    }

    //Android6.0申请权限的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                    init();
                } else {
                    // 没有获取到权限，做特殊处理
                    Toast.makeText(getActivity().getApplicationContext(), "获取位置权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                    ll_null.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }

    //从网络获取经纬度
    public String getLngAndLatWithNetwork() {
        double latitude = 0.0;
        double longitude = 0.0;
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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
