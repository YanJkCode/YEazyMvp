package com.android.eazymvp.base.baseimpl.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

import com.android.eazymvp.listener.ActivityLifecycleListener;

import butterknife.ButterKnife;

public abstract class BaseViewHolder {
    protected View itemView;
    protected static UIHandler handler = new UIHandler();
    protected int position;
    private ActivityLifecycleListener activityLifecycleListener = new ActivityLifecycleListener() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            super.onActivityCreated(activity, savedInstanceState);
            LoadData();
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            super.onActivityDestroyed(activity);
            onDestroy();
        }
    };

    public BaseViewHolder(View view) {
        this(view, -1);
    }

    public BaseViewHolder(View view, int position) {
        ButterKnife.bind(this, view);
        itemView = view;
        this.position = position;
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).setActivityLifecycleListener(activityLifecycleListener);
        }
    }

    protected Context getContext() {
        return itemView.getContext();
    }

    public int getBaseViewPosition() {
        return position;
    }

    protected void showHint(String msg) {
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).showHint(msg);
        }
    }

    protected void showHint(@StringRes int resId) {
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).showHint(resId);
        }
    }

    public void showHintCenter(View view, String msg) {
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).showHintCenter(view, msg);
        }
    }

    public void showHintCenter(@StringRes int resId) {
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).showHintCenter(resId);
        }
    }

    public void showHintCenter(CharSequence msg) {
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).showHintCenter(msg);
        }
    }

    public final void LoadData() {
        initMvp();
        initView();
        initData();
        initListener();
    }

    protected void initMvp() {
    }

    protected void initView() {
    }

    protected void initData() {
    }

    public void upData() {
    }


    protected void initListener() {
    }


    protected void onDestroy() {

    }


    public void startAnimation() {
    }

    public String getString(@StringRes int resId, Object... formatArgs) {
        return getContext().getString(resId, formatArgs);
    }

    public String getString(@StringRes int resId) {
        return getContext().getString(resId);
    }


    public static class UIHandler extends Handler {
    }

    protected void toActivity(final Class<? extends Activity> aClass, final Intent intent) {
        Context context = itemView.getContext();
        if (context instanceof BaseActivity) {
            ((BaseActivity) context).toActivity(aClass, intent);
        }
    }


    protected void toActivityForResult(final Class<? extends Activity> aClass,
                                       final int requestCode,
                                       final Intent intent) {
        Context context = itemView.getContext();
        if (context instanceof BaseActivity) {
            ((BaseActivity) context).toActivityForResult(aClass, requestCode, intent);
        }
    }

    protected void toActivityListener(final Class<? extends Activity> aClass, final Intent intent) {
        if (itemView != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = itemView.getContext();
                    if (context instanceof BaseActivity) {
                        ((BaseActivity) context).toActivity(aClass, intent);
                    }
                }
            });
        }
    }

    protected void toActivityForResultListener(final Class<? extends Activity> aClass,
                                               final int requestCode,
                                               final Intent intent) {
        if (itemView != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = itemView.getContext();
                    if (context instanceof BaseActivity) {
                        ((BaseActivity) context).toActivityForResult(aClass, requestCode, intent);
                    }
                }
            });
        }
    }

    public void bindOnClick(View.OnClickListener baseOnClick, @IdRes int[] resId) {
        if (baseOnClick == null) {
            throw new NullPointerException("回调接口为空!");
        }

        if (resId == null) {
            throw new NullPointerException("请存入需要设置点击监听的ID");
        }

        for (int id : resId) {
            itemView.findViewById(id).setOnClickListener(baseOnClick);
        }
    }
}
