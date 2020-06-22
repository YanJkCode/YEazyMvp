package com.android.eazymvp.base.baseimpl.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;

import com.android.eazymvp.R;
import com.android.eazymvp.base.baseInterface.IBaseChannel;
import com.android.eazymvp.base.baseInterface.IBaseDestroy;
import com.android.eazymvp.base.baseInterface.IBaseFragmentDecorate;
import com.android.eazymvp.base.baseInterface.IBaseGetMateData;
import com.android.eazymvp.base.baseInterface.IBaseLayout;
import com.android.eazymvp.base.baseInterface.IBaseTextFont;
import com.android.eazymvp.base.baseInterface.IBaseToActivity;
import com.android.eazymvp.util.log.LogUtil;
import com.trello.rxlifecycle2.components.support.RxFragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.jessyan.autosize.internal.CustomAdapt;


public abstract class BaseFragment extends RxFragment
        implements IBaseLayout, IBaseToActivity, IBaseFragmentDecorate, CustomAdapt,
        IBaseGetMateData, IBaseTextFont, IBaseDestroy, IBaseChannel {

    protected Unbinder unbinder;
    protected Context mContext;
    private Toast sToast;
    protected View rootView;
    protected BaseActivity mActivity;
    private long lastTime;
    private static final long TargetTime = 1000;

    public BaseFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        destroy = false;
        int resource = initLayout();
        if (resource != 0) {
            rootView = inflater.inflate(resource, container, false);
            unbinder = ButterKnife.bind(this, rootView);
            return rootView;
        } else {
            throw new NullPointerException("请返回布局文件initLayout()");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getActivity() instanceof BaseActivity)
            mActivity = (BaseActivity) getActivity();
    }


    @Override
    public String getChannel() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return "";
        }
        String channel = arguments.getString("channel");
        if (channel == null) {
            return "";
        }
        return channel;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }


    @Override
    public float getSizeInDp() {
        return getMataDataInt("design_width_in_dp");
    }

    public String getMataDataString(String name) {
        ApplicationInfo appInfo = getMetaDataInfo();
        return appInfo.metaData.getString(name);
    }

    /**
     * 获取MetaData 数据
     *
     * @return
     */
    private ApplicationInfo getMetaDataInfo() {
        ApplicationInfo appInfo = null;
        try {
            appInfo =
                    getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(),
                            PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appInfo;
    }

    public float getMataDataFloat(String name) {
        return getMetaDataInfo().metaData.getFloat(name);
    }

    public double getMataDataDouble(String name) {
        return getMetaDataInfo().metaData.getDouble(name);
    }

    public char getMataDataChar(String name) {
        return getMetaDataInfo().metaData.getChar(name);
    }

    public byte getMataDataByte(String name) {
        return getMetaDataInfo().metaData.getByte(name);
    }

    public int getMataDataInt(String name) {
        return getMetaDataInfo().metaData.getInt(name);
    }

    public long getMataDataLong(String name) {
        return getMetaDataInfo().metaData.getLong(name);
    }

    public static void setScaleImage(final Activity activity, final View view, int drawableResId) {

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

    protected void showHint(CharSequence msg) {
        if (sToast == null) {
            View view = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).getView();
            sToast = new Toast(getActivity());
            sToast.setView(view);
        }
        sToast.setText(msg);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.show();
    }

    protected void showHint(@StringRes int resId) {
        if (sToast == null) {
            View view = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).getView();
            sToast = new Toast(getActivity());
            sToast.setView(view);
        }
        sToast.setText(resId);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.show();
    }

    public void showHintCenter(CharSequence msg) {
        if (sToast == null) {
            View view = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).getView();
            sToast = new Toast(getActivity());
            sToast.setView(view);
        }
        sToast.setText(msg);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.setGravity(17, 0, -30);// 居中显示
        sToast.show();
    }

    public void showHintCenter(View view, String msg) {
        if (sToast == null) {
            sToast = new Toast(getActivity());
        }
        sToast.setView(view);
        sToast.setText(msg);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.setGravity(17, 0, -30);// 居中显示
        sToast.show();
    }

    public void showHintCenter(@StringRes int resId) {
        if (sToast == null) {
            View view = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).getView();
            sToast = new Toast(getActivity());
            sToast.setView(view);
        }
        sToast.setText(resId);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.setGravity(17, 0, -30);// 居中显示
        sToast.show();
    }

    public void finish() {
        getActivity().finish();
    }

    public BaseFragment addFragment(FragmentManager manager, Class<? extends BaseFragment> fClass,
                                    int groupID, Bundle args) {
        if (mActivity == null)
            mActivity = (BaseActivity) getActivity();
        return mActivity.addFragment(manager, fClass, groupID, args);
    }

    protected void back() {
        getFragmentManager().popBackStack();
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
    public final void toActivityForResult(Class<?> tClass, int resuleCode, Intent intent) {
        long curTime = System.currentTimeMillis();
        if (curTime - lastTime > TargetTime) {
            lastTime = curTime;
            startActivityForResult(createIntent(tClass, intent), resuleCode);
        }
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

    @Override
    public int enterAnim() {
        if (!isNeedAnimation()) {
            return 0;
        }
        return R.anim.common_page_right_in;
    }

    @Override
    public int exitAnim() {
        if (!isNeedAnimation()) {
            return 0;
        }
        return R.anim.common_page_left_out;
    }

    @Override
    public int popEnterAnim() {
        if (!isNeedAnimation()) {
            return 0;
        }
        return R.anim.common_page_left_in;
    }

    @Override
    public int popExitAnim() {
        if (!isNeedAnimation()) {
            return 0;
        }
        return R.anim.common_page_right_out;
    }

    @Override
    public boolean isNeedAddToBackStack() {
        return true;
    }

    @Override
    public boolean isNeedAnimation() {
        return true;
    }

    protected void initMvp() {
    }

    protected void initView() {
    }

    protected void initData() {
    }

    protected void initListener() {
    }

    public void refresh() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContext = null;
        destroy = true;
        mActivity = null;
        rootView = null;
        unbinder.unbind();
    }

    public void bindOnClick(View.OnClickListener baseOnClick, @IdRes int[] resId) {
        if (baseOnClick == null) {
            throw new NullPointerException("回调接口为空!");
        }

        if (resId == null) {
            throw new NullPointerException("请存入需要设置点击监听的ID");
        }

        for (int id : resId) {
            if (rootView != null) {
                rootView.findViewById(id).setOnClickListener(baseOnClick);
            }
        }
    }

    protected BaseFragment getThis() {
        return this;
    }

    @Override
    public void setTextViewTypeface(TextView view, String FontName) {
        view.setTypeface(Typeface.createFromAsset(view.getContext().getApplicationContext().getAssets(), FontName));
    }

    protected <T extends View> T findViewById(@IdRes int resId) {
        if (resId == -1 || resId == 0) {
            return null;
        }
        if (rootView != null) {
            return rootView.findViewById(resId);
        }
        return null;
    }

    private boolean destroy;

    /**
     * 是否被销毁了
     *
     * @return true被销毁了    false未销毁
     */
    @Override
    public boolean isDestroy() {
        return destroy;
    }
}
