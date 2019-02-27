package com.ruoyu.pigroad.myapplication.Login;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;

import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.iflytek.cloud.thirdparty.S;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.MainActivity;
import com.ruoyu.pigroad.myapplication.MainActivity2;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.Toastutil;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;

/**
 * Created by PIGROAD on 2018/9/4
 * Email:920015363@qq.com
 */
public class LoginActivity extends FragmentActivity implements View.OnClickListener{
    @BindView(R.id.et_phone)
    EditText et_phone;
    @BindView(R.id.et_code)
    EditText et_code;
    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.btn_login)
    Button btn_login;
    @BindView(R.id.tv_server_read)
    TextView tv_server_read;
    @BindView(R.id.btn_get_code)
    Button btn_get_code;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        ButterKnife.bind(this);
        this.init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 控件初始化
     */
    private void init(){

        String server_read = "点击开始，即表示已阅读并同意<font color='#ff0000'>《用车服务条款》</font>";
        tv_server_read.setText(Html.fromHtml(server_read));

        //设置只能输入数字
        et_phone.setInputType(InputType.TYPE_CLASS_NUMBER);
        et_code.setInputType(InputType.TYPE_CLASS_NUMBER);

        //监听edittext输入
        et_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11){
                    btn_get_code.setBackgroundColor(Color.RED);
                    btn_get_code.setEnabled(true);
                    et_code.setEnabled(true);
                }else {
                    btn_get_code.setBackgroundColor(getResources().getColor(R.color.btn_un_active));
                    btn_get_code.setEnabled(false);
                    et_code.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() ==6){
                    btn_login.setEnabled(true);
                    btn_login.setBackgroundColor(Color.RED);
                }else {
                    btn_login.setEnabled(false);
                    btn_login.setBackgroundColor(getResources().getColor(R.color.btn_un_active));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_login.setOnClickListener(this);
        btn_get_code.setOnClickListener(this);
        iv_close.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btn_get_code:
                String number=et_phone.getText().toString();
                if (isMobileNO(number)){
                    GetCode(et_phone.getText().toString());
                }else {
                    Toastutil.show(this,"你输入的手机有误，请重新输入");
                }
                break;
            case R.id.btn_login:
                Login(et_phone.getText().toString(),et_code.getText().toString());
                break;
            case R.id.iv_close:
                finish();
                break;
        }
    }

    /**
     * 判断是否输入手机
     */
    public static boolean isMobileNO(String mobiles) {
        String telRegex = "[1][3586]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles)){
            return false;
        }
        else return mobiles.matches(telRegex);
    }

    /**
     * 验证码倒计时方法
     */
    public void setcodetime(){
        CountDownTimer timer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btn_get_code.setEnabled(false);
                btn_get_code.setText("已发送(" + millisUntilFinished / 1000 + ")");
            }

            @Override
            public void onFinish() {
                btn_get_code.setEnabled(true);
                btn_get_code.setText("重新获取");
            }
        }.start();
    }

    /**
     * 获取验证码
     */
    private void GetCode(String phone){
        OkGo.<String>post(API_URL+"?request=public.user_auth.login.sms.code&platform=app&phone="+phone)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code=jsonObject.getInt("code");
                            String msg=jsonObject.getString("msg");
                            if (code == 0){
                                setcodetime();
                                Toastutil.show(LoginActivity.this,msg);
                            }else {
                                Toastutil.show(LoginActivity.this,msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 登录验证
     */
    private void Login(String phone,String code){
        OkGo.<String>post(API_URL+"?request=public.user_auth.user.login&platform=app&phone="+phone+"&smscode="+code)
        .execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JSONObject jsonObject=new JSONObject(response.body());
                    int code =jsonObject.getInt("code");
                    if (code == 0){
                        Toastutil.show(LoginActivity.this,"登录成功");
                        String token=jsonObject.getString("data");
                        //获取sharedPreferences对象
                        SharedPreferences sharedPreferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                        //获取editor对象
                        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
                        //存储键值对
                        editor.putString("user_token", token);
                        //提交
                        editor.commit();//提交修改
                        //进入主界面
                        startActivity(new Intent(LoginActivity.this,MainActivity2.class));
                        finish();
                    }else {
                        String msg=jsonObject.getString("msg");
                        Toastutil.show(LoginActivity.this,msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }





}
