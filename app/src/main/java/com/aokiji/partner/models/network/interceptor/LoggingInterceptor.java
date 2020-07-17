package com.aokiji.partner.models.network.interceptor;

import com.aokiji.partner.BuildConfig;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by zhangdonghai on 2018/7/9.
 */

public class LoggingInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody requestBody = request.body();
        String requestStr = "";
        if (requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            requestStr = buffer.readString(charset);
        }
        if (BuildConfig.DEBUG) {
            Logger.e("发送请求:\nMethod: %s\nUrl: %s\nHeaders: %sBody: %s",
                    request.method(), request.url(), request.headers(), requestStr);
        }

        long startMs = System.nanoTime();
        Response response = chain.proceed(request);
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startMs);

        ResponseBody responseBody = response.body();
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        Buffer buffer = source.buffer();
        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            try {
                charset = contentType.charset(UTF8);
            } catch (UnsupportedCharsetException e) {
                e.printStackTrace();
            }
        }
        String responseStr = buffer.clone().readString(charset);
        if (BuildConfig.DEBUG) {
            Logger.e("收到响应:\n%s %s %sms\nUrl: %s\nRequestBody: %s\nResponseBody: %s",
                    response.code(), response.message(), tookMs, response.request().url(), requestStr, responseStr);
        }
        return response;
    }

}
