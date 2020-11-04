package com.aokiji.partner.modules.chat;

import android.text.TextUtils;

import com.aokiji.partner.models.network.api.Api;
import com.aokiji.partner.models.entities.chat.ChatParams;
import com.aokiji.partner.models.entities.chat.ChatReturn;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhangdonghai on 2018/7/31.
 */

public class ChatPresenter {

    private Api mApi;
    private ChatView chatView;


    @Inject
    public ChatPresenter(Api mApi, ChatView chatView)
    {
        this.mApi = mApi;
        this.chatView = chatView;
    }


    public void chat(ChatParams params)
    {
        mApi.chat(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ChatReturn>() {
                    @Override
                    public void accept(ChatReturn data) throws Exception
                    {
                        if (chatView != null)
                        {
                            if (data != null)
                            {
                                chatView.onSuccess(data);
                            }
                        }
                    }
                });
    }


    public void getChatBg()
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://guolin.tech/api/bing_pic").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e)
            {
                if (chatView != null)
                {
                    chatView.getBgFail(new Throwable(e));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                if (chatView != null)
                {
                    String url = response.body().string();
                    if (!TextUtils.isEmpty(url))
                    {
                        chatView.getBgSuccess(url);
                    }
                }
            }
        });
    }

}
