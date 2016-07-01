package com.nao.im.player;
import com.nao.im.model.Channel;

import com.nao.im.model.MusicEntity;
import com.nao.im.model.login.SocialUser;

interface IChannelService {
    void connectAndJoinChannel();

    Channel getChannel();

        int fetchState();

        MusicEntity fetchCurrentMusic();

        int fetchBufferedPercent();

        boolean isChannelConnected();

        boolean isChannelJoined();

        boolean isEnable();

        void disconnectChannel();

        void toast(String msg);

        void setCommend(int type);

        int getCommend();

        void loveClick(int type, int userId, String openId);

        int getUserCount();

        int getMusicCount();

        SocialUser getCurrUser();
}
