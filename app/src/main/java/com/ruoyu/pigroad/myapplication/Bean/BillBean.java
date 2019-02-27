package com.ruoyu.pigroad.myapplication.Bean;

/**
 * Created by PIGROAD on 2018/9/10
 * Email:920015363@qq.com
 */
public class BillBean {
    public  String bill_title;
    public String bill_number;
    public int is_select;
    public int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIs_select() {
        return is_select;
    }

    public void setIs_select(int is_select) {
        this.is_select = is_select;
    }

    public String getBill_title() {
        return bill_title;
    }

    public void setBill_title(String bill_title) {
        this.bill_title = bill_title;
    }

    public String getBill_number() {
        return bill_number;
    }

    public void setBill_number(String bill_number) {
        this.bill_number = bill_number;
    }
}
