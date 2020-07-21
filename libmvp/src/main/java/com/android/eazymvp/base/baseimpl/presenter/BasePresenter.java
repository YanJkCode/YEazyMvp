package com.android.eazymvp.base.baseimpl.presenter;

import com.android.eazymvp.base.baseInterface.IBaseDestroy;
import com.android.eazymvp.base.baseInterface.IBaseModel;
import com.android.eazymvp.base.baseInterface.IBasePresenter;
import com.android.eazymvp.base.baseInterface.IBaseView;
import com.android.eazymvp.base.baseimpl.model.BaseModel;
import com.android.eazymvp.util.log.LogUtil;
import com.trello.rxlifecycle2.LifecycleProvider;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.disposables.Disposable;
import okhttp3.MultipartBody;

public abstract class BasePresenter<V extends IBaseView, M extends BaseModel>
        implements IBasePresenter<V>, IBaseModel<M> {

    protected V mView;
    private WeakReference<M> mModel = new WeakReference<>(initModel());
    private static ExecutorService executorService;
    private static LinkedList<Map<String, Object>> sHashMaps;
    private Disposable mDisposable;
    private static BasePresenter mBasePresenter;
    //private int mapMax = 5;

    public BasePresenter() {
        if (sHashMaps == null) {
            sHashMaps = new LinkedList<>();
        }
    }

    public static BasePresenter getBasePresenter() {
        if (mBasePresenter != null) {
            return mBasePresenter;
        } else {
            LogUtil.e("请初始化 BasePresenter  BasePresenter.initBasePresneter(BaseModel)");
            throw new NullPointerException("请初始化 BasePresenter  BasePresenter.initBasePresneter(BaseModel)");
        }
    }

    public static BasePresenter getBasePresenter(BaseModel baseModel) {
        return mBasePresenter != null ? mBasePresenter : initBasePresneter(baseModel);
    }

    /**
     * 初始化 网络服务.请在之前添加好所需的拦截器.
     *
     * @param baseModel
     * @return
     */
    public static BasePresenter initBasePresneter(final BaseModel baseModel) {
        return mBasePresenter != null ? mBasePresenter : (mBasePresenter = new BasePresenter() {
            @Override
            public BaseModel initModel() {
                return baseModel;
            }
        });
    }

    /**
     * 获取V层对象 在创建创建MVP时就调用了该方法
     */
    @Override
    public void attachView(V v) {
        this.mView = v;
        //绑定状态设置为 是 这时候requestData才可以返回值
    }

    /**
     * 把绑定的view转换成LifecycleProvider 发送到M层用
     */
    protected final LifecycleProvider getLifecycleProvider() {
        if (mView == null) {
            return null;
        }
        return (LifecycleProvider) mView;
    }


    /**
     * 获取线程池
     */
    public final ExecutorService getThreadPool() {
        if (executorService == null) {
            synchronized (BasePresenter.class) {
                if (executorService == null) {
                    executorService = Executors.newCachedThreadPool();
                }
            }
        }
        return executorService;
    }

    /**
     * 获取的参数集合在请求完数据后会被清空
     *
     * @return
     */
    @Override
    public HashMap<String, Object> getHashMap() {
        //if (sHashMaps.size() > 0) {
        //    return (Map<String, E>) sHashMaps.removeFirst();
        //} else {
        return new HashMap<String, Object>();
        //}
    }

    public void recycleHashMap(Map<String, Object> map) {
        if (map != null) {
            map.clear();
            //sHashMaps.add(map);
            //if (sHashMaps.size() > mapMax) {
            //    for (int i = 0 ; i < sHashMaps.size() - 5 ; i++) {
            //        Map<String, Object> stringObjectMap = sHashMaps.removeLast();
            //        stringObjectMap = null;
            //    }
            //}
        }
    }

    @Override
    public void detachView() {
        //销毁了则置空线程池
        if (executorService != null) {
            executorService = null;
        }
        //view置空
        if (mView != null) {
            mView = null;
        }
    }

    @Override
    public void detachTemporaryDetach() {

    }

    public final M getModel() {
        return mModel.get();
    }

    @Override
    public void cacheRequest(Disposable disposable) {
        mDisposable = disposable;
    }

    @Override
    public void cancelRequest() {
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
    }

    @Override
    public void requestData(final IBaseDestroy iBaseDestroy, String url) {
        getModel().requestData(url, getHashMap(), null);
    }

    @Override
    public void requestData(final IBaseDestroy iBaseDestroy, String url,
                            final HashMap<String, Object> datas) {
        getModel().requestData(url, datas, null);
    }

    @Override
    public <T> void requestData(final IBaseDestroy iBaseDestroy, String url,
                                final BaseDefViewBack<T> iBaseDefViewBack) {
        final Map<String, Object> datas = getHashMap();
        getModel().requestData(url, datas, new BaseCallback<T>(iBaseDefViewBack) {
            @Override
            public void onCallSuccessful(T value) {
                if (iBaseDefViewBack != null) {
                    if (iBaseDestroy == null) {
                        return;
                    }

                    if (iBaseDestroy.isDestroy()) {
                        return;
                    }
                    iBaseDefViewBack.onSuccessful(value);
                }
                recycleHashMap(datas);
            }

            @Override
            public <M extends Throwable> void onCallFailed(M msg) {
                if (iBaseDefViewBack != null) {
                    if (iBaseDestroy == null) {
                        return;
                    }

                    if (iBaseDestroy.isDestroy()) {
                        return;
                    }
                    iBaseDefViewBack.onFailed(msg.getMessage());
                }
                recycleHashMap(datas);
            }
        });
    }

    @Override
    public <T> void requestData(final IBaseDestroy iBaseDestroy, String url,
                                final HashMap<String, Object> datas,
                                final BaseDefViewBack<T> iBaseDefViewBack) {
        getModel().requestData(url, datas, new BaseCallback<T>(iBaseDefViewBack) {
            @Override
            public void onCallSuccessful(T value) {
                if (iBaseDefViewBack != null) {
                    if (iBaseDestroy == null) {
                        return;
                    }
                    if (iBaseDestroy.isDestroy()) {
                        return;
                    }
                    iBaseDefViewBack.onSuccessful(value);
                }
                recycleHashMap(datas);
            }

            @Override
            public <M extends Throwable> void onCallFailed(M msg) {
                if (iBaseDefViewBack != null) {
                    if (iBaseDestroy == null) {
                        return;
                    }

                    if (iBaseDestroy.isDestroy()) {
                        return;
                    }
                    iBaseDefViewBack.onFailed(msg.getMessage());
                }
                recycleHashMap(datas);
            }
        });
    }

    @Override
    public <T> void requestDataFile(final IBaseDestroy iBaseDestroy, String url,
                                    final HashMap<String, Object> datas,
                                    MultipartBody.Part file,
                                    final BaseDefViewBack<T> iBaseDefViewBack) {
        getModel().requestDataFile(url, datas, file, new BaseCallback<T>(iBaseDefViewBack) {
            @Override
            public void onCallSuccessful(T value) {
                if (iBaseDefViewBack != null) {
                    if (iBaseDestroy == null) {
                        return;
                    }

                    if (iBaseDestroy.isDestroy()) {
                        return;
                    }
                    iBaseDefViewBack.onSuccessful(value);
                }
                recycleHashMap(datas);
            }

            @Override
            public <M extends Throwable> void onCallFailed(M msg) {
                if (iBaseDefViewBack != null) {
                    if (iBaseDestroy == null) {
                        return;
                    }

                    if (iBaseDestroy.isDestroy()) {
                        return;
                    }
                    iBaseDefViewBack.onFailed(msg.getMessage());
                }
                recycleHashMap(datas);
            }
        });
    }
}
