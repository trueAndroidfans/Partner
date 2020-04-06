package com.aokiji.partner.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.aokiji.partner.utils.ActivityController;

/**
 * Created by zhangdonghai on 2018/7/31.
 */

public class BaseActivity extends AppCompatActivity {

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityController.addActivity(this);
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        ActivityController.removeActivity(this);
    }


    public void onFinish(View view) {
        finish();
    }

}
