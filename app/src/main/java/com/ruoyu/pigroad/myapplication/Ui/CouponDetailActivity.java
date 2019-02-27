package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/14
 * Email:920015363@qq.com
 */
public class CouponDetailActivity extends AppCompatActivity{

    @BindView(R.id.tv_titile)
    TextView tv_title;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_coupon_name)
    TextView tv_coupon_name;
    @BindView(R.id.tv_coupon_money)
    TextView tv_coupon_money;
    @BindView(R.id.tv_min_money)
    TextView tv_min_money;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_valid_store_text)
    TextView tv_valid_store_text;
    @BindView(R.id.tv_oil_type_text)
    TextView tv_oil_type_text;
    @BindView(R.id.tv_valid_level_text)
    TextView tv_valid_level_text;
    @BindView(R.id.tv_valid_zc_text)
    TextView tv_valid_zc_text;
    @BindView(R.id.tv_tip)
    TextView tv_tip;
    @BindView(R.id.btn_use)
    Button btn_use;

    private String id;
    private ActivityManager activityManager;
    private String login_token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coupon_detail_layout);
        ButterKnife.bind(this);
        activityManager= ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    private void init() {
        tv_title.setText("优惠券详情");

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        id=getIntent().getStringExtra("id");

        //token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);

        GetData();

        btn_use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    /**
     * 查询优惠券
     */
    private void GetData(){
        OkGo.<String>post(API_URL+"?request=private.coupon.get.the.coupon&token="+login_token+"&platform=app&id="+id)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                JSONObject data=jsonObject.getJSONObject("data");
                                tv_coupon_name.setText(data.getString("coupon_name"));
                                tv_coupon_money.setText("￥"+data.getInt("coupon_money"));
                                tv_min_money.setText("￥"+data.getInt("min_money"));
                                tv_time.setText(data.getString("start_time")+"至"+data.getString("expire_time"));
                                tv_valid_store_text.setText(data.getString("valid_store_text"));
                                tv_oil_type_text.setText(data.getString("oil_type_text"));
                                tv_valid_level_text.setText(data.getString("valid_level_text"));
                                tv_valid_zc_text.setText(data.getString("valid_zc_text"));
                                tv_tip.setText("（1）优惠券可用日期为"+data.getString("start_time")+"至"+data.getString("expire_time")+"；");

                                int use_status=data.getInt("use_status");
                                //判断是否使用优惠券
                                if (use_status == 0){
                                    btn_use.setEnabled(true);
                                }else if (use_status == 1){
                                    btn_use.setEnabled(false);
                                    btn_use.setText("已使用");
                                    btn_use.setBackgroundColor(Color.parseColor("#9ED99D"));
                                }else if (use_status ==2){
                                    btn_use.setEnabled(false);
                                    btn_use.setText("已过期");
                                    btn_use.setBackgroundColor(Color.parseColor("#969696"));
                                }


                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(CouponDetailActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
