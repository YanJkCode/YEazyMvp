package com.android.eazymvp.base.baseInterface;


public interface IBaseFragmentDecorate {
   /**
    * 入场动画
    */
   int enterAnim();

   /**
    * 退出动画
    */
   int exitAnim();

   /**
    * 再次入场动画
    */
   int popEnterAnim();

   /**
    * 再次退出动画
    */
   int popExitAnim();

   /**
    * 是否加入回退栈
    */
   boolean isNeedAddToBackStack();

   /**
    * 是否播放动画
    */
   boolean isNeedAnimation();
}
