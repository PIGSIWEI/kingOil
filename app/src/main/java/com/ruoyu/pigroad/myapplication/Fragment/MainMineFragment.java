package com.ruoyu.pigroad.myapplication.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.MainActivity;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Ui.BillActivity;
import com.ruoyu.pigroad.myapplication.Ui.CouponManagerActivity;
import com.ruoyu.pigroad.myapplication.Ui.FaceActivity;
import com.ruoyu.pigroad.myapplication.Ui.PersonalActivity;
import com.ruoyu.pigroad.myapplication.Ui.PersonalBillManager;
import com.ruoyu.pigroad.myapplication.Ui.RecordActivity;
import com.ruoyu.pigroad.myapplication.Ui.SpecialCarActivity;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.Util.Toastutil;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/18
 * Email:920015363@qq.com
 */
public class MainMineFragment extends Fragment implements View.OnClickListener {

    private LinearLayout ll_bill, ll_car, ll_coupon_manager,ll_spending,ll_face;
    private ImageView iv_user_logo;
    private TextView tv_user_phone,tv_authentication;
    private ImageView iv_login_out;

    private String phone = "";
    private String headimgurl = "";
    private String car_type_id = "";
    private ActivityManager activityManager;

    private String login_token;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_mine_fragment, container, false);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {
        activityManager= ActivityManager.getInstance();
        activityManager.addActivity(getActivity());
        //拿token
        SharedPreferences sp = getActivity().getSharedPreferences("USER", getActivity().MODE_PRIVATE);
        login_token = sp.getString("user_token", null);

        ll_bill = getActivity().findViewById(R.id.ll_bill);
        ll_car = getActivity().findViewById(R.id.ll_car);
        ll_coupon_manager = getActivity().findViewById(R.id.ll_coupon_manager);
        ll_spending = getActivity().findViewById(R.id.ll_spending);
        ll_face = getActivity().findViewById(R.id.ll_face);
        tv_user_phone = getActivity().findViewById(R.id.tv_user_phone);
        iv_user_logo = getActivity().findViewById(R.id.iv_user_logo);
        tv_authentication = getActivity().findViewById(R.id.tv_authentication);
        iv_login_out = getActivity().findViewById(R.id.iv_login_out);

        ll_bill.setOnClickListener(this);
        ll_car.setOnClickListener(this);
        ll_coupon_manager.setOnClickListener(this);
        ll_spending.setOnClickListener(this);
        iv_login_out.setOnClickListener(this);
        ll_face.setOnClickListener(this);


        getInfo();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_spending:
                Intent intent = new Intent(getActivity(), RecordActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_bill:
                Intent intent1 = new Intent(getActivity(), PersonalBillManager.class);
                startActivity(intent1);
                break;
            case R.id.ll_car:
                Intent intent2 = new Intent(getActivity(), SpecialCarActivity.class);
                startActivity(intent2);
                break;
            case R.id.ll_coupon_manager:
                Intent intent3 = new Intent(getActivity(), CouponManagerActivity.class);
                startActivity(intent3);
                break;
            case R.id.iv_login_out:
                //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("确认退出此用户吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activityManager.exit();
                        //clear token
                        SharedPreferences preferences = getActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        //返回登录界面
                        Intent intent4 = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent4);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                break;
            case R.id.ll_face:
                Intent intent4=new Intent(getActivity(), FaceActivity.class);
                startActivity(intent4);
                break;
        }
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

                                if (phone.equals("")) {

                                } else {
                                    String phone_head=phone.substring(0,3);
                                    String phone_foot=phone.substring(7);
                                    tv_user_phone.setText(phone_head+"****"+phone_foot);
                                }

                                if (headimgurl.equals("")) {
                                    iv_user_logo.setBackgroundResource(R.drawable.icon_user);
                                } else {
                                    Glide.with(getActivity()).load(headimgurl).into(iv_user_logo);
                                }

                                if (car_type_id.equals("")) {

                                } else {
                                    tv_authentication.setText(car_type_id);
                                }
                            } else if (code == 999){
                                Toastutil.show(getActivity(),"登录已过期，请重新登录");
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent4);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


}
