package com.android.eazymvp.base.baseInterface;

public interface IBaseViewBack<T> {

   void onSuccessful(T t, int responseType);

   void onFailed(String msg);
}
