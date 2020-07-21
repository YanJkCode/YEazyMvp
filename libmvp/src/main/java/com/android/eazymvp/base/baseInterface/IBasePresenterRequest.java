package com.android.eazymvp.base.baseInterface;


import com.android.eazymvp.base.baseimpl.presenter.BaseDefViewBack;

import java.util.HashMap;

import okhttp3.MultipartBody;

public interface IBasePresenterRequest {
    /**
     * 通用请求
     *
     * @param url 请求的地址
     */
    void requestData(String url);

    /**
     * 通用请求
     *
     * @param url   请求的地址
     * @param datas 上传的数据
     */
    void requestData(String url, final HashMap<String, Object> datas);


    /**
     * 通用请求
     *
     * @param url              请求的地址
     * @param iBaseDefViewBack 回调接口
     * @param <T>              回调类型
     */
    <T> void requestData(String url,
                         final BaseDefViewBack<T> iBaseDefViewBack);

    /**
     * 通用请求
     *
     * @param url              请求的地址
     * @param datas            上传的数据
     * @param iBaseDefViewBack 回调接口
     * @param <T>              回调类型
     */
    <T> void requestData(String url,
                         final HashMap<String, Object> datas,
                         final BaseDefViewBack<T> iBaseDefViewBack);


    /**
     * 通用文件请求
     *
     * @param url              请求的地址
     * @param datas            上传的数据
     * @param file             要上传的文件
     * @param iBaseDefViewBack 回调接口
     * @param <T>              回调类型
     */
    <T> void requestDataFile(String url,
                             final HashMap<String, Object> datas,
                             MultipartBody.Part file,
                             final BaseDefViewBack<T> iBaseDefViewBack);
}
