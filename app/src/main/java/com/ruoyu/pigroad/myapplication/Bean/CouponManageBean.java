package com.ruoyu.pigroad.myapplication.Bean;

/**
 * Created by PIGROAD on 2018/9/13
 * Email:920015363@qq.com
 */
public class CouponManageBean {
    public int id;
    public String coupon_name;
    public String start_time;
    public String expire_time;
    public String min_money;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCoupon_name() {
        return coupon_name;
    }

    public void setCoupon_name(String coupon_name) {
        this.coupon_name = coupon_name;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getExpire_time() {
        return expire_time;
    }

    public void setExpire_time(String expire_time) {
        this.expire_time = expire_time;
    }

    public String getMin_money() {
        return min_money;
    }

    public void setMin_money(String min_money) {
        this.min_money = min_money;
    }
}
