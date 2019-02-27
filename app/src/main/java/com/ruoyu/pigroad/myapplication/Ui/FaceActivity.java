package com.ruoyu.pigroad.myapplication.Ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.facesdk.FaceTracker;
import com.iflytek.cloud.thirdparty.F;
import com.iflytek.cloud.thirdparty.L;
import com.iflytek.cloud.thirdparty.S;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Config.APIService;
import com.ruoyu.pigroad.myapplication.Config.Config;
import com.ruoyu.pigroad.myapplication.MainActivity;
import com.ruoyu.pigroad.myapplication.MainActivity2;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.AccessToken;
import com.ruoyu.pigroad.myapplication.Util.FaceError;
import com.ruoyu.pigroad.myapplication.Util.Md5;
import com.ruoyu.pigroad.myapplication.Util.OnResultListener;
import com.ruoyu.pigroad.myapplication.Widget.DetectLoginActivity;
import com.ruoyu.pigroad.myapplication.Widget.FaceDetectActivity;
import com.ruoyu.pigroad.myapplication.Widget.ImageSaveUtil;
import com.ruoyu.pigroad.myapplication.Widget.RegResult;
import com.ruoyu.pigroad.myapplication.facesdk.FaceEnvironment;
import com.ruoyu.pigroad.myapplication.facesdk.FaceSDKManager;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/10/24
 * Email:920015363@qq.com
 */
public class FaceActivity extends AppCompatActivity{
    private Handler handler = new Handler(Looper.getMainLooper());

    @BindView(R.id.tv_titile)
    TextView tv_titile;
    @BindView(R.id.iv_back)
    ImageView iv_bacl;
    @BindView(R.id.btn_face_register)
    Button btn_face_register;

    private String facePath;
    private Bitmap mHeadBmp;

    private static final int REQUEST_CODE_DETECT_FACE = 1000;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;

