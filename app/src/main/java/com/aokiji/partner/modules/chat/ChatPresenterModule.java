package com.aokiji.partner.modules.chat;

import dagger.Module;
import dagger.Provides;

/**
 * Created by zhangdonghai on 2018/7/31.
 */

@Module
public class ChatPresenterModule {

    private ChatView chatView;


    public ChatPresenterModule(ChatView chatView) {
        this.chatView = chatView;
    }


    @Provides public ChatView provideChatView() {
        return chatView;
    }

}
