package com.android.eazymvp.base.baseimpl.model;

import com.android.httpservice.http.NetUtil;
import com.android.eazymvp.base.baseimpl.presenter.BaseCallback;
import com.android.eazymvp.base.baseimpl.presenter.BasePresenter;
import com.android.eazymvp.netapi.HttpServiceApi;
import com.android.eazymvp.util.log.LogUtil;
import com.google.gson.Gson;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

public abstract class BaseModel<HttpService> {

    private HttpService httpService;//自定义接口

    private HttpServiceApi defHttpService;//默认通用接口

    protected static Map<String, Object> mMemoryCache;

    private ExecutorService executorService;

    private HttpServiceState mHttpServiceState;

    private static Gson mGson;

    {
        //判断是否初始化网络请求接口对象
        if (openHttpService() != null) {
            createHttpService();
        }
    }


    /**
     * openHttpService() 返回一个对象 携带 服务接口 和 服务跟地址
     */
    private final void createHttpService() {
        mHttpServiceState = openHttpService();
        if (mHttpServiceState.httpServiceClass == null) {
            LogUtil.d("httpServiceClass = null,请传入服务接口类");
            return;
        }

        if (mHttpServiceState.baseUrl == null) {
            LogUtil.d("baseUrl = null,请传入服务跟地址");
            return;
        }

        if (!mHttpServiceState.baseUrl.matches("^([http:\\/\\/]|[https:\\/\\/])?[^\\s\\" +
                ".]?[^\\s]+\\" +
                ".[^\\s]+\\/+$")) {
            LogUtil.d("baseUrl 跟地址不正确,请确认传入了正确的跟地址");
            return;
        }

        httpService = NetUtil.getNetUtil().getService(mHttpServiceState.getBaseUrl(),
                mHttpServiceState.getHttpServiceClass());

        defHttpService = NetUtil.getNetUtil().getService(mHttpServiceState.getBaseUrl(), HttpServiceApi.class);
    }

