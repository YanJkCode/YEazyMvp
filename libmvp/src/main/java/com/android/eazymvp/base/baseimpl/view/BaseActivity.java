package com.android.eazymvp.base.baseimpl.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.eazymvp.base.baseInterface.IBaseContext;
import com.android.eazymvp.base.baseInterface.IBaseDestroy;
import com.android.eazymvp.base.baseInterface.IBaseFragmentAddManeage;
import com.android.eazymvp.base.baseInterface.IBaseGetMateData;
import com.android.eazymvp.base.baseInterface.IBaseLayout;
import com.android.eazymvp.base.baseInterface.IBaseTextFont;
import com.android.eazymvp.base.baseInterface.IBaseToActivity;
import com.android.eazymvp.listener.ActivityLifecycleListener;
import com.android.eazymvp.util.log.LogUtil;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.internal.CustomAdapt;

public abstract class BaseActivity extends RxAppCompatActivity
        implements IBaseContext, IBaseLayout, IBaseToActivity,
        IBaseFragmentAddManeage,
        CustomAdapt, IBaseGetMateData, IBaseTextFont, IBaseDestroy {

    private boolean destroy;
    /**
     * ButterKnife解绑对象
     */
    private Unbinder mBind;

    /**
     * 提示对象
     */
    private Toast sToast;

    /**
     * 上一次跳转Activity的时间
     */
    protected static long lastTime;

    /**
     * 必须大于这个时间才可以再次跳转 防止连续跳转
     */
    protected static final long TargetTime = 1000;

    /**
     * 屏幕旋转的状态
     */
    public static final int PORTRAIT = 1;
    public static final int LANDSCAPE = 2;

    /**
     * 屏幕是否可以旋转
     */
    private static int isActivityOrientationMode = PORTRAIT;


    /**
     * 是否开启AutoSize屏幕适配 默认开启
     */
    private static boolean isOpenAutoSize = true;

    /**
     * 点击事件判断 可以在点击事件之前调用isClickTime()
     */
    private HashMap<Integer, Long> clickTimes = new HashMap<>();

    /**
     * 常驻handler
     */
    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            BaseActivity.this.handleMessage(msg);
        }
    };


    /**
     * handler回调
     *
     * @param msg
     */
    protected void handleMessage(@NonNull Message msg) {

    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            clickTimes.clear();
        }
    };

    /**
     * 点击事件 时间间隔判断
     *
     * @param id 被点击的控件ID
     * @return
     */
    public boolean isClickTime(int id) {
        long curTime = System.currentTimeMillis();
        Long aLong = clickTimes.get(id);
        if (aLong == null || curTime - aLong > TargetTime) {
            clickTimes.put(id, curTime);
            handler.postDelayed(mRunnable, 1000);
            return false;
        }
        return true;
    }

    protected BaseActivity getThis() {
        return this;
    }

    /**
     * 点击事件 时间间隔判断
     *
     * @param v 被点击的控件
     * @return
     */
    public boolean isClickTime(View v) {
        long curTime = System.currentTimeMillis();
        Long aLong = clickTimes.get(v.getId());
        if (aLong == 0 || curTime - aLong > TargetTime) {
            clickTimes.put(v.getId(), curTime);
            handler.postDelayed(mRunnable, 1000);
            return false;
        }
        return true;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PORTRAIT, LANDSCAPE})
    @interface ActivityMode {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (isOpenAutoSize) {
            setAutoSize(AutoSizeConfig.getInstance());//启动AutoSize
        }
        super.onCreate(savedInstanceState);
        if (!isShowTitle()) {//是否显示Title  默认不开启
            //无title
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        if (isShowFullScreen()) {//是否开启显示全屏 默认不开启
            //全屏
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (isOpenOrientation()) {//设置为屏幕无法旋转
            switch (isActivityOrientationMode) {
                case PORTRAIT:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case LANDSCAPE:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
            }
        }

        ///**
        // * 更改弹窗类型
        // */
        //WindowManager.LayoutParams params = getWindow().getAttributes();
        //params.width = WindowManager.LayoutParams.MATCH_PARENT;
        //params.height = WindowManager.LayoutParams.MATCH_PARENT;
        //params.gravity = Gravity.CENTER;
        //params.type = WindowManager.LayoutParams.TYPE_TOAST;
        //getWindow().setAttributes(params);


        addWindowMode();//添加窗口状态
        int layoutResID = initLayout();
        if (layoutResID != 0) {
            setContentView(layoutResID);//设置布局文件
        } else {
            throw new NullPointerException("请返回布局文件initLayout()");
        }
        if (!destroy) {
            //使用Btterknife需要添加
            //annotationProcessor 'com.jakewharton:butterknife-compiler:10.1.0' 才有效
            View sourceView = getWindow().getDecorView();
            mBind = ButterKnife.bind(this, sourceView);

            //PushAgent.getInstance(this).onAppStart();//友盟统计用 如果没有使用友盟这个代码可以不用解开注释
        }

        if (destroy) {
            return;
        }
        initMvp();//初始化Mvp
        if (destroy) {
            return;
        }
        initView();//初始化控件 数据
        if (destroy) {
            return;
        }
        initData();//初始化 网络数据
        if (destroy) {
            return;
        }
        initListener();//初始化 控件监听

        if (mActivityLifecycleListener != null)
            mActivityLifecycleListener.onActivityCreated(this, savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (mActivityLifecycleListener != null)
            mActivityLifecycleListener.onActivityResumed(this);
        super.onResume();
        //MobclickAgent.onResume(this);//友盟统计用 如果没有使用友盟这个代码可以不用解开注释
    }

    @Override
    protected void onPause() {
        if (mActivityLifecycleListener != null)
            mActivityLifecycleListener.onActivityPaused(this);
        super.onPause();
        //MobclickAgent.onPause(this);//友盟统计用 如果没有使用友盟这个代码可以不用解开注释
    }

    @Override
    protected void onDestroy() {
        if (mActivityLifecycleListener != null)
            mActivityLifecycleListener.onActivityDestroyed(this);
        destroy = true;
        if (mBind != null) {
            mBind.unbind();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        if (mActivityLifecycleListener != null)
            mActivityLifecycleListener.onActivityStart(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mActivityLifecycleListener != null)
            mActivityLifecycleListener.onActivityStopped(this);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (mActivityLifecycleListener != null)
            mActivityLifecycleListener.onActivitySaveInstanceState(this, outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * 缩放指定资源适配当前宽高
     *
     * @param activity
     * @param view
     * @param drawableResId
     */
    public static void setScaleImage(final Activity activity, final View view,
                                     int drawableResId) {

        // 获取屏幕的高宽
        Point outSize = new Point();
        activity.getWindow().getWindowManager().getDefaultDisplay().getSize(outSize);
        // 解析将要被处理的图片
        Bitmap resourceBitmap;
        if (drawableResId != 0) {
            resourceBitmap = BitmapFactory.decodeResource(activity.getResources(),
                    drawableResId);
        } else {
            resourceBitmap = ((BitmapDrawable) view.getBackground()).getBitmap();
        }

        if (resourceBitmap == null) {
            return;
        }
        // 开始对图片进行拉伸或者缩放

        // 使用图片的缩放比例计算将要放大的图片的高度
        int bitmapScaledHeight =
                Math.round(resourceBitmap.getHeight() * outSize.x * 1.0f / resourceBitmap.getWidth());

        // 以屏幕的宽度为基准，如果图片的宽度比屏幕宽，则等比缩小，如果窄，则放大
        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(resourceBitmap, outSize.x,
                bitmapScaledHeight, false);

        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                //这里防止图像的重复创建，避免申请不必要的内存空间
                if (scaledBitmap.isRecycled())
                    //必须返回true
                    return true;


                // 当UI绘制完毕，我们对图片进行处理
                int viewHeight = view.getMeasuredHeight();


                // 计算将要裁剪的图片的顶部以及底部的偏移量
                int offset = (scaledBitmap.getHeight() - viewHeight) / 2;


                // 对图片以中心进行裁剪，裁剪出的图片就是非常适合做引导页的图片了
                Bitmap finallyBitmap = Bitmap.createBitmap(scaledBitmap, 0, offset,
                        scaledBitmap.getWidth(),
                        scaledBitmap.getHeight() - offset * 2);


                if (!finallyBitmap.equals(scaledBitmap)) {//如果返回的不是原图，则对原图进行回收
                    scaledBitmap.recycle();
                    System.gc();
                }


                // 设置图片显示
                view.setBackgroundDrawable(new BitmapDrawable(activity.getResources(),
                        finallyBitmap));
                return true;
            }
        });
    }

    /**
     * 是否开启 autoSize  true 开启   false 关闭
     * 默认开启
     *
     * @param isOpenAutoSize
     */
    public static final void setOpenAutoSize(boolean isOpenAutoSize) {
        BaseActivity.isOpenAutoSize = isOpenAutoSize;
    }

    /**
     * AutoSize 初始化 可以重写 后自定义需要的功能 默认功能为 使用完整设备尺寸(包括系统状态栏)   并且兼容fragment的自定义Size
     *
     * @param instance
     */
    public void setAutoSize(AutoSizeConfig instance) {
        instance.setUseDeviceSize(true).setCustomFragment(true);//使用设备的完整尺寸
    }

    /**
     * 判断当前是按照 宽还是高进行适配  返回True是以宽适配   false是以高适配
     *
     * @return
     */
    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    /**
     * 返回你设计图的宽或者高  默认会去清单获取 meta-data下的design_width_in_dp
     *
     * @return
     */
    @Override
    public float getSizeInDp() {
        return getMataDataInt("design_width_in_dp");
    }


    /**
     * 是否开启旋转屏幕
     * 默认开启
     *
     * @return true 开启   false 关闭
     */
    protected boolean isOpenOrientation() {
        return true;
    }

    /**
     * 设置旋转屏幕方式
     *
     * @param OrientationMode
     */
    public static void setIsActivityOrientationMode(@ActivityMode int OrientationMode) {
        isActivityOrientationMode = OrientationMode;
    }

    protected void showHint(CharSequence msg) {
        if (sToast == null) {
            View view = Toast.makeText(this, "", Toast.LENGTH_SHORT).getView();
            sToast = new Toast(this);
            sToast.setView(view);
        }
        sToast.setText(msg);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.show();
    }

    protected void showHint(@StringRes int resId) {
        if (sToast == null) {
            View view = Toast.makeText(this, "", Toast.LENGTH_SHORT).getView();
            sToast = new Toast(this);
            sToast.setView(view);
        }
        sToast.setText(resId);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.show();
    }

    public void showHintCenter(CharSequence msg) {
        if (sToast == null) {
            View view = Toast.makeText(this, "", Toast.LENGTH_SHORT).getView();
            sToast = new Toast(this);
            sToast.setView(view);
        }
        sToast.setText(msg);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.setGravity(17, 0, -30);// 居中显示
        sToast.show();
    }

    public void showHintCenter(View view, String msg) {
        if (sToast == null) {
            sToast = new Toast(this);
        }
        sToast.setView(view);
        sToast.setText(msg);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.setGravity(17, 0, -30);// 居中显示
        sToast.show();
    }

    public void showHintCenter(@StringRes int resId) {
        if (sToast == null) {
            View view = Toast.makeText(this, "", Toast.LENGTH_SHORT).getView();
            sToast = new Toast(this);
            sToast.setView(view);
        }
        sToast.setText(resId);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.setGravity(17, 0, -30);// 居中显示
        sToast.show();
    }

    /**
     * 可以用于重写给window添加状态
     */
    protected void addWindowMode() {
    }

    /**
     * 是否全屏 默认false不全屏
     */
    protected boolean isShowFullScreen() {
        return false;
    }

    /**
     * 是否显示title 默认false不显示
     */
    protected boolean isShowTitle() {
        return false;
    }

    @Override
    public final void toActivity(Class<?> tClass) {
        toActivity(tClass, null);
    }

    @Override
    public final void toActivity(Class<?> tClass, Intent intent) {
        long curTime = System.currentTimeMillis();
        if (curTime - lastTime > TargetTime) {
            lastTime = curTime;
            startActivity(createIntent(tClass, intent));
        }
    }

    @Override
    public final void toActivityForResult(Class<?> tClass, int requestCode, Intent intent) {
        long curTime = System.currentTimeMillis();
        if (curTime - lastTime > TargetTime) {
            lastTime = curTime;
            startActivityForResult(createIntent(tClass, intent), requestCode);
        }
    }

    public void toLoginActivity() {
    }

    /**
     * 跳转到指定包名的activity
     *
     * @param classPath 包地址
     */
    public void toClassPathActivity(String classPath) {
        try {
            toActivity(Class.forName(classPath), null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LogUtil.e("包地址错误");
        }
    }

    /**
     * 跳转到指定包名的activity
     *
     * @param classPath 包地址
     * @param intent    intent
     */
    public void toClassPathActivity(String classPath, Intent intent) {
        try {
            toActivity(Class.forName(classPath), intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到指定包名的activity
     *
     * @param classPath 包地址
     * @param hintLog   log日志
     */
    public void toClassPathActivity(String classPath, String hintLog) {
        try {
            toActivity(Class.forName(classPath), null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LogUtil.e(hintLog);
        }
    }

    /**
     * 跳转到指定包名的activity
     *
     * @param classPath 包地址
     * @param hintLog   log日志
     * @param intent    intent
     */
    public void toClassPathActivity(String classPath, String hintLog, Intent intent) {
        try {
            toActivity(Class.forName(classPath), intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LogUtil.e(hintLog);
        }
    }

    public void refresh() {
        initData();
    }


    private Intent createIntent(Class<?> tClass, Intent intent) {
        Intent start;
        if (intent != null) {
            intent.setClass(getContext(), tClass);
            start = intent;
        } else {
            start = new Intent(getContext(), tClass);
        }
        return start;
    }


    protected final boolean applyPermission(String[] permissions) {
        return applyPermission(permissions, 0);
    }

    protected final boolean applyPermission(String[] permissions, int requestCode) {
        ArrayList<String> permissionsList = new ArrayList<>();//创建一个集合用于存储权限数据
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);//如果没有这个权限则 则申请这个权限
            }
        }
        //申请权限 判断是否需要去申请权限
        if (permissionsList.size() != 0) {
            //需要申请权限 创建一个数组把集合数据转移到数组中
            String[] stringPermissions = new String[permissionsList.size()];
            for (int i = 0 ; i < permissionsList.size() ; i++) {
                stringPermissions[i] = permissionsList.get(i);
            }
            //因为toArry返回的是Object[]  不能转换为String[] 所以只能手动转移数据了
            ActivityCompat.requestPermissions(this, stringPermissions, requestCode);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public <T extends BaseFragment> T addFragment(FragmentManager manager, Class<T> fClass,
                                                  int groupID, Bundle args) {
        String tag = fClass.getName();
        Fragment fragment = manager.findFragmentByTag(tag);
        T baseFragment = null;
        FragmentTransaction transaction = manager.beginTransaction();
        if (fragment != null) {
            baseFragment = (T) fragment;
            if (baseFragment.isAdded()) {
                if (baseFragment.isHidden()) {
                    transaction.show(baseFragment);
                }
            } else {
                transaction.add(groupID, baseFragment, tag);
            }
        } else {
            if (fClass != null) {
                try {
                    baseFragment = fClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (baseFragment == null) {
                    throw new UnsupportedOperationException(tag + " Fragment必须提供无参构造方法");
                }
                isFragmentState(tag, baseFragment, transaction, manager.getFragments().size());
                if (baseFragment != null)
                    transaction.add(groupID, baseFragment, tag);
            }
        }
        if (baseFragment != null)
            hideAllFragment(manager, transaction, baseFragment);
        if (args != null)
            baseFragment.setArguments(args);
        transaction.commit();
        return baseFragment;
    }

    private void isFragmentState(String tag, BaseFragment baseFragment,
                                 FragmentTransaction transaction, int fragmrntSize) {
        if (baseFragment.isNeedAnimation())
            transaction.setCustomAnimations(
                    baseFragment.enterAnim(),
                    baseFragment.exitAnim(),
                    baseFragment.popEnterAnim(),
                    baseFragment.popExitAnim());
        if (baseFragment.isNeedAddToBackStack() && fragmrntSize != 0)
            transaction.addToBackStack(tag);
    }

    @Override
    public void hideAllFragment(FragmentManager manager, FragmentTransaction transaction,
                                Fragment curFragment) {
        List<Fragment> fragments = manager.getFragments();
        for (Fragment fragment : fragments)
            if (curFragment != fragment && !fragment.isHidden())
                transaction.hide(fragment);
    }

    @Override
    public Context getContext() {
        return this;
    }

    protected void initMvp() {
    }

    protected void initView() {
    }

    protected void initData() {
    }

    protected void initListener() {
    }


    private long firstTime = 0;
    private boolean isBackState;

    public void setBackState(boolean backState) {
        isBackState = backState;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isBackState) {
            long secondTime = System.currentTimeMillis();
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (secondTime - firstTime < 2000) {
                    System.exit(0);
                } else {
                    showHint("再按一次退出程序");
                    firstTime = System.currentTimeMillis();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void bindOnClick(View.OnClickListener baseOnClick, @IdRes int[] resId) {
        if (baseOnClick == null) {
            throw new NullPointerException("回调接口为空!");
        }

        if (resId == null) {
            throw new NullPointerException("请存入需要设置点击监听的ID");
        }

        for (int id : resId) {
            View view = this.findViewById(id);
            if (view != null) {
                view.setOnClickListener(baseOnClick);
            } else {
                throw new NullPointerException("未找到 目标ID:" + id);
            }

        }
    }

    /**
     * 获取MetaData 数据
     *
     * @return
     */
    private ApplicationInfo getMetaDataInfo() {
        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appInfo;
    }

    @Override
    public String getMataDataString(String name) {
        ApplicationInfo appInfo = getMetaDataInfo();
        return appInfo.metaData.getString(name);
    }

    @Override
    public float getMataDataFloat(String name) {
        return getMetaDataInfo().metaData.getFloat(name);
    }

    @Override
    public double getMataDataDouble(String name) {
        return getMetaDataInfo().metaData.getDouble(name);
    }

    @Override
    public char getMataDataChar(String name) {
        return getMetaDataInfo().metaData.getChar(name);
    }

    @Override
    public byte getMataDataByte(String name) {
        return getMetaDataInfo().metaData.getByte(name);
    }

    @Override
    public int getMataDataInt(String name) {
        return getMetaDataInfo().metaData.getInt(name);
    }

    @Override
    public long getMataDataLong(String name) {
        return getMetaDataInfo().metaData.getLong(name);
    }

    @Override
    public void setTextViewTypeface(TextView view, String FontName) {
        view.setTypeface(Typeface.createFromAsset(view.getContext().getApplicationContext().getAssets(), FontName));
    }

    /**
     * 是否被销毁了
     *
     * @return true被销毁了    false未销毁
     */
    @Override
    public boolean isDestroy() {
        return destroy;
    }

    private ActivityLifecycleListener mActivityLifecycleListener;

    public void setActivityLifecycleListener(ActivityLifecycleListener activityLifecycleListener) {
        mActivityLifecycleListener = activityLifecycleListener;
    }


}
