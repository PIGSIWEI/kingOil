package com.ruoyu.pigroad.myapplication.Bean;

/**
 * Created by PIGROAD on 2018/9/18
 * Email:920015363@qq.com
 */
public class MainOilBean implements Comparable<MainOilBean>{
    private String address;
    private int distance;
    private String pic_url;
    private String name;
    private int id;
    private double lat;
    private double lng;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getPic_url() {
        return pic_url;
    }

    public void setPic_url(String pic_url) {
        this.pic_url = pic_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int compareTo(MainOilBean s) {
        //自定义比较方法，如果认为此实体本身大则返回1，否则返回-1
        if(this.distance >= s.getDistance()){
            return 1;
        }
        return -1;
    }

}
