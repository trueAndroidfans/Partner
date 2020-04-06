package com.aokiji.partner.models.chat;

/**
 * Created by zhangdonghai on 2018/7/31.
 */

public class Message {

    private String name;
    private int head;
    private String message;
    private boolean fromFriend;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHead() {
        return head;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFromFriend() {
        return fromFriend;
    }

    public void setFromFriend(boolean fromFriend) {
        this.fromFriend = fromFriend;
    }
}
