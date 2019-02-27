package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Adapter.CouponAdapter;
import com.ruoyu.pigroad.myapplication.Adapter.OnItemClickLitener;
import com.ruoyu.pigroad.myapplication.Bean.CouponBean;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/10
 * Email:920015363@qq.com
 */
public class CouponActivity extends AppCompatActivity{

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_titile)
    TextView tv_titile;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.ll_info)
    LinearLayout ll_info;
    @BindView(R.id.rl_null)
    RelativeLayout rl_null;

    private String login_token;
    private List<CouponBean> datas=new ArrayList<>();
    private CouponAdapter adapter;
    private String oil_money;
    private String oil_id;
    private String oil_type;
    private ActivityManager activityManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coupon_layout);
        ButterKnife.bind(this);
        activityManager=ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    /**
     * 初始化
     */
    private void init(){

        //获取金额
        oil_money=getIntent().getStringExtra("oil_money");
        oil_id=getIntent().getStringExtra("oil_id");
        oil_type=getIntent().getStringExtra("oil_type");

        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token=sp.getString("user_token",null);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CouponActivity.this,MoneyActivity.class);
                intent.putExtra("coupon_name","暂无");
                intent.putExtra("coupon_id",0);
                setResult(1,intent);
                finish();
            }
        });
        tv_titile.setText("我的优惠券");

        adapter=new CouponAdapter(this,datas);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        //适配器  点击事件
        adapter.setOnItemClickLitener(new OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                adapter.setSelection(position);
                Intent intent=new Intent(CouponActivity.this,MoneyActivity.class);
                intent.putExtra("coupon_name",datas.get(position).getCoupon_name());
                intent.putExtra("coupon_money",datas.get(position).getCoupon_money());
                intent.putExtra("coupon_id",datas.get(position).getId());
                setResult(2,intent);
                finish();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        GetCoupon();

    }

    /**
     * 获取 优惠券
     */
    private void GetCoupon(){
        OkGo.<String>post(API_URL+"?request=private.gas_station.get.my.valid.coupon.list&token="+login_token+"&platform=app&money="+oil_money+"&store_id="+oil_id+"&oil_type="+oil_type)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code = jsonObject.getInt("code");
                            if (code == 0 ){
                                JSONArray data=jsonObject.getJSONArray("data");
                                if (data.length() == 0){
                                    ll_info.setVisibility(View.GONE);
                                    rl_null.setVisibility(View.VISIBLE);
                                }else {
                                    ll_info.setVisibility(View.VISIBLE);
                                    rl_null.setVisibility(View.GONE);
                                }
                                for (int i=0;i<data.length();i++){
                                    JSONObject temp=data.getJSONObject(i);
                                    CouponBean bean=new CouponBean();
                                    bean.setCoupon_name(temp.getString("coupon_name"));
                                    bean.setExpire_time(temp.getString("expire_time"));
                                    bean.setId(temp.getInt("id"));
                                    bean.setStart_time(temp.getString("start_time"));
                                    bean.setCoupon_money(temp.getInt("coupon_money"));
                                    datas.add(bean);
                                }
                                adapter.notifyDataSetChanged();
                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(CouponActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


}
