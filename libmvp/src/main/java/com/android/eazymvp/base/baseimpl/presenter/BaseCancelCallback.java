package com.android.eazymvp.base.baseimpl.presenter;

import com.android.eazymvp.base.baseInterface.IBaseViewBack;

import io.reactivex.disposables.Disposable;

public abstract class BaseCancelCallback<T> extends BaseCallback<T> {

   protected Disposable mDisposable;

   public BaseCancelCallback() {
   }

   public BaseCancelCallback(IBaseViewBack<T> viewBack) {
      super(viewBack);
   }

   void onStart(Disposable disposable) {
      mDisposable = disposable;
   }
}
