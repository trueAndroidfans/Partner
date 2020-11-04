package com.aokiji.partner;

import android.app.Application;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

/**
 * Created by zhangdonghai on 2018/7/30.
 */

public class App extends Application {

    private static App sApp;

    public static App getInstance()
    {
        return sApp;
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        sApp = this;

        init();
    }


    private void init()
    {
        Logger.addLogAdapter(new AndroidLogAdapter());
    }
}
