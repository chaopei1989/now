package com.nao.im.player;

import android.content.Intent;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMClientEventHandler;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.multiprocess.crossprocess.ServiceList;
import com.multiprocess.crossprocess.ServiceManager4Chat;
import com.nao.im.App;
import com.nao.im.GMEnv;
import com.nao.im.model.Message;
import com.nao.im.net.login.SocialUserManager;
import com.nao.im.util.SysUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chaopei on 2015/10/20.
 */
public class AVIMManager extends IAVIMManager.Stub {
    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "AVIMManager" : AVIMManager.class.getSimpleName();

    AVIMClient mClient;
    private AVIMConversation mConv;

    private boolean isLogin = false, isJoined = false;

    private int EVENT_STATE;

    private CustomMessageHandler mReceiveHandler;

    private static AVIMManager instance = new AVIMManager();

    public static AVIMManager getInstance() {
        return instance;
    }

    final public static com.multiprocess.crossprocess.Service INSTALLER = new com.multiprocess.crossprocess.Service() {

        @Override
        public int getServiceId() {
            return ServiceList.ID.CHAT_SERVICE;
        }

        @Override
        public IBinder getService() {
            return instance;
        }

        @Override
        public IInterface asInterface(IBinder binder) {
            return IAVIMManager.Stub.asInterface(binder);
        }

    };

    public static class Constants {
        public static final String ACTION_MSG_SEND_SUCCESS = "ACTION_SEND_SUCCESS";
        public static final String ACTION_MSG_SEND_FAILED = "ACTION_SEND_FAILED";
        public static final String ACTION_MSG_RECEIVE = "ACTION_RECEIVE";

        public static final String ATTR_KEY_ICON = "ATTR_KEY_ICON";
        public static final String ATTR_KEY_NAME = "ATTR_KEY_NAME";
    }

    @Override
    public boolean isLogin() throws RemoteException {
        if (!App.isChatProcess()) {
            IAVIMManager manager = (IAVIMManager) ServiceManager4Chat.getService(ServiceList.ID.CHAT_SERVICE);
            if (null == manager) {
                throw new RemoteException();
            }
            return manager.isLogin();
        }
        if (DEBUG) {
            Log.d(TAG, "[isLogin] : " + isLogin);
        }
        return isLogin;
    }

    @Override
    public boolean isJoined() throws RemoteException {
        if (!App.isChatProcess()) {
            IAVIMManager manager = (IAVIMManager) ServiceManager4Chat.getService(ServiceList.ID.CHAT_SERVICE);
            if (null == manager) {
                throw new RemoteException();
            }
            return manager.isJoined();
        }
        if (DEBUG) {
            Log.d(TAG, "[isJoined] : " + isJoined);
        }
        return isJoined;
    }

