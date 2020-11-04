package com.aokiji.partner.modules.setting;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aokiji.partner.R;
import com.aokiji.partner.base.BaseActivity;
import com.aokiji.partner.event.CleanMsgEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {


    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.rl_clear)
    RelativeLayout rlClear;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        initView();
    }


    @OnClick(R.id.rl_clear)
    void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.rl_clear:
                new AlertDialog.Builder(SettingActivity.this)
                        .setMessage("确定要清除聊天记录吗?")
                        .setNegativeButton("取消", (dialogInterface, i) -> dialogInterface.dismiss())
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            EventBus.getDefault().post(new CleanMsgEvent());
                            dialogInterface.dismiss();
                        })
                        .setCancelable(false)
                        .show();
                break;
            default:
                break;
        }
    }


    private void initView()
    {
        toolbar.setTitle(R.string.text_setting);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

}
