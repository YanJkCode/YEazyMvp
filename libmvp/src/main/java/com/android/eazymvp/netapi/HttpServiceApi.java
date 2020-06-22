package com.android.eazymvp.netapi;


import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface HttpServiceApi {
    @POST
    Observable<ResponseBody> requestData(@Url String url,
                                         @QueryMap Map<String, Object> params);

    @Multipart
    @POST
    Observable<ResponseBody> requestDataFile(@Url String url,
                                             @Part MultipartBody.Part file,
                                             @QueryMap Map<String, Object> params);
}


