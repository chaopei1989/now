package com.nao.im.player;
import com.nao.im.model.Channel;

import com.nao.im.model.MusicEntity;

interface IAVIMManager {

        boolean isLogin();

        boolean isJoined();

        void sendMessage(String text);

        void disconnect();

        void clearNotification();

        oneway void loginAndJoinChat(int channelId);
}
