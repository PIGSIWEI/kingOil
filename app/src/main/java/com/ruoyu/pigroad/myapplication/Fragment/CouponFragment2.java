package com.ruoyu.pigroad.myapplication.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Adapter.CouponManageUsedAdapter;
import com.ruoyu.pigroad.myapplication.Bean.CouponManageBean;
import com.ruoyu.pigroad.myapplication.Bean.CouponManageBean2;
import com.ruoyu.pigroad.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/13
 * Email:920015363@qq.com
 */
public class CouponFragment2 extends Fragment {

    private List<CouponManageBean2> datas=new ArrayList<>();
    private CouponManageUsedAdapter adapter;
    private String login_token;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.coupon_fragment2,container,false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.init();
    }

    private void init() {
        //拿token
        SharedPreferences sp = getActivity().getSharedPreferences("USER", getActivity().MODE_PRIVATE);
        login_token=sp.getString("user_token",null);

        recyclerView=getActivity().findViewById(R.id.rv2);
        adapter=new CouponManageUsedAdapter(getActivity(),datas);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        getData();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TIP,"c2_resume");
    }

    /**
     * 获取数据
     */
    private void getData(){
        OkGo.<String>post(API_URL+"?request=private.coupon.get.unvalid.coupon.list&token="+login_token+"&platform=app")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code = jsonObject.getInt("code");
                            if (code == 0){
                                JSONArray data=jsonObject.getJSONArray("data");
                                if (data.length() == 0){

                                }else {
                                    for (int i=0;i<data.length();i++){
                                        JSONObject temp =data.getJSONObject(i);
                                        CouponManageBean2 bean=new CouponManageBean2();
                                        String star_time=temp.getString("start_time").substring(0,10);
                                        String end_time=temp.getString("expire_time").substring(0,10);
                                        bean.setCoupon_name(temp.getString("coupon_name"));
                                        bean.setExpire_time(end_time);
                                        bean.setId(temp.getInt("id"));
                                        bean.setMin_money(temp.getString("min_money"));
                                        bean.setStart_time(star_time);
                                        datas.add(bean);
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
