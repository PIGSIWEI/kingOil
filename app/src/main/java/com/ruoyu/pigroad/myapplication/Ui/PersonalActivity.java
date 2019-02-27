package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.MainActivity;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PIGROAD on 2018/9/11
 * Email:920015363@qq.com
 */
public class PersonalActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.ll_spending)
    LinearLayout ll_spending;
    @BindView(R.id.ll_bill)
    LinearLayout ll_bill;
    @BindView(R.id.ll_car)
    LinearLayout ll_car;
    @BindView(R.id.ll_coupon_manager)
    LinearLayout ll_coupon_manager;
    @BindView(R.id.btn_login_exit)
    Button btn_login_exit;
    @BindView(R.id.iv_user_logo)
    ImageView iv_user_logo;
    @BindView(R.id.tv_user_phone)
    TextView tv_user_phone;
    @BindView(R.id.tv_authentication)
    TextView tv_authentication;

    private ActivityManager activityManager;
    private String phone = "";
    private String headimgurl = "";
    private String car_type_id = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_layout);
        ButterKnife.bind(this);
        activityManager = ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    private void init() {

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        ll_spending.setOnClickListener(this);
        ll_bill.setOnClickListener(this);
        ll_car.setOnClickListener(this);
        ll_coupon_manager.setOnClickListener(this);
        btn_login_exit.setOnClickListener(this);

        phone = getIntent().getStringExtra("phone");
        headimgurl = getIntent().getStringExtra("headimgurl");
        car_type_id = getIntent().getStringExtra("car_type_id");

        if (phone.equals("")) {

        } else {
            String phone_head=phone.substring(0,3);
            String phone_foot=phone.substring(7);
            tv_user_phone.setText(phone_head+"****"+phone_foot);
        }

        if (headimgurl.equals("")) {
            iv_user_logo.setBackgroundResource(R.drawable.icon_user);
        } else {
            Glide.with(this).load(headimgurl).into(iv_user_logo);
        }

        if (car_type_id.equals("")) {

        } else {
            tv_authentication.setText(car_type_id);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_spending:
                Intent intent = new Intent(PersonalActivity.this, RecordActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_bill:
                Intent intent1 = new Intent(PersonalActivity.this, BillActivity.class);
                startActivity(intent1);
                break;
            case R.id.ll_car:
                Intent intent2 = new Intent(PersonalActivity.this, SpecialCarActivity.class);
                startActivity(intent2);
                break;
            case R.id.ll_coupon_manager:
                Intent intent3 = new Intent(PersonalActivity.this, CouponManagerActivity.class);
                startActivity(intent3);
                break;
            case R.id.btn_login_exit:
                //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
                AlertDialog.Builder builder = new AlertDialog.Builder(PersonalActivity.this);
                builder.setMessage("确认退出此用户吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activityManager.exit();
                        //clear token
                        SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        //返回登录界面
                        Intent intent4 = new Intent(PersonalActivity.this, LoginActivity.class);
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

        }
    }
}
