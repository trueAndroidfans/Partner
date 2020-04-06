package com.aokiji.partner.modules.chat;

import com.aokiji.partner.AppModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by zhangdonghai on 2018/7/31.
 */

@Singleton
@Component(modules = {AppModule.class, ChatPresenterModule.class})
public interface ChatComponent {

    void inject(ChatActivity activity);

}
