package com.android.eazymvp.base.baseimpl.presenter;


import com.android.eazymvp.base.baseInterface.IBaseViewCacheBack;

public abstract class BaseCacheCallback<T> extends BaseCallback<T> {
   public IBaseViewCacheBack<T> mViewCacheBack;

   public BaseCacheCallback() {
   }

   public BaseCacheCallback(IBaseViewCacheBack<T> viewCacheBack) {
      super(viewCacheBack);
      mViewCacheBack = viewCacheBack;
   }

   /**
    * 数据从内存缓存里面读取出来的
    */
   public void onMemoryCacheBack(T value) {
      if (mViewCacheBack != null)
         mViewCacheBack.onMemoryCacheBack(value);
      mViewCacheBack = null;
   }

   /**
    * 数据从 sdcard 中读取出来的
    */
   public void onDiskCacheBack(T value) {
      if (mViewCacheBack != null)
         mViewCacheBack.onDiskCacheBack(value);
      mViewCacheBack = null;
   }
}
