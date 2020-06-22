package com.android.eazymvp.base.baseimpl.presenter;

import java.lang.reflect.ParameterizedType;

public abstract class BaseDefViewBack<T> {
    public Class<T> classt;

    public BaseDefViewBack() {
        ParameterizedType types = (ParameterizedType) getClass().getGenericSuperclass();
        classt = (Class<T>) types.getActualTypeArguments()[0];
    }

    public abstract void onSuccessful(T t);

    public abstract void onFailed(String msg);

    public Class<T> getClasst() {
        return classt;
    }
}
