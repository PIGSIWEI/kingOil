package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.Util.Toastutil;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/10
 * Email:920015363@qq.com
 */
public class AddBillActivity extends AppCompatActivity{

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_titile)
    TextView tv_title;
    @BindView(R.id.et_bill_title)
    EditText et_bill_title;
    @BindView(R.id.tv_et_listener)
    TextView tv_et_listener;
    @BindView(R.id.btn_save)
    Button btn_save;
    @BindView(R.id.et_number)
    EditText et_number;
    @BindView(R.id.cb_select)
    CheckBox cb_select;
    @BindView(R.id.tv_select)
    TextView tv_select;

    private String login_token;
    private ActivityManager activityManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bill_layout);
        ButterKnife.bind(this);
        activityManager=ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    /**
     * 初始化
     */
    private void init(){

        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token=sp.getString("user_token",null);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_title.setText(R.string.add_bill_title);

        //监听发票抬头的个数
        et_bill_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tv_et_listener.setText(s.length()+"/25");
                if (s.length()>0){
                    btn_save.setBackgroundColor(Color.parseColor("#32CD32"));
                    btn_save.setEnabled(true);
                }else {
                    btn_save.setBackgroundColor(Color.parseColor("#b9afa9"));
                    btn_save.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //保存 发票信息
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb_select.isChecked()){
                    AddBill(1);
                }else {
                    AddBill(0);
                }
            }
        });

        tv_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb_select.isChecked()){
                    cb_select.setChecked(false);
                }else {
                    cb_select.setChecked(true);
                }
            }
        });


    }

    /**
     * 添加发票信息
     */
    private void AddBill(int is_select){
        String company_name=et_bill_title.getText().toString();
        String tax_number=et_number.getText().toString();
        OkGo.<String>post(API_URL+"?request=private.invoice.add.invoice&platform=app&company_name="+company_name+"&tax_number="+tax_number+"&is_select="+is_select+"&token="+login_token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            String msg=jsonObject.getString("msg");
                            if (code == 0){
                                Toastutil.show(AddBillActivity.this,msg);
                                finish();
                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(AddBillActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            }else {
                                Toastutil.show(AddBillActivity.this,msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
