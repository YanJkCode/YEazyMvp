package com.android.eazymvp.base.baseInterface;


import com.android.eazymvp.base.baseimpl.presenter.BaseDefViewBack;

import java.util.Map;

import io.reactivex.disposables.Disposable;
import okhttp3.MultipartBody;

public interface IBasePresenter<T extends IBaseView> {
    /**
     * 把V依附到P层
     *
     * @param t
     */
    void attachView(T t);

    /**
     * V与P层解绑
     */
    void detachView();

    /**
     * 解除临时请求
     */
    void detachTemporaryDetach();

    /**
     * 缓存请求
     *
     * @param disposable
     */
    void cacheRequest(Disposable disposable);

    /**
     * 取消请求
     */
    void cancelRequest();

    /**
     * 通用请求 已废弃 会导致内存泄露
     *
     * @param url 请求的地址
     */
    @Deprecated
    void requestData(String url);

    /**
     * 通用请求 已废弃 会导致内存泄露
     *
     * @param url   请求的地址
     * @param datas 上传的数据
     */
    @Deprecated
    void requestData(String url, final Map<String, Object> datas);

    /**
     * 通用请求
     *
     * @param iBaseDestroy 进行请求的窗口
     * @param url          请求的地址
     */
    void requestData(IBaseDestroy iBaseDestroy, String url);

    /**
     * 通用请求
     *
     * @param iBaseDestroy 进行请求的窗口
     * @param url          请求的地址
     * @param datas        上传的数据
     */
    void requestData(IBaseDestroy iBaseDestroy, String url, final Map<String, Object> datas);

    /**
     * 通用请求 已废弃 会导致内存泄露
     *
     * @param url              请求的地址
     * @param iBaseDefViewBack 回调接口
     * @param <T>              回调类型
     */
    @Deprecated
    <T> void requestData(String url, final BaseDefViewBack<T> iBaseDefViewBack);

    /**
     * 通用请求 已废弃 会导致内存泄露
     *
     * @param url              请求的地址
     * @param datas            上传的数据
     * @param iBaseDefViewBack 回调接口
     * @param <T>              回调类型
     */
    @Deprecated
    <T> void requestData(String url,
                         final Map<String, Object> datas,
                         final BaseDefViewBack<T> iBaseDefViewBack);

    /**
     * 通用请求
     *
     * @param iBaseDestroy     进行请求的窗口
     * @param url              请求的地址
     * @param iBaseDefViewBack 回调接口
     * @param <T>              回调类型
     */
    <T> void requestData(IBaseDestroy iBaseDestroy, String url,
                         final BaseDefViewBack<T> iBaseDefViewBack);

    /**
     * 通用请求
     *
     * @param iBaseDestroy     进行请求的窗口
     * @param url              请求的地址
     * @param datas            上传的数据
     * @param iBaseDefViewBack 回调接口
     * @param <T>              回调类型
     */
    <T> void requestData(IBaseDestroy iBaseDestroy, String url,
                         final Map<String, Object> datas,
                         final BaseDefViewBack<T> iBaseDefViewBack);

    /**
     * 通用文件请求 已废弃 会导致内存泄露
     *
     * @param Url              请求的地址
     * @param datas            上传的数据
     * @param file             要上传的文件
     * @param iBaseDefViewBack 回调接口
     * @param <T>              回调类型
     */
    @Deprecated
    <T> void requestDataFile(String Url,
                             final Map<String, Object> datas,
                             MultipartBody.Part file,
                             final BaseDefViewBack<T> iBaseDefViewBack);


    /**
     * 通用文件请求
     *
     * @param iBaseDestroy     进行请求的窗口
     * @param url              请求的地址
     * @param datas            上传的数据
     * @param file             要上传的文件
     * @param iBaseDefViewBack 回调接口
     * @param <T>              回调类型
     */
    <T> void requestDataFile(IBaseDestroy iBaseDestroy, String url,
                             final Map<String, Object> datas,
                             MultipartBody.Part file,
                             final BaseDefViewBack<T> iBaseDefViewBack);
}
