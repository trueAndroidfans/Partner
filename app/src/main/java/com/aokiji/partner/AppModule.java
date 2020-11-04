package com.aokiji.partner;

import android.content.Context;

import com.aokiji.partner.models.network.api.Api;
import com.aokiji.partner.models.network.interceptor.HeadInterceptor;
import com.aokiji.partner.models.network.interceptor.LoggingInterceptor;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zhangdonghai on 2018/7/31.
 */

@Module
public class AppModule {

    private Context mContext;


    public AppModule(Context mContext)
    {
        this.mContext = mContext;
    }


    @Provides
    public Context provideContext()
    {
        return mContext;
    }


    @Provides
    @Singleton
    public Api provideApi()
    {
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HeadInterceptor())
                .addInterceptor(new LoggingInterceptor())
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Settings.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(Api.class);
    }

}
