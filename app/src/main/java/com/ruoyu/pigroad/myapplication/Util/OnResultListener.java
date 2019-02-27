/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.ruoyu.pigroad.myapplication.Util;

public interface OnResultListener<T> {
    void onResult(T result);

    void onError(FaceError error);
}
