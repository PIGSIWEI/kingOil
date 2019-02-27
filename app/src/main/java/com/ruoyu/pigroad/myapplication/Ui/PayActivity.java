package com.ruoyu.pigroad.myapplication.Ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.iflytek.cloud.thirdparty.S;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.AliPay.AuthResult;
import com.ruoyu.pigroad.myapplication.AliPay.OrderInfoUtil2_0;
import com.ruoyu.pigroad.myapplication.AliPay.PayResult;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.Util.Toastutil;
import com.ruoyu.pigroad.myapplication.Widget.DetectLoginActivity;
import com.ruoyu.pigroad.myapplication.Widget.ImageSaveUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/6
 * Email:920015363@qq.com
 */
public class PayActivity extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.iv_oil_img)
    ImageView iv_oil_img;
    @BindView(R.id.tv_titile)
    TextView tv_title;
    @BindView(R.id.tv_oil_type)
    TextView tv_oil_type;
    @BindView(R.id.tv_oil_name)
    TextView tv_oil_name;
    @BindView(R.id.tv_oil_money)
    TextView tv_oil_money;
    @BindView(R.id.ll_ali)
    RelativeLayout ll_ali;
    @BindView(R.id.ll_wx)
    RelativeLayout ll_wx;
    @BindView(R.id.rb_wx)
    ImageView rb_wx;
    @BindView(R.id.rb_ali)
    ImageView rb_ali;
    @BindView(R.id.btn_pay)
    Button btn_pay;
    @BindView(R.id.btn_face)
    Button btn_face;
    private String pay_method="";
    private int id=0;

    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;

    private ActivityManager activityManager;
    private String login_token,user_id;

    private String notify_url="";

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        Toast.makeText(PayActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                        alipaySucces(payResult.getResult());
                        Log.i("pppppppppppppppp",payResult.getResult());
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        Toast.makeText(PayActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                        Log.i("pppppppppppppppp",payResult.toString());
                    }
                    break;
                }
                case SDK_AUTH_FLAG: {
                    @SuppressWarnings("unchecked")
                    AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
                    String resultStatus = authResult.getResultStatus();

                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
                        // 传入，则支付账户为该授权账户
                        Toast.makeText(PayActivity.this,
                                "授权成功\n" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        // 其他状态值则为授权失败
                        Toast.makeText(PayActivity.this,
                                "授权失败" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT).show();

                    }
                    break;
                }
                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_layout);
        ButterKnife.bind(this);
        activityManager = ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    private void init() {
        id=getIntent().getIntExtra("id",0);
        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);
        user_id = sp.getString("user_id", null);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Glide.with(this).load(getIntent().getStringExtra("oil_img")).into(iv_oil_img);
        tv_title.setText(getIntent().getStringExtra("oil_title"));
        tv_oil_name.setText(getIntent().getStringExtra("oil_title"));
        tv_oil_type.setText(getIntent().getStringExtra("oil_name"));
        tv_oil_money.setText("￥" + getIntent().getIntExtra("money", 0));

        ll_ali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb_ali.setBackgroundResource(R.drawable.icon_check);
                rb_wx.setBackgroundResource(R.drawable.icon_un_check);
                pay_method="alipay";
            }
        });

        ll_wx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb_wx.setBackgroundResource(R.drawable.icon_check);
                rb_ali.setBackgroundResource(R.drawable.icon_un_check);
                pay_method="wx";
            }
        });

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pay_method.equals("")){
                    Toastutil.show(PayActivity.this,"请选择付款方式！");
                }else {
                    payConfirm();
                }
            }
        });

        //人脸支付
        btn_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PayActivity.this, DetectLoginActivity.class);
                intent.putExtra("user_id",user_id);
                startActivityForResult(intent,0);
            }
        });

    }

    /**
     * 进行支付（已选择微信支付或支付宝支付）-返回订单金额、秘钥等相关数据
     */
    private void payConfirm(){
        OkGo.<String>post(API_URL+"?request=private.pay_temp.action_pay_gas_station&platform=app&token="+login_token+"&id="+id+"&pay_method="+pay_method)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code=jsonObject.getInt("code");
                            if (code == 0){
                                JSONObject data=jsonObject.getJSONObject("data");
                                if (pay_method.equals("alipay")){
                                    notify_url=data.getString("notify_url");
                                    alipay(data.getString("app_id"),data.getString("rsa2_private"),null,data.getString("orderid"),data.getString("money"),data.getString("subject"),data.getString("body"));
                                }else {

                                }
                            }else {
                                Toastutil.show(PayActivity.this,jsonObject.getString("msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void alipay(String APPID,String RSA2_PRIVATE,String RSA_PRIVATE,String trade_id,String money,String title,String body){
        if (TextUtils.isEmpty(APPID) || (TextUtils.isEmpty(RSA2_PRIVATE) && TextUtils.isEmpty(RSA_PRIVATE))) {
            new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置APPID | RSA_PRIVATE")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            finish();
                        }
                    }).show();
            return;
        }

        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo的获取必须来自服务端；
         */
        boolean rsa2 = (RSA2_PRIVATE.length() > 0);
        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID, rsa2,trade_id,money,title,body);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);

        String privateKey = rsa2 ? RSA2_PRIVATE : RSA_PRIVATE;
        String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);
        final String orderInfo = orderParam + "&" + sign;

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(PayActivity.this);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.i("msp", result.toString());

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }


    /**
     *支付完成后立马调用的接口，插入数据到正式表中
     */
    private void alipaySucces(final String result){
        OkGo.<String>post(notify_url)
                .params("data",result)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                activityManager.exit();
                                Intent intent=new Intent(PayActivity.this,PayFinishActivity.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == 2){
            Intent intent=new Intent(PayActivity.this,FaceCarActivity.class);
            intent.putExtra("id",id);
            startActivity(intent);
            activityManager.exit();
        }else {
            show();
        }
    }

    /**
     * 人脸认证失败弹窗
     */
    private void show(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("人脸认证不通过！请重试");
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();

    }

}