package com.android.eazymvp.base.baseInterface;

public interface IBaseViewCacheBack<T> extends IBaseViewBack<T> {
   /**
    * 数据从内存缓存里读取的回调
    */
   void onMemoryCacheBack(T value);

   /**
    * 数据从sdcard中读读取的回调
    */
   void onDiskCacheBack(T value);
}
