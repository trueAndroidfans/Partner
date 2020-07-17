package com.aokiji.partner.modules.chat;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aokiji.partner.App;
import com.aokiji.partner.AppModule;
import com.aokiji.partner.R;
import com.aokiji.partner.Settings;
import com.aokiji.partner.ui.adapter.MessageAdapter;
import com.aokiji.partner.base.BaseActivity;
import com.aokiji.partner.others.bdasr.AutoCheck;
import com.aokiji.partner.others.bdasr.recog.MyRecognizer;
import com.aokiji.partner.others.bdasr.recog.RecogResult;
import com.aokiji.partner.others.bdasr.recog.listener.IRecogListener;
import com.aokiji.partner.others.bdtts.control.InitConfig;
import com.aokiji.partner.others.bdtts.control.MySyntherizer;
import com.aokiji.partner.others.bdtts.control.NonBlockSyntherizer;
import com.aokiji.partner.others.bdtts.listener.UiMessageListener;
import com.aokiji.partner.others.bdtts.util.OfflineResource;
import com.aokiji.partner.db.DatabaseHelper;
import com.aokiji.partner.event.CleanMsgEvent;
import com.aokiji.partner.models.entities.chat.ChatParams;
import com.aokiji.partner.models.entities.chat.ChatReturn;
import com.aokiji.partner.models.entities.chat.Message;
import com.aokiji.partner.modules.setting.SettingActivity;
import com.aokiji.partner.utils.KeyboardsUtil;
import com.aokiji.partner.utils.SoftKeyboardStateHelper;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.aokiji.partner.Settings.AUTO_SPEAK;
import static com.aokiji.partner.Settings.BD_APPID;
import static com.aokiji.partner.Settings.BD_APPKEY;
import static com.aokiji.partner.Settings.BD_SECRETKEY;

public class ChatActivity extends BaseActivity implements ChatView {

    @Bind(R.id.iv_bg) ImageView ivBg;
    @Bind(R.id.et_content) EditText etContent;
    @Bind(R.id.rv_message) RecyclerView rvMessage;
    @Bind(R.id.iv_voice) ImageView ivVoice;
    @Bind(R.id.rl_panel) RelativeLayout rlPanel;
    @Bind(R.id.ll_input) LinearLayout llInput;
    @Bind(R.id.tv_record_state) TextView tvRecordState;
    @Bind(R.id.iv_talking) ImageView ivTalking;

    private MessageAdapter mAdapter;
    private List<Message> mList = new ArrayList<>();

    @Inject ChatPresenter mPresenter;

    private DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mDb;

    private boolean pressed = false;

    private boolean passAll = true;

    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;


    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;

    protected Handler mainHandler;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        initDependency();

        initView();

        initData();

        initPermission();

        initASR();

