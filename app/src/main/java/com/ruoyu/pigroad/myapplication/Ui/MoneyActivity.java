package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.thirdparty.S;
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
 * Created by PIGROAD on 2018/9/7
 * Email:920015363@qq.com
 */
public class MoneyActivity extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_titile)
    TextView tv_titile;
    @BindView(R.id.tv_oil_price)
    TextView tv_oil_price;
    @BindView(R.id.tv_litre)
    TextView tv_litre;
    @BindView(R.id.tv_discountMoney)
    TextView tv_discountMoney;
    @BindView(R.id.et_oil_money)
    EditText et_oil_money;
    @BindView(R.id.ll_coupon)
    LinearLayout ll_coupon;
    @BindView(R.id.ll_bill)
    LinearLayout ll_bill;
    @BindView(R.id.btn_confirm)
    Button btn_confirm;
    @BindView(R.id.tv_bill_name)
    TextView tv_bill_name;
    @BindView(R.id.tv_coupon_name)
    TextView tv_coupon_name;

    private String oil_id;
    private String gun_id;
    private String login_token;
    private String oil_type;
    private String oil_name;
    private int coupon_money = 0;
    int default_coupon_money = 0;
    private int money = 0;
    private ActivityManager activityManager;
    private String coupon_name="";
    private String need_payment = "";
    private String discountName = "";
    private String discountType = "";
    private String use_oil_price = "";
    private int bill_id = 0;
    private int coupon_id = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.money_layout);
        ButterKnife.bind(this);
        activityManager = ActivityManager.getInstance();
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


        //获取焦点
        et_oil_money.setFocusable(true);
        et_oil_money.setFocusableInTouchMode(true);
        et_oil_money.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);

        tv_titile.setText(R.string.money_activity_title);
        oil_id = getIntent().getStringExtra("oil_id");
        gun_id = getIntent().getStringExtra("gun_id");
        oil_type = getIntent().getStringExtra("oil_type");
        oil_name = getIntent().getStringExtra("oil_name");

        et_oil_money.setInputType(InputType.TYPE_CLASS_NUMBER);

        et_oil_money.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 0) {
                    CheckMoney(s.toString());
                    Log.i(LOG_TIP,"default_coupon_money:"+default_coupon_money);
                } else {
                    tv_discountMoney.setVisibility(View.INVISIBLE);
                    tv_oil_price.setVisibility(View.INVISIBLE);
                    tv_litre.setVisibility(View.INVISIBLE);
                    tv_oil_price.setText("");
                    tv_litre.setText("");
                    tv_discountMoney.setText("");

                    btn_confirm.setEnabled(false);
                    btn_confirm.setBackgroundColor(Color.parseColor("#b9afa9"));

                    btn_confirm.setText("请输入金额");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    CheckMoney(s.toString());
                    money = Integer.parseInt(et_oil_money.getText().toString()) - coupon_money - default_coupon_money;
                } else {

                    tv_discountMoney.setVisibility(View.INVISIBLE);
                    tv_oil_price.setVisibility(View.INVISIBLE);
                    tv_litre.setVisibility(View.INVISIBLE);
                    tv_oil_price.setText("");
                    tv_litre.setText("");
                    tv_discountMoney.setText("");

                    btn_confirm.setEnabled(false);
                    btn_confirm.setBackgroundColor(Color.parseColor("#b9afa9"));

                    btn_confirm.setText("请输入金额");

                }
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        //选择优惠价
        ll_coupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String money = et_oil_money.getText().toString();
                if (money.equals("")) {
                    Toastutil.show(MoneyActivity.this, "请输入金额");
                } else {
                    Intent intent = new Intent(MoneyActivity.this, CouponActivity.class);
                    intent.putExtra("oil_money", et_oil_money.getText().toString());
                    intent.putExtra("oil_type", getIntent().getStringExtra("oil_type"));
                    intent.putExtra("oil_id", oil_id);
                    startActivityForResult(intent, 2);
                }
            }
        });

        //选择 发票
        ll_bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoneyActivity.this, BillActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        //支付按钮点击事件
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmPay();
            }
        });
    }

    /**
     * 实时查询 金额
     */
    private void CheckMoney(final String money) {
        OkGo.<String>post(API_URL + "?request=private.gas_station.get_my_discount_oil_price&platform=app&store_id=" + oil_id + "&gun_id=" + gun_id + "&money=" + money + "&token=" + login_token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            Log.i(LOG_TIP, response.body());
                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                JSONObject data = jsonObject.getJSONObject("data");

                                String country_oil_price = data.getString("country_oil_price");
                                String litre = data.getString("litre");
                                String discountMoney = data.getString("discountMoney");
                                need_payment=data.getString("need_payment");
                                discountName=data.getString("discountName");
                                discountType=data.getString("discountType");
                                use_oil_price=data.getString("use_oil_price");

                                //可视化
                                tv_discountMoney.setVisibility(View.VISIBLE);
                                tv_oil_price.setVisibility(View.VISIBLE);
                                tv_litre.setVisibility(View.VISIBLE);

                                tv_oil_price.setText("约" + litre + "升");
                                tv_litre.setText("国家价" + country_oil_price + "元/升，已优惠");
                                tv_discountMoney.setText("￥" + discountMoney);

                                //可支付下一步
                                btn_confirm.setEnabled(true);
                                btn_confirm.setBackgroundColor(Color.parseColor("#32CD32"));

                                try {
                                    int c_price = Integer.parseInt(et_oil_money.getText().toString()) - coupon_money ;

                                    btn_confirm.setText("确认支付" + c_price + "元");

                                } catch (Exception e) {

                                }

//                                int perfect_coupon_status = data.getInt("perfect_coupon_status");
//
//                                if (perfect_coupon_status == 0) {
//                                    //判断 perfectCoupon
//                                    JSONObject pcJson = data.getJSONObject("perfect_coupon");
//
//                                    if (coupon_name.equals("")){
//                                        tv_coupon_name.setText(pcJson.getString("coupon_name"));
//                                    }
//
//                                    default_coupon_money = pcJson.getInt("coupon_money");
//
//                                    try {
//                                        int c_price = Integer.parseInt(et_oil_money.getText().toString()) - coupon_money -default_coupon_money;
//
//                                        btn_confirm.setText("确认支付" + c_price + "元");
//
//
//                                    }catch (Exception e){
//
//                                    }
//
//                                } else {
//
//                                    default_coupon_money=0;
//                                    if (coupon_name.equals("")){
//
//                                        tv_coupon_name.setText("暂无");
//                                    }
//
//                                    try {
//                                        int c_price = Integer.parseInt(et_oil_money.getText().toString()) - coupon_money -default_coupon_money;
//
//                                        btn_confirm.setText("确认支付" + c_price + "元");
//                                    }catch (Exception e){
//
//                                    }
//
//                                }


                            } else if (code == 999) {
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4 = new Intent(MoneyActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            } else {

                                tv_discountMoney.setVisibility(View.INVISIBLE);
                                tv_oil_price.setVisibility(View.INVISIBLE);
                                tv_litre.setVisibility(View.INVISIBLE);

                                btn_confirm.setEnabled(false);
                                btn_confirm.setBackgroundColor(Color.parseColor("#b9afa9"));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (1 == requestCode) {
                tv_bill_name.setText(data.getStringExtra("bill_name"));
                bill_id=data.getIntExtra("bill_id",0);
            } else if (2 == requestCode) {
                coupon_name=data.getStringExtra("coupon_name");
                tv_coupon_name.setText(coupon_name);
                coupon_money = data.getIntExtra("coupon_money", 0);
                money = Integer.parseInt(et_oil_money.getText().toString()) - coupon_money;
                coupon_id=data.getIntExtra("coupon_id",0);
                btn_confirm.setText("确认支付" + money + "元");
                et_oil_money.setEnabled(false);
            }
        }
    }

    /**
     *点击确定，进入支付页面/将数据添加到临时表
     */
    private void confirmPay(){
        OkGo.<String>post(API_URL+"?request=private.gas_station.action.submit.order&platform=app&store_id="+oil_id+"&gun_id="+gun_id+"&token="+login_token+"&money="+et_oil_money.getText()+"&discount_money="+coupon_money+"&need_payment="+need_payment+"&discountName="
            +discountName+"&discountType="+discountType+"&use_oil_price="+use_oil_price+"&tax_id="+bill_id+"&coupon_id="+coupon_id)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObjec=new JSONObject(response.body());
                            int code =jsonObjec.getInt("code");
                            if (code == 0){
                                int id=jsonObjec.getInt("id");
                                Intent intent = new Intent(MoneyActivity.this, PayActivity.class);
                                intent.putExtra("oil_title", getIntent().getStringExtra("oil_title"));
                                intent.putExtra("oil_img", getIntent().getStringExtra("oil_img"));
                                intent.putExtra("oil_name", getIntent().getStringExtra("oil_name"));
                                intent.putExtra("id", id);
                                intent.putExtra("money", Integer.parseInt(et_oil_money.getText().toString()) - coupon_money);
                                startActivity(intent);
                            }else {
                                Toastutil.show(MoneyActivity.this,jsonObjec.getString("msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
