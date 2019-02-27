package com.ruoyu.pigroad.myapplication.Ui;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.parkingwang.keyboard.KeyboardInputController;
import com.parkingwang.keyboard.PopupKeyboard;
import com.parkingwang.keyboard.view.InputView;
import com.parkingwang.keyboard.view.KeyboardView;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.Util.Toastutil;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnBindViewListener;
import com.ywp.addresspickerlib.AddressPickerView;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.baymax.android.keyboard.BaseKeyboard;
import cn.baymax.android.keyboard.KeyboardManager;
import cn.baymax.android.keyboard.NumberKeyboard;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/12
 * Email:920015363@qq.com
 */
public class InputInfoActivity extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_titile)
    TextView tv_title;
    @BindView(R.id.tv_phone_number)
    TextView tv_phone_number;
    @BindView(R.id.tv_car_type)
    TextView tv_car_type;
    @BindView(R.id.tv_address)
    TextView tv_address;
    @BindView(R.id.ll_city)
    LinearLayout ll_city;
    @BindView(R.id.ll_car_number)
    LinearLayout ll_car_number;
    @BindView(R.id.btn_next)
    Button btn_next;
    @BindView(R.id.input_view)
    InputView mInputView;

    private String type_id;
    private String login_token;
    private NumberKeyboard numberKeyboard;
    private KeyboardManager keyboardManagerAbc;
    private ActivityManager activityManager;
    private PopupKeyboard mPopupKeyboard;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_info_layout);
        ButterKnife.bind(this);
        activityManager = ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    private void init() {

        type_id = getIntent().getStringExtra("type_id");

        //token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_title.setText("填写信息");

        GetData();

        ll_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TDialog.Builder(getSupportFragmentManager())
                        .setLayoutRes(R.layout.address_dialog)
                        .setHeight(800)//设置弹窗固定高度
                        .setGravity(Gravity.BOTTOM)
                        .setScreenWidthAspect(InputInfoActivity.this, 1f) //动态设置弹窗宽度为屏幕宽度百分比(取值0-1f)
                        .setScreenHeightAspect(InputInfoActivity.this, 0.6f)//设置弹窗高度为屏幕高度百分比(取值0-1f)
                        .setCancelableOutside(true)
                        .setOnBindViewListener(new OnBindViewListener() {
                            @Override
                            public void bindView(final BindViewHolder viewHolder) {
                                final AddressPickerView apvAddress = viewHolder.getView(R.id.apvAddress);
                                apvAddress.setOnAddressPickerSure(new AddressPickerView.OnAddressPickerSureListener() {
                                    @Override
                                    public void onSureClick(String address, String provinceCode, String cityCode, String districtCode) {
                                        tv_address.setText(address);
                                        new Thread() {
                                            public void run() {
                                                try {
                                                    Instrumentation inst = new Instrumentation();
                                                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                                                } catch (Exception e) {
                                                    Log.e(LOG_TIP,
                                                            e.toString());
                                                }
                                            }
                                        }.start();
                                    }
                                });
                            }
                        })
                        .create()
                        .show();
            }
        });

        // Init Views

        // 创建弹出键盘
        mPopupKeyboard = new PopupKeyboard(this);
        // 隐藏确定按钮
        mPopupKeyboard.getKeyboardEngine().setHideOKKey(true);
        // 弹出键盘内部包含一个KeyboardView，在此绑定输入两者关联。
        mPopupKeyboard.attach(mInputView, this);


//        et_car_number.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                Log.i("字符变换后", "afterTextChanged");
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                Log.i("字符变换前", s + "-" + start + "-" + count + "-" + after);
//                if (s.length() == 7) {
//
//                }
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.i("字符变换中", s + "-" + "-" + start + "-" + before + "-" + count);
//            }
//        });

//        ll_car_number.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                et_car_number.setFocusable(true);
//                et_car_number.requestFocus();
//                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//            }
//        });
//
        //下一步
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInputView.getNumber().equals("")) {
                    Toastutil.show(InputInfoActivity.this, "车牌号不能为空");
                } else {
                    SendData();
//                    Toastutil.show(InputInfoActivity.this, mInputView.getNumber());
                }
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPopupKeyboard.isShown()){
            mPopupKeyboard.dismiss(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    private void GetData() {
        OkGo.<String>post(API_URL + "?request=private.car_auth.car.auth.info&token=" + login_token + "&platform=app&type_id=" + type_id)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP, response.body());
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                JSONObject data = jsonObject.getJSONObject("data");
                                tv_address.setText(data.getString("place_name"));
                                tv_car_type.setText(data.getString("car_name"));
                                tv_phone_number.setText(data.getString("phone"));
                            } else if (code == 999) {
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4 = new Intent(InputInfoActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 请求操作
     */
    private void SendData() {
        OkGo.<String>post(API_URL + "?request=private.car_auth.car.auth.apply&token=" + login_token + "&platform=app&type_id=" + type_id + "&car_no=" + mInputView.getNumber() + "&phone=" + tv_phone_number.getText() + "&place_name=" + tv_address.getText())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP, response.body());
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            int code = jsonObject.getInt("code");
                            String msg = jsonObject.getString("msg");
                            if (code == 0) {
                                Toastutil.show(InputInfoActivity.this, msg);
                                Intent intent = new Intent(InputInfoActivity.this, UploadActivity.class);
                                intent.putExtra("id",jsonObject.getString("id"));
                                startActivity(intent);
                                finish();
                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(InputInfoActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            }else{
                                Toastutil.show(InputInfoActivity.this, msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 键盘初始化
     */
//    private void initNumberKeyboard() {
//        numberKeyboard = new NumberKeyboard(this,R.xml.province_abbreviation);
//        numberKeyboard.setActionDoneClickListener(new NumberKeyboard.ActionDoneClickListener() {
//            @Override
//            public void onActionDone(CharSequence charSequence) {
//                if (TextUtils.isEmpty(charSequence) || charSequence.toString().equals("0") || charSequence.toString().equals("0.0")) {
//                    Toastutil.show(InputInfoActivity.this, "请输入车牌号");
//
//                } else {
//                    et_car_number.requestFocus();
//                    numberKeyboard.hideKeyboard();
//                }
//            }
//        });
//
//        numberKeyboard.setKeyStyle(new BaseKeyboard.KeyStyle() {
//            @Override
//            public Drawable getKeyBackound(Keyboard.Key key) {
//                if(key.iconPreview != null) {
//                    return key.iconPreview;
//                } else {
//                    return ContextCompat.getDrawable(InputInfoActivity.this,R.drawable.key_number_bg);
//                }
//            }
//
//            @Override
//            public Float getKeyTextSize(Keyboard.Key key) {
//                if(key.codes[0] == InputInfoActivity.this.getResources().getInteger(R.integer.action_done)) {
//                    return convertSpToPixels(InputInfoActivity.this, 20f);
//                }
//                return convertSpToPixels(InputInfoActivity.this, 24f);
//            }
//
//            @Override
//            public Integer getKeyTextColor(Keyboard.Key key) {
//                if(key.codes[0] == InputInfoActivity.this.getResources().getInteger(R.integer.action_done)) {
//                    return Color.WHITE;
//                }
//                return null;
//            }
//
//            @Override
//            public CharSequence getKeyLabel(Keyboard.Key key) {
//                return null;
//            }
//        });
//
//    }
    public float convertSpToPixels(Context context, float sp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
        return px;
    }


}
