package com.ruoyu.pigroad.myapplication.Ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.Util.Toastutil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/12
 * Email:920015363@qq.com
 */
public class UploadActivity extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_titile)
    TextView tv_titile;
    @BindView(R.id.ll_upload)
    LinearLayout ll_upload;
    @BindView(R.id.iv_bg)
    ImageView iv_bg;
    @BindView(R.id.rl_bg)
    RelativeLayout rl_bg;

    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_CAMERA_IMAGE = 2;
    private String mCurrentPhotoPath = null;
    private String id;
    private String login_token;
    private ActivityManager activityManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.updata_layout);
        ButterKnife.bind(this);
        activityManager= ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {

        tv_titile.setText("上传截图");

        ll_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopueWindow();
            }
        });

        rl_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopueWindow();
            }
        });



        id = getIntent().getStringExtra("id");

        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);
    }


    /**
     * 弹出信息框
     */
    private void showPopueWindow() {
        View popView = View.inflate(this, R.layout.upload_dialog, null);
        Button bt_album = (Button) popView.findViewById(R.id.btn_pop_album);
        Button bt_camera = (Button) popView.findViewById(R.id.btn_pop_camera);
        Button bt_cancle = (Button) popView.findViewById(R.id.btn_pop_cancel);
        //获取屏幕宽高
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels * 1 / 3;
        final PopupWindow popupWindow = new PopupWindow(popView, weight, height);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        //点击外部popueWindow消失
        popupWindow.setOutsideTouchable(true);
        bt_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
                popupWindow.dismiss();

            }
        });
        bt_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeCamera(RESULT_CAMERA_IMAGE);
                popupWindow.dismiss();

            }
        });
        bt_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

            }
        });
        //popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });
        //popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM, 0, 50);

    }

    /**
     * 调用相机
     *
     * @param num
     */
    private void takeCamera(int num) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
            }
        }

        startActivityForResult(takePictureIntent, num);//跳转界面传回拍照所得数据
    }

    private File createImageFile() {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = null;
        try {
            image = File.createTempFile(
                    generateFileName(),  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static String generateFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        return imageFileName;
    }


    /**
     * 接受返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMAGE && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                final String picturePath = cursor.getString(columnIndex);
                cursor.close();

                Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss     ");

                Bitmap bm = BitmapFactory.decodeFile(picturePath);

                Upload(saveBitmap(bm, formatter.format(curDate) + ".jpg"));

                Glide.with(UploadActivity.this).load(picturePath)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .override(1080, 1920)//图片压缩
                        .centerCrop()
                        .dontAnimate()
                        .into(iv_bg);

            } else if (requestCode == RESULT_CAMERA_IMAGE) {

                SimpleTarget target = new SimpleTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        iv_bg.setImageBitmap(resource);
                        //拍照的图片生成到本地
                        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss     ");
                        Upload(saveBitmap(resource, formatter.format(curDate) + ".jpg"));
                    }

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);

                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);

                    }
                };

                Glide.with(UploadActivity.this).load(mCurrentPhotoPath)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .override(1080, 1920)//图片压缩
                        .centerCrop()
                        .dontAnimate()
                        .into(target);

            }
        }
    }


    /**
     * 上传图片操作
     */
    private void Upload(final File file) {
        OkGo.<String>post(API_URL + "?request=private.car_auth.upload.car.auth.pic.action&token=" + login_token + "&platform=app&file_dir=car_auth&file_type=form_data&id=" + id)
                .params("file", file)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP, response.body());
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            int code = jsonObject.getInt("code");
                            String msg = jsonObject.getString("msg");
                            if (code == 0) {
                                Toastutil.show(UploadActivity.this, msg);
                                Intent intent=new Intent(UploadActivity.this,DataActivity.class);
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
                                Intent intent4=new Intent(UploadActivity.this, LoginActivity.class);
                                startActivity(intent4);
                            } else {
                                Toastutil.show(UploadActivity.this, msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /*
     * 保存文件，文件名为当前日期
     */
    public File saveBitmap(Bitmap bitmap, String bitName) {
        String fileName;
        File file;
        if (Build.BRAND.equals("Xiaomi")) { // 小米手机
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + bitName;
        } else {  // Meizu 、Oppo
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + bitName;
        }
        file = new File(fileName);

        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
                // 插入图库
                MediaStore.Images.Media.insertImage(this.getContentResolver(), file.getAbsolutePath(), bitName, null);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return file;
    }


}
