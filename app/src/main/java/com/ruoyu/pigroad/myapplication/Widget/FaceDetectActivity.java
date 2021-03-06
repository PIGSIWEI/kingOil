/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.ruoyu.pigroad.myapplication.Widget;


import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.facesdk.FaceInfo;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.facesdk.CameraImageSource;
import com.ruoyu.pigroad.myapplication.facesdk.DetectRegionProcessor;
import com.ruoyu.pigroad.myapplication.facesdk.FaceDetectManager;
import com.ruoyu.pigroad.myapplication.facesdk.FaceFilter;
import com.ruoyu.pigroad.myapplication.facesdk.FaceSDKManager;
import com.ruoyu.pigroad.myapplication.facesdk.ImageFrame;
import com.ruoyu.pigroad.myapplication.facesdk.PreviewView;
import com.ruoyu.pigroad.myapplication.facesdk.camera.ICameraControl;
import com.ruoyu.pigroad.myapplication.facesdk.camera.PermissionCallback;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.ruoyu.pigroad.myapplication.Widget.Base64RequestBody.readFile;


/**
 * 实时检测调用identify进行人脸识别，MainActivity未给出改示例的入口，开发者可以在MainActivity调用
 * Intent intent = new Intent(MainActivity.this, FaceDetectActivity.class);
 * startActivity(intent);
 */
public class FaceDetectActivity extends AppCompatActivity {

    private static final int MSG_INITVIEW = 1001;
    private static final int MSG_BEGIN_DETECT = 1002;
    private TextView nameTextView;
    private PreviewView previewView;
    private View mInitView;
    private FaceRoundView rectView;
    private boolean mGoodDetect = false;
    private static final double ANGLE = 15;
    private ImageView closeIv;
    private boolean mDetectStoped = false;
    private ImageView mSuccessView;
    private Handler mHandler;
    private String mCurTips;
    private boolean mUploading = false;
    private long mLastTipsTime = 0;
    private int mCurFaceId = -1;
    private TCircleProgressView progress_view;
    private ActivityManager activityManager;

