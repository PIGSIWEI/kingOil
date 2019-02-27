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
import com.ruoyu.pigroad.myapplication.Adapter.RecordAdapter;
import com.ruoyu.pigroad.myapplication.Bean.CouponBean;
import com.ruoyu.pigroad.myapplication.Bean.RecordBean;
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
 * Created by PIGROAD on 2018/9/11
 * Email:920015363@qq.com
 */
public class RecordActivity extends AppCompatActivity{

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_titile)
    TextView tv_title;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.tv_count)
    TextView tv_count;
    @BindView(R.id.tv_pay_money)
    TextView tv_pay_money;
    @BindView(R.id.tv_discount_money)
    TextView tv_discount_money;
    @BindView(R.id.ll_main)
    LinearLayout ll_main;
    @BindView(R.id.rl_null)
    RelativeLayout rl_null;

    private String login_token;
    private List<RecordBean> datas=new ArrayList<>();
    private RecordAdapter adapter;
    private ActivityManager activityManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_layout);
        ButterKnife.bind(this);
        activityManager= ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {

        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token=sp.getString("user_token",null);

        tv_title.setText("消费记录");

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adapter=new RecordAdapter(this,datas);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        CheckData();
    }


    /**
     * 查询数据
     */
    private void CheckData(){
        OkGo.<String>post(API_URL+"?request=private.order.get.pay.records.list&token="+login_token+"&platform=app&page=1&page_size=24")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){

                                JSONArray data=jsonObject.getJSONArray("data");
                                //通过data来判断是否空数组
                                if (data.length() ==0){
                                    ll_main.setVisibility(View.GONE);
                                    rl_null.setVisibility(View.VISIBLE);
                                }else {
                                    ll_main.setVisibility(View.VISIBLE);
                                    rl_null.setVisibility(View.GONE);
                                    for (int i=0;i<data.length();i++){
                                        JSONObject temp=data.getJSONObject(i);
                                        RecordBean bean=new RecordBean();
                                        bean.setDiscount_money(temp.getString("discount_money"));
                                        bean.setName(temp.getString("name"));
                                        bean.setPay_money(temp.getString("pay_money"));
                                        bean.setPay_time(temp.getString("pay_time"));
                                        datas.add(bean);
                                    }

                                    JSONObject total=jsonObject.getJSONObject("total");
                                    tv_count.setText(total.getString("count")+"笔");
                                    tv_discount_money.setText(total.getString("discount_money"));
                                    tv_pay_money.setText(total.getString("pay_money"));

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
                                Intent intent4=new Intent(RecordActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
