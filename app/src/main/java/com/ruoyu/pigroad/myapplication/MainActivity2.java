package com.ruoyu.pigroad.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.facesdk.FaceTracker;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Config.APIService;
import com.ruoyu.pigroad.myapplication.Config.Config;
import com.ruoyu.pigroad.myapplication.Fragment.MainMineFragment;
import com.ruoyu.pigroad.myapplication.Fragment.MainOilFragment;
import com.ruoyu.pigroad.myapplication.Ui.CardActivity;
import com.ruoyu.pigroad.myapplication.Util.AccessToken;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.Util.FaceError;
import com.ruoyu.pigroad.myapplication.Util.OnResultListener;
import com.ruoyu.pigroad.myapplication.facesdk.FaceEnvironment;
import com.ruoyu.pigroad.myapplication.facesdk.FaceSDKManager;
import com.timmy.tdialog.TDialog;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;

/**
 * Created by PIGROAD on 2018/9/18
 * Email:920015363@qq.com
 */
public class MainActivity2 extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.content)
    FrameLayout content;
    @BindView(R.id.ll_oil)
    LinearLayout ll_oil;
    @BindView(R.id.ll_mine)
    LinearLayout ll_mine;
    @BindView(R.id.iv_mine)
    ImageView iv_mine;
    @BindView(R.id.iv_oil)
    ImageView iv_oil;
    @BindView(R.id.tv_oil)
    TextView tv_oil;
    @BindView(R.id.tv_mine)
    TextView tv_mine;
    @BindView(R.id.ll_yjjy)
    LinearLayout ll_yjjy;


    private FragmentManager fragmentManager;
    private ActivityManager activityManager;

    private MainMineFragment mainMineFragment;
    private MainOilFragment mainOilFragment;

    private String login_token;

    private int mDefaultColor= Color.parseColor("#969696");
    private int mActiveColor= Color.parseColor("#32CD32");

    private List<View> bottomTabs=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout2);
        ButterKnife.bind(this);
        activityManager= ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    private void init() {
        setHalfTransparent();

        fragmentManager = getSupportFragmentManager();

        bottomTabs.add(ll_mine);
        bottomTabs.add(ll_oil);

        setSelectTab(0);

        ll_mine.setOnClickListener(this);
        ll_oil.setOnClickListener(this);
        ll_yjjy.setOnClickListener(this);

        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);

        checkFace();
    }


    /**
     * Fragment选择方法
     */
    public void setSelectTab(int index) {
        clearSelection();
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);

        switch (index) {
            case 0:
                tv_oil.setTextColor(mActiveColor);
                iv_oil.setBackgroundResource(R.drawable.ac_main_icon_oil);

                if (mainOilFragment == null) {
                    mainOilFragment = new MainOilFragment();
                    transaction.add(R.id.content, mainOilFragment);

                } else {
                    transaction.show(mainOilFragment);
                }
                break;
            case 1:
                tv_mine.setTextColor(mActiveColor);
                iv_mine.setBackgroundResource(R.drawable.ac_main_icon_mine);

                if (mainMineFragment == null) {
                    mainMineFragment = new MainMineFragment();
                    transaction.add(R.id.content, mainMineFragment);
                } else {
                    transaction.show(mainMineFragment);
                }
                break;


                default:
                break;
        }
        transaction.commit();
    }

    /**
     * 开启fragment事物
     */
    private void clearSelection() {
        tv_mine.setTextColor(mDefaultColor);
        tv_oil.setTextColor(mDefaultColor);
        iv_mine.setBackgroundResource(R.drawable.main_icon_mine);
        iv_oil.setBackgroundResource(R.drawable.main_icon_oil);
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (mainMineFragment != null) {
            transaction.hide(mainMineFragment);
        }
        if (mainOilFragment != null) {
            transaction.hide(mainOilFragment);
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_oil:
                setSelectTab(0);
                break;
            case R.id.ll_mine:
                setSelectTab(1);
                break;
            case R.id.ll_yjjy:

                final ZLoadingDialog dialog = new ZLoadingDialog(MainActivity2.this);
                dialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                        .setLoadingColor(Color.parseColor("#32cd32"))//颜色
                        .setHintText("加载中")
                        .setCanceledOnTouchOutside(false)
                        .setHintTextSize(16f)
                        .setCancelable(false)
                        .show();
                new Handler().postDelayed(new Runnable(){
                    public void run(){

                        //execute the task
                        dialog.dismiss();

                        //获取附件油站
                        startActivity(new Intent(MainActivity2.this, CardActivity.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                    }
                },2000);
                break;
        }
    }

    /**
     * 半透明状态栏
     */
    protected void setHalfTransparent() {

        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        } else if (Build.VERSION.SDK_INT >= 19) {//19表示4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * SDK初始化
     */
    private void initSDK(String groupID,String apiKey,String secretKey){
        initLib();
        APIService.getInstance().init(this);
        APIService.getInstance().setGroupId(groupID);
        // 用ak，sk获取token, 调用在线api，如：注册、识别等。为了ak、sk安全，建议放您的服务器，
        APIService.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                Log.i("wtf", "AccessToken->" + result.getAccessToken());
            }

            @Override
            public void onError(FaceError error) {
                Log.e("xx", "AccessTokenError:" + error);
                error.printStackTrace();

            }
        }, this, apiKey, secretKey);

    }

    /**
     * 初始化SDK
     */
    private void initLib() {
        // 为了android和ios 区分授权，appId=appname_face_android ,其中appname为申请sdk时的应用名
        // 应用上下文
        // 申请License取得的APPID
        // assets目录下License文件名
        FaceSDKManager.getInstance().init(this, Config.licenseID, Config.licenseFileName);
        setFaceConfig();
    }

    private void setFaceConfig() {
        FaceTracker tracker = FaceSDKManager.getInstance().getFaceTracker(this);  //.getFaceConfig();
        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整

        // 模糊度范围 (0-1) 推荐小于0.7
        tracker.set_blur_thr(FaceEnvironment.VALUE_BLURNESS);
        // 光照范围 (0-1) 推荐大于40
        tracker.set_illum_thr(FaceEnvironment.VALUE_BRIGHTNESS);
        // 裁剪人脸大小
        tracker.set_cropFaceSize(FaceEnvironment.VALUE_CROP_FACE_SIZE);
        // 人脸yaw,pitch,row 角度，范围（-45，45），推荐-15-15
        tracker.set_eulur_angle_thr(FaceEnvironment.VALUE_HEAD_PITCH, FaceEnvironment.VALUE_HEAD_ROLL,
                FaceEnvironment.VALUE_HEAD_YAW);

        // 最小检测人脸（在图片人脸能够被检测到最小值）80-200， 越小越耗性能，推荐120-200
        tracker.set_min_face_size(FaceEnvironment.VALUE_MIN_FACE_SIZE);
        //
        tracker.set_notFace_thr(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
        // 人脸遮挡范围 （0-1） 推荐小于0.5
        tracker.set_occlu_thr(FaceEnvironment.VALUE_OCCLUSION);
        // 是否进行质量检测
        tracker.set_isCheckQuality(true);
        // 是否进行活体校验
        tracker.set_isVerifyLive(false);
    }

    /**
     * 判断此用户是否有人脸库
     */
    private void checkFace(){
        OkGo.<String>post(API_URL+"?request=private.user.get.face.info&platform=app&token="+login_token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code=jsonObject.getInt("code");
                            if (code ==0){
                                JSONObject data=jsonObject.getJSONObject("data");
                                String groupID=data.getString("groupID");
                                String apiKey=data.getString("apiKey");
                                String secretKey=data.getString("secretKey");
                                initSDK(groupID,apiKey,secretKey);
                            }else {
                                JSONObject data=jsonObject.getJSONObject("data");
                                String groupID=data.getString("groupID");
                                String apiKey=data.getString("apiKey");
                                String secretKey=data.getString("secretKey");
                                initSDK(groupID,apiKey,secretKey);
                            }
                            //获取sharedPreferences对象
                            SharedPreferences sharedPreferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                            //获取editor对象
                            SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
                            //存储键值对
                            JSONObject data=jsonObject.getJSONObject("data");
                            editor.putString("user_id", data.getString("id"));
                            //提交
                            editor.commit();//提交修改
                            //进入主界面
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }


}