    /**
     * @return 网络请求服务接口对象
     */
    protected final HttpService getHttpService() {
        if (httpService == null) {
            try {
                throw new NullPointerException("服务器接口为空,请确认传入了服务器接口类");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return httpService;
    }

    /**
     * 判断是否启动Http服务
     */
    protected abstract HttpServiceState openHttpService();

    /**
     * observer方法的默认flatMap方法
     *
     * @param provider
     * @param observable 网络请求回的Observable<T>
     * @param callback   P层传入的返回接口
     * @param <T>        网络请求返回时的泛型
     */
    protected final <T extends Response, R> void observer(LifecycleProvider provider,
                                                          Observable<T> observable,
                                                          BaseCallback<R> callback) {
        observer(provider, observable, null, callback, false, null);
    }

    protected final <T extends Response, R> void observer(LifecycleProvider provider,
                                                          Observable<T> observable,
                                                          Function<T, ObservableSource<R>> flatMap,
                                                          BaseCallback<R> callback) {
        observer(provider, observable, flatMap, callback, false, null);
    }

    protected final <T extends Response, R> void cacheObserver(LifecycleProvider provider,
                                                               Observable<T> observable,
                                                               Function<T, ObservableSource<R>> flatMap,
                                                               BaseCallback<R> callback,
                                                               String cacheKey) {
        if (mMemoryCache == null) {
            mMemoryCache = new HashMap<>();
        }
        observer(provider, observable, flatMap, callback, true, cacheKey);
    }

    /**
     * 把处理完成的数据返回为callback指定的类型
     *
     * @param provider
     * @param observable 网络请求回的Observable<T>
     * @param flatMap    定义创建一个Function定义如何处理Observable的数据
     * @param <T>        网络请求返回时的泛型
     * @param <R>        被Function处理完成后的类型
     */
    protected <T extends Response, R> void observer(LifecycleProvider provider,
                                                    Observable<T> observable,
                                                    Function<T, ObservableSource<R>> flatMap,
                                                    BaseCallback<R> baseCallback,
                                                    final boolean isCache, final String cacheKey) {
        //以被观察者调用flatmap处理数据,flatmap是在model层所写的数据处理方式
        Observable<R> rObservable;
        if (flatMap == null) {
            rObservable = observable.flatMap(new Function<T, ObservableSource<R>>() {
                @Override
                public ObservableSource<R> apply(final T t) throws Exception {

                    if (t == null) {
                        return Observable.error(new Exception("请求失败,数据为空"));
                    }
                    if (isCache && cacheKey != null) {
                        getThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                cacheToMemory(t, cacheKey);
                            }
                        });
                    }
                    Object data = t.body();
                    return data == null ?
                            (ObservableSource<R>) Observable.error(new Exception(t.message())) :
                            (ObservableSource<R>) Observable.just(data);
                }
            });
        } else {
            rObservable = observable.flatMap(flatMap);
        }
        if (provider != null) {
            rObservable.compose(rxObservableTransformer())
                    .compose(isLifectcleProviderType(provider))//用于防止Activity关闭时回调造成的内存泄露与空指针
                    //处理完成发送数据到callBack
                    .subscribe(new BaseObserver(baseCallback));
        } else {
            rObservable.compose(rxObservableTransformer())
                    //处理完成发送数据到callBack
                    .subscribe(new BaseObserver(baseCallback));
        }
    }

    /**
     * 获取线程池
     */
    protected final ExecutorService getThreadPool() {
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
     * 用于observble的线程切换功能
     *
     * @param <T>
     * @return
     */
    protected <T> ObservableTransformer<T, T> rxObservableTransformer() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                //把当前的observble的线程转换到io线程请求,把返回结果的线程设置为主线程
                return upstream
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }


    /**
     * 用于Rxjava的防内存泄露
     *
     * @param provider
     * @param <T>
     * @return
     */
    protected <T> LifecycleTransformer<T> isLifectcleProviderType(LifecycleProvider provider) {
        //三元运算符 判断这个类是否为RxFragment的子类或RxFragment本身   如果是则以RxFragment的方式处理
        return (LifecycleTransformer<T>) (provider instanceof RxFragment ?
                //RxFragment的处理方式,把provider对象转换成RxFragment,
                // 调用bindUntilEvent方法设置为在DESTROY,是销毁时取消订阅状态
                ((RxFragment) provider).bindUntilEvent(FragmentEvent.DESTROY) :
                //RxAppCompatActivity的处理方式,也是先转换对象
                //然后调用bindUntilEvent方法设置为在ActivityEvent.DESTROY,是销毁时取消订阅状态
                //还有各种生命周期状态可以选择,ActivityEvent和FragmentEvent的生命周期方法不同
                //所以提供的方法也不同
                ((RxAppCompatActivity) (provider)).bindUntilEvent(ActivityEvent.DESTROY));
    }

    public static Object getMemoryCacheValue(String key) {
        if (mMemoryCache == null) {
            mMemoryCache = new HashMap<>();
        }
        return mMemoryCache.get(key);
    }

    public static void removeMemoryCacheValue(String key) {
        if (mMemoryCache != null) {
            mMemoryCache.remove(key);
        }
    }

    public static void destroyCache() {
        if (mMemoryCache != null) {
            mMemoryCache.clear();
            mMemoryCache = null;
        }
    }

    protected <T extends Response> void cacheToMemory(final T data, final String key) {
        //从集合中获取当前key对应的值
        Object cacheValue = getMemoryCacheValue(key);
        //如果是null说明没有缓存过该对象 直接缓存
        if (cacheValue == null) {
            mMemoryCache.put(key, data.body());
        } else {
            //否则说明该对象已经被缓存过了
            //判断这个对象是不是集合
            if (data.body() instanceof Collection) {
                //是集合   判断2个集合是否值相等如果相同则跳过
                Collection dataList = (Collection) data.body();
                Collection valueList = (Collection) cacheValue;
                valueList.retainAll(dataList);
            } else {
                //不是集合 反射获取这个对象里的集合,和属性判断属性或集合是否相同
                Class<?> aClass = data.body().getClass();
                //遍历属性找到list集合
                for (Field field : aClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        if (field.getType().toString().contains("java.util.List")) {
                            List list = (List) field.get(cacheValue);
                            List collection = (List) field.get(data.body());
                            if (!checkDiffrent(list, collection)) {
                                list.addAll(collection);
                            }
                        } else {
                            Object o1 = field.get(cacheValue);
                            Object o2 = field.get(data.body());
                            if (!o1.equals(o2)) {
                                field.set(cacheValue, o2);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static <T> boolean checkDiffrent(List<T> list, List<T> list1) {
        if (list.size() == list1.size()) {
            int repeatSeveralTimes = 0;
            for (int i = 0 ; i < list.size() ; i++) {
                Field[] declaredFields = list.get(i).getClass().getDeclaredFields();
                for (int j = 0 ; j < declaredFields.length ; j++) {
                    try {
                        declaredFields[j].setAccessible(true);
                        Object o = declaredFields[j].get(list.get(i));
                        Object o1 = declaredFields[j].get(list1.get(i));
                        if (o.equals(o1)) {
                            repeatSeveralTimes++;
                        }
                        if (repeatSeveralTimes > 4) {
                            return true;
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }
        } else {
            return false;
        }
        return false;
    }


    public <T> void requestData(final String url, Map<String, Object> datas,
                                final BaseCallback<T> callback) {
        if (defHttpService != null) {
            defHttpService.requestData(url, datas)//请求数据
                    .subscribeOn(Schedulers.io())//转换到子线程
                    .unsubscribeOn(Schedulers.io())//解除子线程
                    .observeOn(AndroidSchedulers.mainThread())//转换到主线程
                    .subscribe(new Observer<ResponseBody>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            //LogUtil.d("开始请求数据 " + url);
                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            if (callback != null) {
                                if (mGson == null) {
                                    mGson = new Gson();
                                }
                                try {
                                    T t = mGson.fromJson(responseBody.string(),
                                            callback.getClasst());
                                    callback.onCallSuccessful(t);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
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
                            //LogUtil.d("请求完成 " + url);
                        }
                    });
        }
    }

    public <T> void requestDataFile(final String url, Map<String, Object> datas,
                                    MultipartBody.Part file,
                                    final BaseCallback<T> callback) {
        if (defHttpService != null) {
            defHttpService.requestDataFile(url, file, datas)//请求数据
                    .subscribeOn(Schedulers.io())//转换到子线程
                    .unsubscribeOn(Schedulers.io())//解除子线程
                    .observeOn(AndroidSchedulers.mainThread())//转换到主线程
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            //LogUtil.d("开始请求数据 " + url);
                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            if (callback != null) {
                                if (mGson == null) {
                                    mGson = new Gson();
                                }

                                try {
                                    T t = mGson.fromJson(responseBody.string(), callback.getClasst());
                                    callback.onCallSuccessful(t);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
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
                            //LogUtil.d("请求完成 " + url);
                        }
                    });
        }
    }

    public class HttpServiceState {
        private Class<HttpService> httpServiceClass;
        private String baseUrl;

        public Class<HttpService> getHttpServiceClass() {
            return httpServiceClass;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public HttpServiceState(Class<HttpService> aClass, String baseUrl) {
            httpServiceClass = aClass;
            this.baseUrl = baseUrl;
        }
    }
}
