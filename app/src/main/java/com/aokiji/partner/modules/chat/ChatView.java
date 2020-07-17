package com.aokiji.partner.modules.chat;

import com.aokiji.partner.models.entities.chat.ChatReturn;

/**
 * Created by zhangdonghai on 2018/7/31.
 */

public interface ChatView {

    void onSuccess(ChatReturn data);

    void onFail(Throwable throwable);

    void getBgSuccess(String url);

    void getBgFail(Throwable throwable);

}
