package com.ruoyu.pigroad.myapplication.Ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.lang.UProperty;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ruoyu.pigroad.myapplication.Config.APIService;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.FaceError;
import com.ruoyu.pigroad.myapplication.Util.Md5;
import com.ruoyu.pigroad.myapplication.Util.OnResultListener;
import com.ruoyu.pigroad.myapplication.Widget.DetectLoginActivity;
import com.ruoyu.pigroad.myapplication.Widget.FaceDetectActivity;
import com.ruoyu.pigroad.myapplication.Widget.ImageSaveUtil;
import com.ruoyu.pigroad.myapplication.Widget.RegResult;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/10/27
 * Email:920015363@qq.com
 */
public class UpFaceActivity extends AppCompatActivity {

    private String facePath;
    private Bitmap mHeadBmp;
    final ZLoadingDialog dialog = new ZLoadingDialog(UpFaceActivity.this);
    @BindView(R.id.btn_face_register)
    Button btn_face_register;
    @BindView(R.id.tv_titile)
    TextView tv_titile;

    private static final int REQUEST_CODE_DETECT_FACE = 1000;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.up_face_layout);
        ButterKnife.bind(this);
        this.init();
    }

    private void init() {

        tv_titile.setText("人脸更新");

        //人脸注册
        btn_face_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(UpFaceActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UpFaceActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
                    return;
                }
                Intent it = new Intent(UpFaceActivity.this, UpFaceReActivity.class);
                startActivityForResult(it, REQUEST_CODE_DETECT_FACE);
            }
        });
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
               showLoading2(facePath);
            }
        } else if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            facePath = getRealPathFromURI(uri);

            if (mHeadBmp != null) {
                mHeadBmp.recycle();
            }
            mHeadBmp = ImageSaveUtil.loadBitmapFromPath(this, facePath);
            if (mHeadBmp != null) {
                //showLoading2("注册中",facePath);
                showLoading2(facePath);
            }
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

    private void Upreg(String filePath) {


//        if (!isELineCharacter(username)) {
//            toast("请输入数字、字母或下划线组合的用户名！");
//            return;
//        }

        final File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(UpFaceActivity.this, "文件不存在", Toast.LENGTH_LONG).show();
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
        String uid = Md5.MD5(getIntent().getStringExtra("user_id"), "utf-8");


        APIService.getInstance().upreg(new OnResultListener<RegResult>() {
            @Override
            public void onResult(RegResult result) {
                Log.i(LOG_TIP, "orientation->" + result.getJsonRes());
                try {
                    JSONObject jsonObject=new JSONObject(result.getJsonRes());
                    //人脸更新成功
                    dialog.dismiss();
                    faceSuccesDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(FaceError error) {
                Log.i(LOG_TIP, "orientation->" + error);
                //showDialog("注册更新失败！，请退出重试");
                dialog.dismiss();
                faceErrowDialog();
            }
        }, file, uid, getIntent().getStringExtra("user_id"));
    }

    /**
     * 加载中~~~
     */
    private void showLoading2( final String filepath){
        dialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                .setLoadingColor(Color.parseColor("#32cd32"))//颜色
                .setHintText("更新中")
                .setCanceledOnTouchOutside(false)
                .setHintTextSize(16f)
                .setCancelable(false)
                .show();
                Upreg(filepath);
    }

    /**
     * 人脸识别失败弹出
     */
    private void faceErrowDialog()
    {
        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("更新失败！");
        builder.setMessage("人脸更新失败，请确认网络设置！");
        builder.setCancelable(false);
        builder.setPositiveButton("再试一次", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO 实时人脸检测
                Intent it = new Intent(UpFaceActivity.this, UpFaceReActivity.class);
                startActivityForResult(it, REQUEST_CODE_DETECT_FACE);
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
        builder.setTitle("更新成功！");
        builder.setMessage("人脸更新成功！");
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

}
