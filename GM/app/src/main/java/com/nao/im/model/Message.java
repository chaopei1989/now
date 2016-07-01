package com.nao.im.model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by chaopei on 2015/10/22.
 */
public class Message extends DataSupport {
    private int id;
    private int channelId;

    /**
     * LeanCloud上的id
     */
    private String messageId;
    /**
     * 文本内容
     */
    private String txt;

    private String fromUserName;
    /**
     * 发送者
     * userID
     */
    private int fromId;

    private String toUserName;
    /**
     * 接收者
     * userID
     */
    private int toId;
    /**
     * 消息接受者类型, 0-群聊
     */
    private int toType;

    private String iconUrl;

    public static final int MSG_TO_TYPE_GROUP = 0;

    /**
     * 消息类型
     * 文本消息 -1
     * 图像消息 -2
     * 音频消息 -3
     * 视频消息 -4
     * 位置消息 -5
     * 文件消息 -6
     *
     */
    private int type;

    public static final int MSG_TYPE_TEXT = -1;

    /**
     * 信息发送到服务器的时间
     */
    private Date timestamp;

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getTxt() {
        return txt;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getToType() {
        return toType;
    }

    public void setToType(int toType) {
        this.toType = toType;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }
}