    private FaceDetectManager faceDetectManager;
    private DetectRegionProcessor cropProcessor = new DetectRegionProcessor();
    private WaveHelper mWaveHelper;
    private WaveView mWaveview;
    private int mBorderColor = Color.parseColor("#28FFFFFF");
    private int mBorderWidth = 0;
    private int mScreenW;
    private int mScreenH;
    private boolean mSavedBmp = false;
    // 开始人脸检测
    private boolean mBeginDetect = false;
    private TextView tv_titile;
    private ImageView iv_back;
    int start=0;
    int pro=0;
    String str = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_detected);
        faceDetectManager = new FaceDetectManager(this);
        initScreen();
        mHandler = new InnerHandler(this);
        mHandler.sendEmptyMessageDelayed(MSG_INITVIEW, 500);
        mHandler.sendEmptyMessageDelayed(MSG_BEGIN_DETECT, 500);
        tv_titile=findViewById(R.id.tv_titile);
        tv_titile.setText("人脸注册");
        activityManager= ActivityManager.getInstance();
        activityManager.addActivity(this);
        initView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            Intent intent2=new Intent();
            setResult(2,intent2);
            finish();
            return true;//不执行父类点击事件
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }

    private void initScreen() {
        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        mScreenW = outMetrics.widthPixels;
        mScreenH = outMetrics.heightPixels;
    }

    private void initView() {
        iv_back=findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2=new Intent();
                setResult(2,intent2);
                finish();
            }
        });
        mInitView = findViewById(R.id.camera_layout);
        previewView = (PreviewView) findViewById(R.id.preview_view);
        rectView = (FaceRoundView) findViewById(R.id.rect_view);
        final CameraImageSource cameraImageSource = new CameraImageSource(this);
        cameraImageSource.setPreviewView(previewView);

        faceDetectManager.setImageSource(cameraImageSource);
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(final int retCode, FaceInfo[] infos, ImageFrame frame) {

                if (mUploading) {
                    //   Log.d("DetectLoginActivity", "is uploading ,not detect time");
                    return;
                }
                //  Log.d("DetectLoginActivity", "retCode is:" + retCode);
                if (retCode == 0) {
                    if (infos != null && infos[0] != null) {
                        FaceInfo info = infos[0];
                        boolean distance = false;
                        if (info != null && frame != null) {
                            if (info.mWidth >= (0.9 * frame.getWidth())) {
                                distance = false;
                                str = getResources().getString(R.string.detect_zoom_out);
                                pro = 60;
                                mHandler.sendEmptyMessage(1003);
                            } else if (info.mWidth <= 0.4 * frame.getWidth()) {
                                distance = false;
                                str = getResources().getString(R.string.detect_zoom_in);
                                pro = 60;
                                mHandler.sendEmptyMessage(1003);
                            } else {
                                distance = true;
                            }
                        }
                        boolean headUpDown;
                        if (info != null) {
                            if (info.headPose[0] >= ANGLE) {
                                headUpDown = false;
                                str = getResources().getString(R.string.detect_head_up);
                                pro = 70;
                                mHandler.sendEmptyMessage(1003);
                            } else if (info.headPose[0] <= -ANGLE) {
                                headUpDown = false;
                                str = getResources().getString(R.string.detect_head_down);
                                pro = 70;
                                mHandler.sendEmptyMessage(1003);
                            } else {
                                headUpDown = true;
                            }

                            boolean headLeftRight;
                            if (info.headPose[1] >= ANGLE) {
                                headLeftRight = false;
                                str = getResources().getString(R.string.detect_head_left);
                                pro = 70;
                                mHandler.sendEmptyMessage(1003);
                            } else if (info.headPose[1] <= -ANGLE) {
                                headLeftRight = false;
                                str = getResources().getString(R.string.detect_head_right);
                                pro = 72;
                                mHandler.sendEmptyMessage(1003);
                            } else {
                                headLeftRight = true;
                            }

                            if (distance && headUpDown && headLeftRight) {
                                mGoodDetect = true;
                            } else {
                                mGoodDetect = false;
                            }

                        }
                    }
                } else if (retCode == 1) {
                    str = getResources().getString(R.string.detect_head_up);
                    pro = 72;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 2) {
                    str = getResources().getString(R.string.detect_head_down);
                    pro = 71;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 3) {
                    str = getResources().getString(R.string.detect_head_left);
                    pro = 61;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 4) {
                    str = getResources().getString(R.string.detect_head_right);
                    pro = 62;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 5) {
                    str = getResources().getString(R.string.detect_low_light);
                    pro = 72;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 6) {
                    str = getResources().getString(R.string.detect_face_in);
                    pro = 43;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 7) {
                    str = getResources().getString(R.string.detect_face_in);
                    pro = 42;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 10) {
                    str = getResources().getString(R.string.detect_keep);
                    pro = 73;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 11) {
                    str = getResources().getString(R.string.detect_occ_right_eye);
                    pro = 81;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 12) {
                    str = getResources().getString(R.string.detect_occ_left_eye);
                    pro = 82;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 13) {
                    str = getResources().getString(R.string.detect_occ_nose);
                    pro=91;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 14) {
                    str = getResources().getString(R.string.detect_occ_mouth);
                    pro=92;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 15) {
                    str = getResources().getString(R.string.detect_right_contour);
                    pro=62;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 16) {
                    str = getResources().getString(R.string.detect_left_contour);
                    pro=62;
                    mHandler.sendEmptyMessage(1003);
                } else if (retCode == 17) {
                    str = getResources().getString(R.string.detect_chin_contour);
                    pro=82;
                    mHandler.sendEmptyMessage(1003);
                }

                boolean faceChanged = true;
                if (infos != null && infos[0] != null) {
                    Log.d("DetectLogin", "face id is:" + infos[0].face_id);
                    if (infos[0].face_id == mCurFaceId) {
                        faceChanged = false;
                    } else {
                        faceChanged = true;
                    }
                    mCurFaceId = infos[0].face_id;
                }

                if (faceChanged) {
                    showProgressBar(false);
                    //progress_view.setProgressByAnimation(0,200);
                    onRefreshSuccessView(false);
                }

                final int resultCode = retCode;
                if (!(mGoodDetect && retCode == 0)) {
                    if (faceChanged) {
                        showProgressBar(false);
                        onRefreshSuccessView(false);
                    }
                }

                if (retCode == 6 || retCode == 7 || retCode < 0) {
                    rectView.processDrawState(true);
                } else {
                    rectView.processDrawState(false);
                }

                mCurTips = str;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((System.currentTimeMillis() - mLastTipsTime) > 1000) {
                            nameTextView.setText(mCurTips);
                            mLastTipsTime = System.currentTimeMillis();
                            mHandler.sendEmptyMessage(1003);
                        }
                        if (mGoodDetect && resultCode == 0) {
                            progress_view.setText("");
                            onRefreshSuccessView(true);
                            //showProgressBar(true);
                        }
                    }
                });

                if (infos == null) {
                    mGoodDetect = false;
                }


            }
        });
        faceDetectManager.setOnTrackListener(new FaceFilter.OnTrackListener() {
            @Override
            public void onTrack(FaceFilter.TrackedModel trackedModel) {
                if (trackedModel.meetCriteria() && mGoodDetect) {
                    // upload(trackedModel);
                    mGoodDetect = false;
                    if (!mSavedBmp && mBeginDetect) {
                        if (saveFaceBmp(trackedModel)) {
                            mHandler.sendEmptyMessage(1004);
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                }
            }
        });

        cameraImageSource.getCameraControl().setPermissionCallback(new PermissionCallback() {
            @Override
            public boolean onRequestPermission() {
                ActivityCompat.requestPermissions(FaceDetectActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 100);
                return true;
            }
        });

        rectView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                start();
                rectView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        ICameraControl control = cameraImageSource.getCameraControl();
        control.setPreviewView(previewView);
        // 设置检测裁剪处理器
        faceDetectManager.addPreProcessor(cropProcessor);

        int orientation = getResources().getConfiguration().orientation;
        boolean isPortrait = (orientation == Configuration.ORIENTATION_PORTRAIT);

        if (isPortrait) {
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
        } else {
            previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        cameraImageSource.getCameraControl().setDisplayOrientation(rotation);
        //   previewView.getTextureView().setScaleX(-1);
        nameTextView = (TextView) findViewById(R.id.name_text_view);
        closeIv = (ImageView) findViewById(R.id.closeIv);
        progress_view=findViewById(R.id.progress_view);

        progress_view.setTotalProgress(100);
        progress_view.setAnimationDuration(500);
        progress_view.setHintTextSize(15);

        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSuccessView = (ImageView) findViewById(R.id.success_image);

        mSuccessView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mSuccessView.getTag() == null) {
                    Rect rect = rectView.getFaceRoundRect();
                    RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mSuccessView.getLayoutParams();
                    int w = (int) getResources().getDimension(R.dimen.x45);
                    rlp.setMargins(
                            rect.centerX() - (w / 2),
                            rect.top - (w / 2 -120),
                            0,
                            0);
                    mSuccessView.setLayoutParams(rlp);
                    mSuccessView.setTag("setlayout");
                    mHandler.sendEmptyMessage(1004);
                }
                mSuccessView.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mSuccessView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mSuccessView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        // mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        init();
    }

    private void initWaveview(Rect rect) {
        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.root_view);

        RelativeLayout.LayoutParams waveParams = new RelativeLayout.LayoutParams(
                rect.width(), rect.height());

        waveParams.setMargins(rect.left, rect.top, rect.left, rect.top);
        waveParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        waveParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        mWaveview = new WaveView(this);
        rootView.addView(mWaveview, waveParams);

        // mWaveview = (WaveView) findViewById(R.id.wave);
        mWaveHelper = new WaveHelper(mWaveview);

        mWaveview.setShapeType(WaveView.ShapeType.CIRCLE);
        mWaveview.setWaveColor(
                Color.parseColor("#28FFFFFF"),
                Color.parseColor("#3cFFFFFF"));

//        mWaveview.setWaveColor(
//                Color.parseColor("#28f16d7a"),
//                Color.parseColor("#3cf16d7a"));

        mBorderColor = Color.parseColor("#28f16d7a");
        mWaveview.setBorder(mBorderWidth, mBorderColor);
    }

    private void visibleView() {
        mInitView.setVisibility(View.INVISIBLE);
    }

    private boolean saveFaceBmp(FaceFilter.TrackedModel model) {

        final Bitmap face = model.cropFace();
        if (face != null) {
            Log.d("save", "save bmp");
            ImageSaveUtil.saveCameraBitmap(FaceDetectActivity.this, face, "head_tmp.jpg");
        }
        String filePath = ImageSaveUtil.loadCameraBitmapPath(this, "head_tmp.jpg");
        final File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        boolean saved = false;
        try {
            byte[] buf = readFile(file);
            if (buf.length > 0) {
                saved = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!saved) {
            Log.d("fileSize", "file size >=-99");
        } else {
            mSavedBmp = true;
        }
        return saved;
    }

    private void initBrightness() {
        int brightness = BrightnessTools.getScreenBrightness(FaceDetectActivity.this);
        if (brightness < 200) {
            BrightnessTools.setBrightness(this, 200);
        }
    }


    private void init() {

        FaceSDKManager.getInstance().getFaceTracker(this).set_min_face_size(200);
        FaceSDKManager.getInstance().getFaceTracker(this).set_isCheckQuality(true);
        // 该角度为商学，左右，偏头的角度的阀值，大于将无法检测出人脸，为了在1：n的时候分数高，注册尽量使用比较正的人脸，可自行条件角度
        FaceSDKManager.getInstance().getFaceTracker(this).set_eulur_angle_thr(15, 15, 15);
        FaceSDKManager.getInstance().getFaceTracker(this).set_isVerifyLive(true);
        FaceSDKManager.getInstance().getFaceTracker(this).set_notFace_thr(0.2f);
        FaceSDKManager.getInstance().getFaceTracker(this).set_occlu_thr(0.1f);

        initBrightness();
    }

    private void start() {

        Rect dRect = rectView.getFaceRoundRect();

        //   RectF newDetectedRect = new RectF(detectedRect);
        int preGap = getResources().getDimensionPixelOffset(R.dimen.x50);
        int w = getResources().getDimensionPixelOffset(R.dimen.x2);

        int orientation = getResources().getConfiguration().orientation;
        boolean isPortrait = (orientation == Configuration.ORIENTATION_PORTRAIT);
        if (isPortrait) {
            // 检测区域矩形宽度
            int rWidth = mScreenW - 2 * preGap;
            // 圆框宽度
            int dRectW = dRect.width();
            // 检测矩形和圆框偏移
            int h = (rWidth - dRectW) / 2;
            //  Log.d("liujinhui hi is:", " h is:" + h + "d is:" + (dRect.left - 150));
            int rLeft = w;
            int rRight = rWidth - w;
            int rTop = dRect.top - h - preGap + w;
            int rBottom = rTop + rWidth - w;

            //  Log.d("liujinhui", " rLeft is:" + rLeft + "rRight is:" + rRight + "rTop is:" + rTop + "rBottom is:" + rBottom);
            RectF newDetectedRect = new RectF(rLeft, rTop, rRight, rBottom);
            cropProcessor.setDetectedRect(newDetectedRect);
        } else {
            int rLeft = mScreenW / 2 - mScreenH / 2 + w;
            int rRight = mScreenW / 2 + mScreenH / 2 + w;
            int rTop = 0;
            int rBottom = mScreenH;

            RectF newDetectedRect = new RectF(rLeft, rTop, rRight, rBottom);
            cropProcessor.setDetectedRect(newDetectedRect);
        }


        faceDetectManager.start();
        initWaveview(dRect);
    }

    @Override
    protected void onStop() {
        super.onStop();
        faceDetectManager.stop();
        mDetectStoped = true;
        onRefreshSuccessView(false);
        mHandler.sendEmptyMessage(1005);
        if (mWaveview != null) {
            mWaveview.setVisibility(View.GONE);
            mWaveHelper.cancel();
        }
    }

    private void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    if (mWaveview != null) {
                        mWaveview.setVisibility(View.VISIBLE);
                        mWaveHelper.start();
                    }
                } else {
                    if (mWaveview != null) {
                        mWaveview.setVisibility(View.GONE);
                        mWaveHelper.cancel();
                    }
                }

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWaveview != null) {
            mWaveHelper.cancel();
            mWaveview.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDetectStoped) {
            faceDetectManager.start();
            mDetectStoped = false;
        }

    }

    private void onRefreshSuccessView(final boolean isShow) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isShow){
                    mSuccessView.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessage(1004);
                }else {
                    mSuccessView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private  class InnerHandler extends Handler {
        private WeakReference<FaceDetectActivity> mWeakReference;

        public InnerHandler(FaceDetectActivity activity) {
            super();
            this.mWeakReference = new WeakReference<FaceDetectActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakReference == null || mWeakReference.get() == null) {
                return;
            }
            FaceDetectActivity activity = mWeakReference.get();
            if (activity == null) {
                return;
            }
            if (msg == null) {
                return;

            }
            switch (msg.what) {
                case MSG_INITVIEW:
                    activity.visibleView();
                    break;
                case MSG_BEGIN_DETECT:
                    activity.mBeginDetect = true;
                    break;
                case 1003:
                    progress_view.setText(str);
                    progress_view.setProgressByAnimation(start,pro);
                    start=pro;
                    break;
                case 1004:
                    //progress_view.setText(str);
                    Log.i("pppppppp","1004");
                    progress_view.setProgressByAnimation(pro,progress_view.getTotalProgress());
                    //start=pro;
                    break;
                case 1005:
                    progress_view.setText(str);
                    progress_view.setProgressByAnimation(start,0);
                    start=0;
                    break;
            }
        }
    }



}
