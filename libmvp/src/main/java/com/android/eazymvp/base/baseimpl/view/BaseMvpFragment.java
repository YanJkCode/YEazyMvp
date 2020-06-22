package com.android.eazymvp.base.baseimpl.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.eazymvp.base.baseInterface.IBasePresenter;
import com.android.eazymvp.base.baseInterface.IBaseView;

public abstract class BaseMvpFragment<P extends IBasePresenter> extends BaseFragment implements IBaseView<P> {
    protected P mPresenter;

    @Override
    protected final void initMvp() {
        mPresenter = createPresenter();
        if (mPresenter != null) {
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
}
