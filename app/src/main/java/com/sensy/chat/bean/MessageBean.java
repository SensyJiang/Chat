package com.sensy.chat.bean;

/**
 * Created by Sensy on 2018/1/4.
 */

public class MessageBean {
    private int messageType;//1：消息
    private String data;
    private String name;
    private String time;
    private int locate;//1：左；2：右

    public MessageBean(){}

    public MessageBean(int messageType, String data, String name, String time, int locate) {
        this.messageType = messageType;
        this.data = data;
        this.name = name;
        this.time = time;
        this.locate = locate;
    }

    public int getMessageType() {
        return messageType;
    }

    public String getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public int getLocate() {
        return locate;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLocate(int locate) {
        this.locate = locate;
    }
}
