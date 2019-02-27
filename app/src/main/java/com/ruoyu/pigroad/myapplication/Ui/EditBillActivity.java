package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
public class EditBillActivity extends AppCompatActivity{

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
    @BindView(R.id.btn_delete)
    Button btn_delete;
    @BindView(R.id.tv_select)
    TextView tv_select;

    private String login_token;
    private String id;
    private String bill_title;
    private String bill_number;
    private String is_select;

    private ActivityManager activityManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_bill_layout);
        ButterKnife.bind(this);
        activityManager= ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    private void init() {

        //token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token=sp.getString("user_token",null);

        id=getIntent().getStringExtra("bill_id");
        bill_title=getIntent().getStringExtra("bill_title");
        bill_number=getIntent().getStringExtra("bill_number");
        is_select=getIntent().getStringExtra("is_select");

        et_bill_title.setText(bill_title);
        et_number.setText(bill_number);
        if (is_select.equals("1")){
            cb_select.setChecked(true);
        }else {
            cb_select.setChecked(false);
        }

        tv_et_listener.setText(et_bill_title.getText().toString().trim().length()+"/25");

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_title.setText(R.string.address_edit);

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

        //保存发票操作
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb_select.isChecked()){
                    ChangeBill(1);
                }else {
                    ChangeBill(2);
                }
            }
        });

        //删除 发票
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(EditBillActivity.this);
                builder.setMessage(R.string.confirm_deletion);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteBill();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
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


    //编辑 发票 保存 操作
    private void ChangeBill(int is_select){
        OkGo.<String>post(API_URL+"?request=private.invoice.action.edit.invoice&token="+login_token+"&platform=app&id="+id+"&company_name="+et_bill_title.getText()+"&tax_number="+et_number.getText().toString()+"&is_select="+is_select)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            String msg=jsonObject.getString("msg");
                            if (code == 0){
                                Toastutil.show(EditBillActivity.this,msg);
                                finish();
                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(EditBillActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            }else {
                                Toastutil.show(EditBillActivity.this,msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //删除发票操作
    private void DeleteBill(){
        OkGo.<String>post(API_URL+"?request=private.invoice.action.del.invoice&platform=app&id="+id+"&token="+login_token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            String msg=jsonObject.getString("msg");
                            if (code ==0){
                                Toastutil.show(EditBillActivity.this,msg);
                                finish();
                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(EditBillActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            }else {
                                Toastutil.show(EditBillActivity.this,msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

}
