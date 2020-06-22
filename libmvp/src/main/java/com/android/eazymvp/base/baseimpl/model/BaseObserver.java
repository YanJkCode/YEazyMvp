package com.android.eazymvp.base.baseimpl.model;


import com.android.eazymvp.base.baseimpl.presenter.BaseCallback;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class BaseObserver<T> implements Observer<T> {

   private BaseCallback<T> callback;
   private Disposable mDisposable;

   public BaseObserver(BaseCallback<T> callback) {
      this.callback = callback;
   }

   @Override
   public void onSubscribe(Disposable d) {
      mDisposable = d;
   }

   @Override
   public void onNext(T t) {
      if (callback != null) {
         callback.onCallSuccessful(t);
      }
   }

   @Override
   public void onError(Throwable e) {
      if (callback != null) {
         callback.onCallFailed(e);
      }
   }

   @Override
   public void onComplete() {
      if (mDisposable != null) {
         mDisposable.dispose();
         mDisposable = null;
      }
      if (callback != null) {
         callback = null;
      }
   }
}
