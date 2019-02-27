package com.ruoyu.pigroad.myapplication.Ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Adapter.OnItemClickLitener;
import com.ruoyu.pigroad.myapplication.Adapter.SpecialCarAdapter;
import com.ruoyu.pigroad.myapplication.Bean.SpecialBean;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.Util.Toastutil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/12
 * Email:920015363@qq.com
 */
public class SpecialCarActivity extends Activity{

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_titile)
    TextView tv_title;
    @BindView(R.id.btn_next)
    Button btn_next;


    private List<SpecialBean> datas=new ArrayList<>();
    private SpecialCarAdapter adapter;
    private String login_token;
    private ActivityManager activityManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.special_car_layout);
        ButterKnife.bind(this);
        activityManager=ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    private void init() {

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_title.setText("专车认证");

        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token=sp.getString("user_token",null);

        adapter=new SpecialCarAdapter(SpecialCarActivity.this,datas);

        adapter.setOnItemClickLitener(new OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                int state=datas.get(position).getAuth_status();
                //判断 是否 申请中 或者 已验证
                if (state == 0){
                    adapter.setSelection(position);
                }else{

                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(SpecialCarActivity.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        GetData();

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.isSelection() == true){
                    Intent intent=new Intent(SpecialCarActivity.this,InputInfoActivity.class);
                    intent.putExtra("type_id",datas.get(adapter.getSelection()).getType_id()+"");
                    startActivity(intent);
                    finish();
                }else {
                    Toastutil.show(SpecialCarActivity.this,getString(R.string.choose_car_type));
                }
            }
        });



    }

    private void GetData(){
        OkGo.<String>post(API_URL+"?request=private.car_auth.car.auth.list&token="+login_token+"&platform=app")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                JSONArray jsonArray=jsonObject.getJSONArray("data");
                                if (jsonArray.length() == 0){

                                }else {
                                    String a=jsonArray.toString();
                                    JSONArray data=new JSONArray(a);
                                    JSONArray data_arr=data.getJSONArray(0);
                                    for (int i=0;i<data_arr.length();i++){
                                        JSONObject temp =data_arr.getJSONObject(i);
                                        SpecialBean bean=new SpecialBean();
                                        bean.setType_id(temp.getInt("type_id"));
                                        bean.setAuth_status(temp.getInt("auth_status"));
                                        bean.setCar_icon(temp.getString("car_icon"));
                                        bean.setCar_name(temp.getString("car_name"));
                                        datas.add(bean);
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(SpecialCarActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}