    private String login_token;
    private String user_id="";
    final ZLoadingDialog loadingDialog = new ZLoadingDialog(FaceActivity.this);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_layout);
        ButterKnife.bind(this);
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {
        tv_titile.setText("人脸识别");
        iv_bacl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //人脸注册
        btn_face_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(FaceActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FaceActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
                    return;
                }
                Intent it = new Intent(FaceActivity.this, FaceDetectActivity.class);
                startActivityForResult(it, REQUEST_CODE_DETECT_FACE);
            }
        });

        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);
        loadingDialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                .setLoadingColor(Color.parseColor("#32cd32"))//颜色
                .setHintText("加载中")
                .setCanceledOnTouchOutside(false)
                .setHintTextSize(16f)
                .setCancelable(false)
                .show();

        checkFace();

    }

    private void initFace() {
        // 如果图片中的人脸小于200*200个像素，将不能检测出人脸，可以根据需求在100-400间调节大小
        FaceSDKManager.getInstance().getFaceTracker(this).set_min_face_size(200);
        FaceSDKManager.getInstance().getFaceTracker(this).set_isCheckQuality(true);
        // 该角度为商学，左右，偏头的角度的阀值，大于将无法检测出人脸，为了在1：n的时候分数高，注册尽量使用比较正的人脸，可自行条件角度
        FaceSDKManager.getInstance().getFaceTracker(this).set_eulur_angle_thr(45, 45, 45);
        FaceSDKManager.getInstance().getFaceTracker(this).set_isVerifyLive(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DETECT_FACE && resultCode == Activity.RESULT_OK) {

            facePath = ImageSaveUtil.loadCameraBitmapPath(this, "head_tmp.jpg");
            if (mHeadBmp != null) {
                mHeadBmp.recycle();
            }
            mHeadBmp = ImageSaveUtil.loadBitmapFromPath(this, facePath);
            if (mHeadBmp != null) {
                btn_face_register.setEnabled(false);
                btn_face_register.setClickable(false);
                showLoading2("注册中",facePath);
            }
        } else if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            facePath = getRealPathFromURI(uri);

            if (mHeadBmp != null) {
                mHeadBmp.recycle();
            }
            mHeadBmp = ImageSaveUtil.loadBitmapFromPath(this, facePath);
            if (mHeadBmp != null) {
                btn_face_register.setEnabled(false);
                btn_face_register.setClickable(false);
                showLoading2("注册中",facePath);
            }
        }else if (requestCode == 0 && resultCode == 1){
            //人脸识别失败
            faceErrowDialog();
        }else if (requestCode == REQUEST_CODE_DETECT_FACE && resultCode == 2){

        }else if (requestCode == 0 && resultCode == 2){
            //人脸识别成功
            faceSuccesDialog();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void reg(String filePath) {

//        if (!isELineCharacter(username)) {
//            toast("请输入数字、字母或下划线组合的用户名！");
//            return;
//        }

        final File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(FaceActivity.this, "文件不存在", Toast.LENGTH_LONG).show();
            return;
        }
        // TODO 人脸注册说明 https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/add
        // 模拟注册，先提交信息注册获取uid，再使用人脸+uid到百度人脸库注册，
        // TODO 实际使用中，建议注册放到您的服务端进行（这样可以有效防止ak，sk泄露） 把注册信息包括人脸一次性提交到您的服务端，
        // TODO 注册获得uid，然后uid+人脸调用百度人脸注册接口，进行注册。

        // 每个开发者账号只能创建一个人脸库；
        // 每个人脸库下，用户组（group）数量没有限制；
        // 每个用户组（group）下，可添加最多300000张人脸，如每个uid注册一张人脸，则最多300000个用户uid；
        // 每个用户（uid）所能注册的最大人脸数量没有限制；
        // 说明：人脸注册完毕后，生效时间最长为35s，之后便可以进行识别或认证操作。
        // 说明：注册的人脸，建议为用户正面人脸。
        // 说明：uid在库中已经存在时，对此uid重复注册时，新注册的图片默认会追加到该uid下，如果手动选择action_type:replace，
        // 则会用新图替换库中该uid下所有图片。
        // uid          是	string	用户id（由数字、字母、下划线组成），长度限制128B
        // user_info    是	string	用户资料，长度限制256B
        // group_id	    是	string	用户组id，标识一组用户（由数字、字母、下划线组成），长度限制128B。
        // 如果需要将一个uid注册到多个group下，group_id,需要用多个逗号分隔，每个group_id长度限制为48个英文字符
        // image	    是	string	图像base64编码，每次仅支持单张图片，图片编码后大小不超过10M
        // action_type	否	string	参数包含append、replace。如果为“replace”，则每次注册时进行替换replace（新增或更新）操作，
        // 默认为append操作
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                faceReg(file);
            }
        }, 1000);

    }


    private void faceReg(File file) {

        // 用户id（由数字、字母、下划线组成），长度限制128B
        // uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。

        // String uid = 修改为自己用户系统中用户的id;
        // 模拟使用username替代
   //     String username = usernameEt.getText().toString().trim();
        String uid = Md5.MD5(user_id, "utf-8");


        APIService.getInstance().reg(new OnResultListener<RegResult>() {
            @Override
            public void onResult(RegResult result) {
                Log.i(LOG_TIP, "orientation->" + result.getJsonRes());
                try {
                    JSONObject jsonObject=new JSONObject(result.getJsonRes());
                    JSONObject face_result=jsonObject.getJSONObject("result");
                    sendFaceToken(face_result.getString("face_token"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(FaceError error) {
                Log.i(LOG_TIP, "orientation->" + error);
                showDialog("注册人脸失败！，请退出重试");
            }
        }, file, uid, user_id);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

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
                                btn_face_register.setEnabled(true);
                                btn_face_register.setClickable(true);
                                loadingDialog.dismiss();
                                JSONObject data=jsonObject.getJSONObject("data");
                                String groupID=data.getString("groupID");
                                String apiKey=data.getString("apiKey");
                                String secretKey=data.getString("secretKey");
                                user_id=data.getString("id");
                                initSDK(groupID,apiKey,secretKey);
                            }else {
                                loadingDialog.dismiss();
                                showDialog("你已注册过人脸，无需再注册！");
                                JSONObject data=jsonObject.getJSONObject("data");
                                String groupID=data.getString("groupID");
                                String apiKey=data.getString("apiKey");
                                String secretKey=data.getString("secretKey");
                                user_id=data.getString("id");
                                initSDK(groupID,apiKey,secretKey);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

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

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                       // Toast.makeText(FaceActivity.this, "启动成功", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(FaceError error) {
                Log.e("xx", "AccessTokenError:" + error);
                error.printStackTrace();

            }
        }, this, apiKey, secretKey);

    }

    /**
     * Dialog提示
     */
    private void showDialog(String str){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.face_dialog, null);
        builder.setCancelable(false);
        builder.create();
        Button btn_confirm=view.findViewById(R.id.btn_confirm);
        Button btn_change=view.findViewById(R.id.btn_change);
        TextView tv=view.findViewById(R.id.tv);
        tv.setText(str);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //人脸修改
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 实时人脸检测
                Intent intent = new Intent(FaceActivity.this, DetectLoginActivity.class);
                intent.putExtra("user_id",user_id);
                startActivityForResult(intent,0);
                dialog.dismiss();
            }
        });
    }

    /**
     * 加载中~~~
     */
    private void showLoading(String str){
        final ZLoadingDialog dialog = new ZLoadingDialog(FaceActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                .setLoadingColor(Color.parseColor("#32cd32"))//颜色
                .setHintText(str)
                .setCanceledOnTouchOutside(false)
                .setHintTextSize(16f)
                .setCancelable(false)
                .show();
        new Handler().postDelayed(new Runnable(){
            public void run(){
                //execute the task
                dialog.dismiss();
            }
        },2000);
    }

    /**
     * 加载中~~~
     */
    private void showLoading2(String str, final String filepath){
        final ZLoadingDialog dialog = new ZLoadingDialog(FaceActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                .setLoadingColor(Color.parseColor("#32cd32"))//颜色
                .setHintText(str)
                .setCanceledOnTouchOutside(false)
                .setHintTextSize(16f)
                .setCancelable(false)
                .show();
        new Handler().postDelayed(new Runnable(){
            public void run(){
                //execute the task
                reg(filepath);
                dialog.dismiss();
            }
        },3000);
    }

    /**
     * 将face_token 发送服务器
     */
    private void sendFaceToken(String face_token){
        OkGo.<String>post(API_URL+"?request=private.user.save_face_token&platform=app&token="+login_token+"&face_token="+face_token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                showDialog("注册人脸成功！");

                            }else {
                                showDialog("注册人脸失败！，请退出重试");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 人脸识别失败弹出
     */
    private void faceErrowDialog()
    {
        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("校验失败！");
        builder.setMessage("人脸识别失败，请确认是否本人！");
        builder.setCancelable(false);
        builder.setPositiveButton("再试一次", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO 实时人脸检测
                Intent intent = new Intent(FaceActivity.this, DetectLoginActivity.class);
                intent.putExtra("user_id",user_id);
                startActivityForResult(intent,0);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

    /**
     * 人脸 检验成功弹窗
     */
    private void faceSuccesDialog(){
        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("校验成功！");
        builder.setMessage("人脸识别成功，请重新录入！");
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent=new Intent(FaceActivity.this,UpFaceActivity.class);
                intent.putExtra("user_id",user_id);
                startActivity(intent);
                finish();
            }
        });
        builder.create().show();
    }

}
