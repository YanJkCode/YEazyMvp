package com.android.eazymvp.base.baseInterface;


public interface IBaseView<T extends IBasePresenter> {
   T createPresenter();
}