    @Override
    synchronized public void disconnect() throws RemoteException {
        if (!App.isChatProcess()) {
            IAVIMManager manager = (IAVIMManager) ServiceManager4Chat.getService(ServiceList.ID.CHAT_SERVICE);
            if (null == manager) {
                throw new RemoteException();
            }
            manager.disconnect();
            return;
        }
        if (!isLogin) {
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "[disconnect]");
        }
        mClient.close(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
            }
        });
        isLogin = isJoined = false;
    }

    @Override
    public void clearNotification() throws RemoteException {
        if (!App.isChatProcess()) {
            IAVIMManager manager = (IAVIMManager) ServiceManager4Chat.getService(ServiceList.ID.CHAT_SERVICE);
            if (null == manager) {
                throw new RemoteException();
            }
            manager.clearNotification();
            return;
        }
        ChatNotification.getInstance().clear();
    }

    synchronized public void loginAndJoinChat(final int channelId) throws RemoteException {
        if (!App.isChatProcess()) {
            IAVIMManager manager = (IAVIMManager) ServiceManager4Chat.getService(ServiceList.ID.CHAT_SERVICE);
            if (null == manager) {
                throw new RemoteException();
            }
            manager.loginAndJoinChat(channelId);
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "[loginAndJoinChat]");
        }
        if (isLogin && isJoined) {
            return;
        } else if (isLogin) {
            joinChat(channelId);
            return;
        }
        if (null == mReceiveHandler) {
            mReceiveHandler = new CustomMessageHandler();
            AVIMMessageManager.registerDefaultMessageHandler(mReceiveHandler);
        }
        AVIMClient.setClientEventHandler(mClientEventHandler);
        mClient = AVIMClient.getInstance(String.valueOf(SocialUserManager.getInstance().getSocialUser().getUserId()));
        sendAttrs.put(Constants.ATTR_KEY_ICON, SocialUserManager.getInstance().getSocialUser().getIconUrl());
        sendAttrs.put(Constants.ATTR_KEY_NAME, SocialUserManager.getInstance().getSocialUser().getUserName());
        mClient.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e == null) {
                    if (DEBUG) {
                        Log.d(TAG, "[loginAndJoinChat] : AVIM 登录成功");
                    }
                    isLogin = true;
                    joinChat(channelId);
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "[loginAndJoinChat] : AVIM 登录失败, 重试", e);
                    }
                    try {
                        loginAndJoinChat(channelId);
                    } catch (RemoteException e1) {
                        ChatNotification.getInstance().clear();
                    }
                }
            }
        });
    }

    private synchronized void joinChat(final int channelId) {
        AVIMConversationQuery query = mClient.getQuery();
        query.limit(1);
        query.whereEqualTo("name", String.valueOf(channelId));
        query.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> list, AVIMException e) {
                if (e == null && null != list && 0 < list.size()) {
                    final AVIMConversation conv = list.get(0);
                    conv.join(new AVIMConversationCallback() {
                        @Override
                        public void done(AVIMException e) {
                            if (null == e) {
                                if (DEBUG) {
                                    Log.d(TAG, "[AVIMConversation.join] : 会话加入成功");
                                }
                                mConv = conv;
                                isJoined = true;
                            } else {
                                if (DEBUG) {
                                    Log.e(TAG, "[AVIMConversation.join] 失败, 重试", e);
                                }
                                joinChat(channelId);
                            }
                        }
                    });
                } else {
                    if (DEBUG) {
                        Log.e(TAG, "[findInBackground] : 失败，list=" + list + ", 重试", e);
                    }
                    joinChat(channelId);
                }
            }
        });
    }

    Map<String, Object> sendAttrs = new HashMap<String, Object>();

    @Override
    synchronized public void sendMessage(String text) throws RemoteException {
        if (!App.isChatProcess()) {
            IAVIMManager manager = (IAVIMManager) ServiceManager4Chat.getService(ServiceList.ID.CHAT_SERVICE);
            if (null == manager) {
                throw new RemoteException();
            }
            manager.sendMessage(text);
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "[sendMessage]");
        }
        if (!isLogin || !isJoined) {
            if (DEBUG) {
                Log.d(TAG, "[sendMessage] : cannot sendMessage");
                Log.d(TAG, "[sendMessage] : isLogin=" + isLogin);
                Log.d(TAG, "[sendMessage] : isJoined=" + isJoined);
            }
            return;
        }
        final AVIMTextMessage avimTextMessage = new AVIMTextMessage();
        avimTextMessage.setText(text);
        avimTextMessage.setAttrs(sendAttrs);
        mConv.sendMessage(avimTextMessage, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                if (e == null) {
                    if (DEBUG) {
                        Log.d(TAG, "发送成功！msg.messageId=" + avimTextMessage.getMessageId());
                    }
                    try {
                        Message msg = new Message();
                        msg.setChannelId(Integer.valueOf(mConv.getName()));
                        msg.setMessageId(avimTextMessage.getMessageId());
                        msg.setTxt(avimTextMessage.getText());
                        msg.setFromId(Integer.valueOf(avimTextMessage.getFrom()));
                        msg.setType(Message.MSG_TYPE_TEXT);
                        msg.setFromUserName(getAttrString(avimTextMessage, Constants.ATTR_KEY_NAME));
                        msg.setIconUrl(getAttrString(avimTextMessage, Constants.ATTR_KEY_ICON));
                        msg.setToType(Message.MSG_TO_TYPE_GROUP);
                        msg.setTimestamp(new Date(avimTextMessage.getTimestamp()));
                        msg.save();
                        Intent intent = new Intent(Constants.ACTION_MSG_SEND_SUCCESS);
                        SysUtils.sendBroadcast(App.getContext(), intent);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    if (DEBUG) {
                        Log.e(TAG, "", e);
                    }
                }
            }
        });
    }

    private AVIMClientEventHandler mClientEventHandler = new AVIMClientEventHandler() {
        @Override
        public void onConnectionPaused(AVIMClient avimClient) { //指网络连接断开事件发生，此时聊天服务不可用
            if (DEBUG) {
                Log.d(TAG, "[onConnectionPaused]");
            }
            EVENT_STATE = 1;
        }

        @Override
        public void onConnectionResume(AVIMClient avimClient) {// 指网络连接恢复正常，此时聊天服务变得可用。
            if (DEBUG) {
                Log.d(TAG, "[onConnectionResume]");
            }
            EVENT_STATE = 0;
        }

        @Override
        public void onClientOffline(AVIMClient avimClient, int i) {
            if (DEBUG) {
                Log.d(TAG, "[onClientOffline]");
            }
            isLogin = isJoined = false;
        }
    };


    public static class CustomMessageHandler extends AVIMMessageHandler {
        //接收到消息后的处理逻辑
        @Override
        public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
            if (message instanceof AVIMTextMessage) {
                AVIMTextMessage avimTextMessage = (AVIMTextMessage) message;
                if (DEBUG) {
                    Log.d(TAG, avimTextMessage.getText());
                    Log.d(TAG, avimTextMessage.getFrom());
                    Log.d(TAG, avimTextMessage.getTimestamp() + "");
                }
                try {
                    Message msg = new Message();
                    msg.setChannelId(Integer.valueOf(conversation.getName()));
                    msg.setMessageId(avimTextMessage.getMessageId());
                    msg.setTxt(avimTextMessage.getText());
                    msg.setFromId(Integer.valueOf(avimTextMessage.getFrom()));
                    msg.setType(Message.MSG_TYPE_TEXT);
                    msg.setFromUserName(getAttrString(avimTextMessage, Constants.ATTR_KEY_NAME));
                    msg.setIconUrl(getAttrString(avimTextMessage, Constants.ATTR_KEY_ICON));
                    msg.setToType(Message.MSG_TO_TYPE_GROUP);
                    msg.setTimestamp(new Date(avimTextMessage.getTimestamp()));
                    msg.save();
                    if (DEBUG) {
                        Log.d(TAG, "[onMessage]: show notification.");
                    }
                    ChatNotification.getInstance().show(msg.getFromUserName(), msg.getTxt(), msg.getTimestamp().getTime());
                    Intent intent = new Intent(Constants.ACTION_MSG_RECEIVE);
                    SysUtils.sendBroadcast(App.getContext(), intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {

        }
    }

    private static String getAttrString(AVIMTextMessage avimTextMessage, String key) {
        Map<String, Object> attrs = avimTextMessage.getAttrs();
        if (null == attrs) {
            return null;
        }
        if (null == attrs.get(key)) {
            return null;
        }
        return String.valueOf(attrs.get(key));
    }

    private static String setAttrString(AVIMTextMessage avimTextMessage, String key) {
        Map<String, Object> attrs = avimTextMessage.getAttrs();
        if (null == attrs) {
            return null;
        }
        if (null == attrs.get(key)) {
            return null;
        }
        return String.valueOf(attrs.get(key));
    }
}
