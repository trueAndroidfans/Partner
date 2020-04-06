package com.aokiji.partner.api;

import com.aokiji.partner.models.chat.ChatParams;
import com.aokiji.partner.models.chat.ChatReturn;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by zhangdonghai on 2018/7/31.
 */

public interface Api {

    /**
     * 聊天
     *
     * @param chatParams
     * @return
     */
    @POST("openapi/api/v2") Observable<ChatReturn> chat(@Body ChatParams chatParams);

}
