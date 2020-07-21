package com.android.eazymvp.base.baseimpl.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.eazymvp.base.baseInterface.IBasePresenter;
import com.android.eazymvp.base.baseInterface.IBasePresenterRequest;
import com.android.eazymvp.base.baseInterface.IBaseView;
import com.android.eazymvp.base.baseimpl.presenter.BaseDefViewBack;
import com.android.eazymvp.base.baseimpl.presenter.BasePresenter;

import java.util.HashMap;

import okhttp3.MultipartBody;


public abstract class BaseMvpFragment<P extends IBasePresenter> extends BaseFragment implements IBaseView<P>, IBasePresenterRequest {
    protected P mPresenter;

    @Override
    protected final void initMvp() {
        mPresenter = createPresenter();
        if (mPresenter != null && this instanceof IBaseView) {
            mPresenter.attachView(this);
        }
    }

    @Override
    public final void onDestroyView() {
        super.onDestroyView();
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter = null;
        }
    }

    public void Failed(String msg) {
        if (!isOnInternet(getActivity())) {
            showHintCenter("网络连接失败,请确认网络状态!");
        } else {
            showHintCenter(msg);
        }
    }

    public static boolean isOnInternet(Context context) {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                @SuppressLint("MissingPermission") NetworkInfo networkInfo =
                        connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public P createPresenter() {
        BasePresenter basePresenter = BasePresenter.getBasePresenter();
        try {
            return (P) basePresenter;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取一个HashMap String,Object
     */
    public HashMap<String, Object> getHashMap() {
        if (isNotPresenter()) {
            return new HashMap<String, Object>();
        }
        return mPresenter.getHashMap();
    }

    /**
     * 判断当前 数据服务是否存在  防止空指针
     *
     * @return true 不存在  false 存在
     */
    private boolean isNotPresenter() {
        return mPresenter == null;
    }

    @Override
    public void requestData(String url) {
        if (isNotPresenter()) {
            return;
        }
        mPresenter.requestData(this, url);
    }

    @Override
    public void requestData(String url, HashMap<String, Object> datas) {
        if (isNotPresenter()) {
            return;
        }
        mPresenter.requestData(this, url, datas);
    }


    @Override
    public <T> void requestData(String url, BaseDefViewBack<T> iBaseDefViewBack) {
        if (isNotPresenter()) {
            return;
        }
        mPresenter.requestData(this, url, iBaseDefViewBack);
    }

    @Override
    public <T> void requestData(String url, HashMap<String, Object> datas, BaseDefViewBack<T> iBaseDefViewBack) {
        if (isNotPresenter()) {
            return;
        }
        mPresenter.requestData(this, url, datas, iBaseDefViewBack);
    }


    @Override
    public <T> void requestDataFile(String url, HashMap<String, Object> datas, MultipartBody.Part file, BaseDefViewBack<T> iBaseDefViewBack) {
        if (isNotPresenter()) {
            return;
        }
        mPresenter.requestDataFile(this, url, datas, file, iBaseDefViewBack);
    }
}
