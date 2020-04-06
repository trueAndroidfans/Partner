package com.aokiji.partner.interceptor;


import com.aokiji.partner.utils.AppUtil;
import com.aokiji.partner.utils.SystemUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhangdonghai on 2018/7/2.
 */

public class HeadInterceptor implements Interceptor {

    @Override public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder()
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Accept", "application/json")
                .header("Charset", "UTF-8")
                .header("Client-Type", "Android")
                .header("User-Agent", SystemUtil.getSystemModel())
                .header("Mac", AppUtil.getMacAddr())
                .method(originalRequest.method(), originalRequest.body());
        Request request = builder.build();
        Response originalResponse = chain.proceed(request);
        return originalResponse;
    }

}
