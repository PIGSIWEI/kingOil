package com.ruoyu.pigroad.myapplication.Util;

import android.content.Context;
import android.widget.Toast;

import com.ruoyu.pigroad.myapplication.MapApplication;

/**
 * Created by PIGROAD on 2018/9/4
 * Email:920015363@qq.com
 */
public class Toastutil {
    private static Toast toast;

    public static void show(Context context, String title){
        if (toast == null){
            toast = Toast.makeText(context,title,Toast.LENGTH_LONG);
        }else {
            toast.setText(title);
        }
        toast.show();
    }
}