        initTTS();
    }


    private void initDependency() {
        DaggerChatComponent.builder()
                .appModule(new AppModule(this))
                .chatPresenterModule(new ChatPresenterModule(this))
                .build()
                .inject(this);
    }


    @OnClick({R.id.iv_voice}) void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_voice:
                if (!passAll) {
                    initPermission();
                } else {
                    if (pressed) {
                        ivVoice.setImageResource(R.drawable.ic_voice);
                        pressed = false;
                        rlPanel.setVisibility(View.GONE);
                    } else {
                        KeyboardsUtil.closeKeyboard(etContent, this);
                        ivVoice.setImageResource(R.drawable.ic_voice_passed);
                        pressed = true;
                        rlPanel.setVisibility(View.VISIBLE);
                    }
                }
                break;
            default:
                break;
        }
    }


    private void initView() {
        setSupportActionBar(findViewById(R.id.toolbar));

        SoftKeyboardStateHelper stateHelper = new SoftKeyboardStateHelper(findViewById(R.id.root));
        stateHelper.addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override public void onSoftKeyboardOpened(int keyboardHeightInPx) {

            }

            @Override public void onSoftKeyboardClosed() {
                clearEditTextFocus();
            }
        });

        etContent.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!TextUtils.isEmpty(etContent.getText().toString().trim())) {
                    sendMessageToTulin(etContent.getText().toString().trim());
                    etContent.setText("");
                }
                return true;
            }
            return false;
        });

        etContent.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ivVoice.setImageResource(R.drawable.ic_voice);
                pressed = false;
                rlPanel.setVisibility(View.GONE);
            }
        });

        ivTalking.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    tvRecordState.setText(R.string.text_talking);
                    float currentScaleX = ivTalking.getScaleX();
                    float currentScaleY = ivTalking.getScaleY();
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivTalking, "scaleX", currentScaleX, (float) (currentScaleX + 0.3), currentScaleX);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivTalking, "scaleY", currentScaleY, (float) (currentScaleY + 0.3), currentScaleY);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(scaleX).with(scaleY);
                    animatorSet.setDuration(500);
                    animatorSet.start();
                    // 开始语音识别
                    start();
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    tvRecordState.setText(R.string.text_pressed_to_talk);
                    // 取消本次识别,加上延迟,避免识别未完成
                    new Handler().postDelayed(() -> cancel(), 2000);
                    break;
            }
            return true;
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvMessage.setLayoutManager(layoutManager);
        mAdapter = new MessageAdapter(mList, this);
        mAdapter.setOnElementClickListener((view, position) -> {
            speak(mList.get(position).getMessage());
        });
        rvMessage.setAdapter(mAdapter);

        rvMessage.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (KeyboardsUtil.isSoftInputShow(ChatActivity.this)) {
                    KeyboardsUtil.closeKeyboard(etContent, ChatActivity.this);
                    clearEditTextFocus();
                }
                if (rlPanel.getVisibility() == View.VISIBLE) {
                    ivVoice.setImageResource(R.drawable.ic_voice);
                    pressed = false;
                    rlPanel.setVisibility(View.GONE);
                }
                return true;
            }
            return false;
        });

        mainHandler = new Handler() {
            @Override public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
            }
        };
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                break;
            case R.id.action_auto_speak:
                boolean isChecked = !item.isChecked();
                item.setChecked(isChecked);
                AUTO_SPEAK = isChecked;
                Toast.makeText(this, isChecked ? R.string.hint_auto_speak_open : R.string.hint_auto_speak_close, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(ChatActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    private void initData() {
        mPresenter.getChatBg();

        mDatabaseHelper = new DatabaseHelper(this, "MessageBox.db", null, 1);
        mDb = mDatabaseHelper.getWritableDatabase();

        //查询Message表中的所有数据
        Cursor cursor = mDb.query("Message", null, null, null, null, null, null);
        List<Message> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int sender = cursor.getInt(cursor.getColumnIndex("sender"));
                String content = cursor.getString(cursor.getColumnIndex("message"));
                Message message = new Message();
                message.setFromFriend(sender == 0 ? false : true);
                message.setHead(sender == 0 ? R.drawable.self : R.drawable.robot);
                message.setMessage(content);
                list.add(message);
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        mList.addAll(list);
        if (!mList.isEmpty()) {
            mAdapter.notifyDataSetChanged();
            rvMessage.smoothScrollToPosition(mList.size() - 1);
        }
    }


    private void initPermission() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }


    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            passAll = false;
                        }
                    }
                }
                break;
            default:
                break;
        }

        if (!passAll) {
            Toast.makeText(this, "一些权限缺失", Toast.LENGTH_SHORT).show();
        }
    }


    private void initASR() {
        // 新建一个回调类，识别引擎会回调这个类告知重要状态和识别结果
        IRecogListener listener = new IRecogListener() {
            @Override public void onAsrReady() {

            }

            @Override public void onAsrBegin() {

            }

            @Override public void onAsrEnd() {

            }

            @Override public void onAsrPartialResult(String[] results, RecogResult recogResult) {

            }

            @Override public void onAsrOnlineNluResult(String nluResult) {

            }

            @Override public void onAsrFinalResult(String[] results, RecogResult recogResult) {
                if (results.length > 0) {
                    sendMessageToTulin(results[0]);
                }
                stop();
            }

            @Override public void onAsrFinish(RecogResult recogResult) {

            }

            @Override public void onAsrFinishError(int errorCode, int subErrorCode, String descMessage, RecogResult recogResult) {

            }

            @Override public void onAsrLongFinish() {

            }

            @Override public void onAsrVolume(int volumePercent, int volume) {

            }

            @Override public void onAsrAudio(byte[] data, int offset, int length) {

            }

            @Override public void onAsrExit() {

            }

            @Override public void onOfflineLoaded() {

            }

            @Override public void onOfflineUnLoaded() {

            }
        };
        // 注册输出事件
        myRecognizer = new MyRecognizer(this, listener);
    }


    protected void start() {
        // 拼接识别参数
        final Map<String, Object> params = new HashMap<>();
        params.put("accept-audio-volume", false);
        Logger.i("设置的start输入参数：" + params);
        // 复制此段可以自动检测常规错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, false)).checkAsr(params);

        // 开始识别
        myRecognizer.start(params);
    }


    protected void stop() {
        myRecognizer.stop();
    }


    protected void cancel() {
        myRecognizer.cancel();
    }


    private void initTTS() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);

        Map<String, String> params = getParams();


        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(BD_APPID, BD_APPKEY, BD_SECRETKEY, ttsMode, params, listener);

        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
        com.aokiji.partner.others.bdtts.util.AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 100) {
                    com.aokiji.partner.others.bdtts.util.AutoCheck autoCheck = (com.aokiji.partner.others.bdtts.util.AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        toPrint(message); // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });
        synthesizer = new NonBlockSyntherizer(this, initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
    }


    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "4");
        // 设置合成的音量，0-9 ，默认 5X
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }


    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            toPrint("【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }


    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    private void speak(String content) {
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        int result = synthesizer.speak(content);
        checkResult(result, "speak");
    }


    /*
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     */
    private void stopSynthesiz() {
        int result = synthesizer.stop();
        checkResult(result, "stop");
    }


    private void checkResult(int result, String method) {
        if (result != 0) {
            toPrint("error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }


    protected void toPrint(String str) {
        android.os.Message msg = android.os.Message.obtain();
        msg.obj = str;
        mainHandler.sendMessage(msg);
    }


    private void sendMessageToTulin(String msg) {
        ChatParams.Perception.InputText inputText = new ChatParams.Perception.InputText();
        inputText.setText(msg);
        ChatParams.Perception perception = new ChatParams.Perception();
        perception.setInputText(inputText);

        ChatParams.UserInfo userInfo = new ChatParams.UserInfo();
        userInfo.setApiKey(Settings.TULING_API_KEY);
        userInfo.setUserId(Settings.TULIN_USER_ID);

        ChatParams params = new ChatParams();
        params.setReqType(0);
        params.setPerception(perception);
        params.setUserInfo(userInfo);

        mPresenter.chat(params);

        Message message = new Message();
        message.setFromFriend(false);
        message.setHead(R.drawable.self);
        message.setMessage(msg);
        mList.add(message);
        mAdapter.notifyDataSetChanged();
        rvMessage.smoothScrollToPosition(mList.size() - 1);

        //添加数据到数据库
        ContentValues values = new ContentValues();
        values.put("sender", 0);
        values.put("time", String.valueOf(System.currentTimeMillis()));
        values.put("message", msg);
        mDb.insert("Message", null, values);
        values.clear();
    }


    private void clearEditTextFocus() {
        llInput.requestFocus();
        llInput.setFocusableInTouchMode(true);
    }


    @Override protected void onResume() {
        super.onResume();
        getWindow().getDecorView().addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            if (bottom != 0 && oldBottom != 0 && bottom - rect.bottom <= 0) {
                // 隐藏
            } else {
                // 显示
                if (!mList.isEmpty()) {
                    rvMessage.smoothScrollToPosition(mList.size() - 1);
                }
            }
        });
    }


    @Override public void onSuccess(ChatReturn data) {
        List<ChatReturn.Item> list = data.getResults();
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                Message message = new Message();
                message.setFromFriend(true);
                message.setHead(R.drawable.robot);
                message.setMessage(list.get(i).getValues().getText());
                mList.add(message);

                //添加数据到数据库
                ContentValues values = new ContentValues();
                values.put("sender", 1);
                values.put("time", String.valueOf(System.currentTimeMillis()));
                values.put("message", list.get(i).getValues().getText());
                mDb.insert("Message", null, values);
                values.clear();

                if (AUTO_SPEAK) {
                    speak(list.get(i).getValues().getText());
                }
            }
            mAdapter.notifyDataSetChanged();
            rvMessage.smoothScrollToPosition(mList.size() - 1);
        }
    }


    @Override public void onFail(Throwable throwable) {
        Logger.e(throwable.getMessage());
    }


    @Override public void getBgSuccess(String url) {
        runOnUiThread(() ->
                Glide.with(this)
                        .load(url)
                        .centerCrop()
                        .placeholder(R.color.md_grey_300)
                        .error(R.color.md_grey_500)
                        .crossFade()
                        .override(800, 1280)
                        .into(ivBg)
        );
    }


    @Override public void getBgFail(Throwable throwable) {
        Logger.e(throwable.getMessage());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CleanMsgEvent event) {
        if (event != null) {
            mList.clear();
            mAdapter.notifyDataSetChanged();

            int count = mDb.delete("Message", null, null);
            Toast.makeText(App.getInstance(), count + "条记录已清除", Toast.LENGTH_SHORT).show();
        }
    }


    @Override protected void onDestroy() {
        myRecognizer.release();
        synthesizer.release();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
