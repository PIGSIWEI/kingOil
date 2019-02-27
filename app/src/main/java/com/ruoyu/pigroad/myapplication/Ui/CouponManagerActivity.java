package com.ruoyu.pigroad.myapplication.Ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruoyu.pigroad.myapplication.Fragment.CouponFragment1;
import com.ruoyu.pigroad.myapplication.Fragment.CouponFragment2;
import com.ruoyu.pigroad.myapplication.Fragment.CouponFragment3;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PIGROAD on 2018/9/13
 * Email:920015363@qq.com
 */
public class CouponManagerActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_titile)
    TextView tv_title;

    @BindView(R.id.tv_used)
    TextView tv_used;
    @BindView(R.id.tv_can)
    TextView tv_can;
    @BindView(R.id.tv_over)
    TextView tv_over;
    @BindView(R.id.content)
    FrameLayout content;


    private ActivityManager activityManager;
    private int mDefaultColor= Color.parseColor("#969696");
    private int mActiveColor= Color.parseColor("#32CD32");
    private FragmentManager fragmentManager;
    private List<View> bottomTabs=new ArrayList<>();

    private CouponFragment1 couponFragment1;
    private CouponFragment2 couponFragment2;
    private CouponFragment3 couponFragment3;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coupon_manager_layout);
        ButterKnife.bind(this);
        activityManager= ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_title.setText("优惠券管理");

        tv_can.setOnClickListener(this);
        tv_used.setOnClickListener(this);
        tv_over.setOnClickListener(this);

        fragmentManager = getSupportFragmentManager();

        bottomTabs.add(tv_can);
        bottomTabs.add(tv_used);
        bottomTabs.add(tv_over);

        setSelectTab(0);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_can:
                setSelectTab(0);
                break;
            case R.id.tv_used:
                setSelectTab(1);
                break;
            case R.id.tv_over:
                setSelectTab(2);
                break;
        }
    }
    /**
     * Fragment选择方法
     */
    public void setSelectTab(int index) {
        clearSelection();
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);

        switch (index) {
            case 0:
                tv_can.setTextColor(mActiveColor);

                if (couponFragment1 == null) {
                    couponFragment1 = new CouponFragment1();
                    transaction.add(R.id.content, couponFragment1);

                } else {
                    transaction.show(couponFragment1);
                }
                break;
            case 1:
                tv_used.setTextColor(mActiveColor);

                if (couponFragment2 == null) {
                    couponFragment2 = new CouponFragment2();
                    transaction.add(R.id.content, couponFragment2);
                } else {
                    transaction.show(couponFragment2);
                }
                break;
            case 2:
                tv_over.setTextColor(mActiveColor);
                if (couponFragment3 == null) {
                    couponFragment3 = new CouponFragment3();
                    transaction.add(R.id.content, couponFragment3);
                } else {
                    transaction.show(couponFragment3);
                }
                break;
            default:
                break;
        }
        transaction.commit();
    }

    /**
     * 开启fragment事物
     */
    private void clearSelection() {
        tv_can.setTextColor(mDefaultColor);
        tv_used.setTextColor(mDefaultColor);
        tv_over.setTextColor(mDefaultColor);
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (couponFragment1 != null) {
            transaction.hide(couponFragment1);
        }
        if (couponFragment2 != null) {
            transaction.hide(couponFragment2);
        }
        if (couponFragment3 != null) {
            transaction.hide(couponFragment3);
        }

    }





}
