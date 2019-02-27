/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.ruoyu.pigroad.myapplication.Util;


/**
 * JSON解析
 * @param <T>
 */
public interface Parser<T> {
    T parse(String json) throws FaceError;
}
