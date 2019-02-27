package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Bean.GunBean;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.Util.Toastutil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.baymax.android.keyboard.BaseKeyboard;
import cn.baymax.android.keyboard.KeyboardManager;
import cn.baymax.android.keyboard.NumberKeyboard;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/7
 * Email:920015363@qq.com
 */
public class OilActivity2 extends AppCompatActivity{

    @BindView(R.id.iv_oil_img)
    ImageView iv_oil_img;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_titile)
    TextView tv_title;
    @BindView(R.id.et_oil_gun)
    EditText et_oil_gun;
    @BindView(R.id.tv_oil_gun)
    TextView tv_oil_gun;
    @BindView(R.id.btn_oil_confirm)
    Button btn_oil_confirm;


    private NumberKeyboard numberKeyboard;
    private KeyboardManager keyboardManagerAbc;
    private String oil_id;
    private String oil_img;
    private String oil_title;
    private String login_token;
    private String oil_type;
    private String oil_name;
    private List<GunBean> datas=new ArrayList<>();
    private ActivityManager activityManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_layout2);
        ButterKnife.bind(this);
        activityManager= ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    private void init(){

        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token=sp.getString("user_token",null);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        oil_id=getIntent().getStringExtra("oil_id");
        oil_title=getIntent().getStringExtra("oil_title");
        tv_title.setText(oil_title);
        oil_img=getIntent().getStringExtra("oil_img");

        check_all_oil_gun();

        Glide.with(this)
                .load(oil_img)
                .into(iv_oil_img);

        et_oil_gun.setInputType(InputType.TYPE_CLASS_NUMBER);

        //键盘初始化
        keyboardManagerAbc = new KeyboardManager(this);
        initNumberKeyboard();
        keyboardManagerAbc.bindToEditor(et_oil_gun,numberKeyboard);

        //监听 油枪号
        et_oil_gun.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length()>0){

                        //判断是否有 油枪

                        for (int i=0;i<datas.size();i++){
                            String input_value=s.toString();
                            if (input_value.equals(datas.get(i).getGun_id())){
                                Log.i(LOG_TIP,"s="+s+"id="+datas.get(i).getGun_id());
                                tv_oil_gun.setText(datas.get(i).getOil_name());
                                tv_oil_gun.setVisibility(View.VISIBLE);
                                tv_oil_gun.setTextColor(Color.parseColor("#32CD32"));
                                oil_type=datas.get(i).getOil_type_id();
                                oil_name=datas.get(i).getOil_name();
                                btn_oil_confirm.setEnabled(true);
                                btn_oil_confirm.setBackgroundColor(Color.parseColor("#32CD32"));
                                break;
                            }else {
                                Log.i(LOG_TIP,"s="+s+"id="+datas.get(i).getGun_id());
                                tv_oil_gun.setTextColor(Color.RED);
                                tv_oil_gun.setVisibility(View.VISIBLE);
                                tv_oil_gun.setText("油枪号错误！");
                                btn_oil_confirm.setEnabled(false);
                                btn_oil_confirm.setBackgroundColor(Color.parseColor("#C3BAB0"));
                            }
                        }

                    }else {
                        tv_oil_gun.setVisibility(View.INVISIBLE);
                        btn_oil_confirm.setBackgroundColor(Color.parseColor("#C3BAB0"));
                        btn_oil_confirm.setEnabled(false);
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //确认按钮
        btn_oil_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(OilActivity2.this,MoneyActivity.class);
                intent.putExtra("oil_id",oil_id);
                intent.putExtra("gun_id",et_oil_gun.getText().toString());
                intent.putExtra("oil_img",oil_img);
                intent.putExtra("oil_title",oil_title);
                intent.putExtra("oil_type",oil_type);
                intent.putExtra("oil_name",oil_name);
                startActivity(intent);
            }
        });


    }

    /**
     * 键盘初始化
     */
    private void initNumberKeyboard() {
        numberKeyboard = new NumberKeyboard(this,R.xml.keyboard_number);
        numberKeyboard.setActionDoneClickListener(new NumberKeyboard.ActionDoneClickListener() {
            @Override
            public void onActionDone(CharSequence charSequence) {
                if (TextUtils.isEmpty(charSequence) || charSequence.toString().equals("0") || charSequence.toString().equals("0.0")) {
                    Toastutil.show(OilActivity2.this, "请输入油枪号");

                } else {
                    et_oil_gun.requestFocus();
                    numberKeyboard.hideKeyboard();
                }
            }
        });

        numberKeyboard.setKeyStyle(new BaseKeyboard.KeyStyle() {
            @Override
            public Drawable getKeyBackound(Keyboard.Key key) {
                if(key.iconPreview != null) {
                    return key.iconPreview;
                } else {
                    return ContextCompat.getDrawable(OilActivity2.this,R.drawable.key_number_bg);
                }
            }

            @Override
            public Float getKeyTextSize(Keyboard.Key key) {
                if(key.codes[0] == OilActivity2.this.getResources().getInteger(R.integer.action_done)) {
                    return convertSpToPixels(OilActivity2.this, 20f);
                }
                return convertSpToPixels(OilActivity2.this, 24f);
            }

            @Override
            public Integer getKeyTextColor(Keyboard.Key key) {
                if(key.codes[0] == OilActivity2.this.getResources().getInteger(R.integer.action_done)) {
                    return Color.WHITE;
                }
                return null;
            }

            @Override
            public CharSequence getKeyLabel(Keyboard.Key key) {
                return null;
            }
        });

    }

    public float convertSpToPixels(Context context, float sp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
        return px;
    }

    /**
     * 实时查询 油枪是否能使用
     */
    private void check_oil_gun(String gun){
        OkGo.<String>post(API_URL+"?request=private.gas_station.check.gun.status&platform=app&gun_id="+gun+"&store_id="+oil_id)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            Log.i(LOG_TIP,response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                JSONObject gun=jsonObject.getJSONObject("gun");
                                tv_oil_gun.setText(gun.getString("oil_name"));
                                tv_oil_gun.setVisibility(View.VISIBLE);
                                tv_oil_gun.setTextColor(Color.parseColor("#32CD32"));
                                btn_oil_confirm.setEnabled(true);
                                btn_oil_confirm.setBackgroundColor(Color.parseColor("#32CD32"));
                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(OilActivity2.this, LoginActivity.class);
                                startActivity(intent4);
                            }else {
                                tv_oil_gun.setTextColor(Color.RED);
                                tv_oil_gun.setVisibility(View.VISIBLE);
                                tv_oil_gun.setText("油枪号错误！");
                                btn_oil_confirm.setEnabled(false);
                                btn_oil_confirm.setBackgroundColor(Color.parseColor("#C3BAB0"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /**
     * 查询所有 油枪 是否能使用
     */
    private void check_all_oil_gun(){
        OkGo.<String>post(API_URL+"?request=private.gas_station.get.store.all.oil.gun&token="+login_token+"&platform=app&store_id="+oil_id)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                JSONArray data=jsonObject.getJSONArray("data");
                                for (int i=0;i<data.length();i++){
                                    JSONObject temp=data.getJSONObject(i);
                                    GunBean bean=new GunBean();
                                    bean.setGun_id(temp.getString("gun_id"));
                                    bean.setOil_name(temp.getString("oil_name"));
                                    bean.setOil_type_id(temp.getString("oil_type_id"));
                                    datas.add(bean);
                                }
                            }else if (code == 999){
                                Toastutil.show(OilActivity2.this,"登录已过期，请重新登录");
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(OilActivity2.this, LoginActivity.class);
                                startActivity(intent4);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}
