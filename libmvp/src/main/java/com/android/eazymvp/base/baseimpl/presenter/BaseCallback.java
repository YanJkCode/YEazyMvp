package com.android.eazymvp.base.baseimpl.presenter;

import com.android.eazymvp.base.baseInterface.IBaseViewBack;

public class BaseCallback<T> {
    private IBaseViewBack<T> mViewBack;
    private BaseDefViewBack<T> mBaseDefViewBack;
    private int type;

    public BaseCallback() {
    }

    public BaseCallback(IBaseViewBack<T> viewBack) {
        mViewBack = viewBack;
        type = 1;
    }

    public BaseCallback(BaseDefViewBack<T> baseDefViewBack) {
        mBaseDefViewBack = baseDefViewBack;
        type = 2;
    }

    public void onCallSuccessful(T value) {
        switch (type) {
            case 1:
                if (mViewBack != null) {
                    mViewBack.onSuccessful(value, -1);
                }
                break;
            case 2:
                if (mBaseDefViewBack != null) {
                    mBaseDefViewBack.onSuccessful(value);
                }
                break;
        }

    }

    public <M extends Throwable> void onCallFailed(M msg) {
        switch (type) {
            case 1:
                if (mViewBack != null) {
                    mViewBack.onFailed(msg.getMessage());
                }
                break;
            case 2:
                if (mBaseDefViewBack != null) {
                    mBaseDefViewBack.onFailed(msg.getMessage());
                }
                break;
        }

    }

    public Class<T> getClasst() {
        return mBaseDefViewBack != null ? mBaseDefViewBack.getClasst() : null;
    }

}
