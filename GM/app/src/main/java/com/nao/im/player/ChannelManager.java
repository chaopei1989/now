package com.nao.im.player;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.nao.im.App;
import com.nao.im.GMEnv;
import com.nao.im.model.login.SocialUser;
import com.nao.im.net.GMApi;
import com.nao.im.net.login.SocialUserManager;
import com.nao.im.net.parser.ReplyWrapper;
import com.nao.im.util.SysUtils;

import java.net.URISyntaxException;

/**
 * Created by chaopei on 2015/8/29.
 * 长连接管理类
 */
public class ChannelManager {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "ChannelManager" : ChannelManager.class.getSimpleName();

    private static ChannelManager sInstance;

    private ChannelManager(){}

    private static final int MSG_HEART = 0, MSG_TRY_CONNECT = 1;

    private int mTryConnectCount = 0;

    private final static int HEART_INTERVAL = 20000, TRY_CONNECT_COUNT_INTERVAL = 5000;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HEART:
                    removeMessages(MSG_HEART);
                    if(!mIsConnected){
                        break;
                    }
                    if (DEBUG) {
                        Log.d(TAG, "[MSG_HEART]");
                    }
                    mSocket.emit(Constants.EVENT_HEART_CS, "");
                    break;
                case MSG_TRY_CONNECT:
                    if(mIsConnected) {
                        break;
                    }
                    mTryConnectCount ++;
                    if(0 == mTryConnectCount % 2) {
                        mSocket.connect();
                    }
                    if (DEBUG) {
                        Log.d(TAG, String.format("[MSG_TRY_CONNECT] : disconnected (%d秒)", (mTryConnectCount*5)));
                    }
                    sendEmptyMessageDelayed(MSG_TRY_CONNECT, TRY_CONNECT_COUNT_INTERVAL);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public static ChannelManager getInstance() {
        if (null == sInstance) {
            sInstance = new ChannelManager();
        }
        return sInstance;
    }

    private Socket mSocket;

    private boolean mIsConnected = false;

    private boolean mIsJoined = false;

    public boolean isConnected() {
        return mIsConnected;
    }

    public boolean isJoined() {
        return mIsJoined;
    }

    private Emitter.Listener mConnectionListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (DEBUG) {
                Log.d(TAG, "[connect]");
            }
            mIsConnected = true;
            mHandler.removeMessages(MSG_TRY_CONNECT);
            mTryConnectCount = 0;
            SocialUser user = SocialUserManager.getInstance().getSocialUser();
            mSocket.emit(Constants.EVENT_JOIN_CS, String.format("{\"cid\":%d, \"userId\":%d, \"openId\":\"%s\"}", mHolder.cid, user.getUserId(), user.getOpenId()));

            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_CONNECT_BROADCAST);
            SysUtils.sendBroadcast(App.getContext(), intent);
        }
    };

    private Emitter.Listener mDisconnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (DEBUG) {
                Log.d(TAG, "[disconnect]");
            }
            mIsConnected = false;
            mIsJoined = false;
            if(null != mHolder && null != mHolder.disconnectListener) {
                mHolder.disconnectListener.call(args);
            }
            if (!forceDisconnect) {
                mHandler.sendEmptyMessageDelayed(MSG_TRY_CONNECT, TRY_CONNECT_COUNT_INTERVAL);
            } else {
                forceDisconnect = false;
            }
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_DISCONNECT_BROADCAST);
            SysUtils.sendBroadcast(App.getContext(), intent);
        }
    };

    /**
     * 主动心跳请求的回调
     */
    private Emitter.Listener mHeartListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            synchronized (ChannelManager.this) {
                if (DEBUG) {
                    Log.d(TAG, "[heart]");
                }
                ReplyWrapper rw = ReplyWrapper.parse(args[0].toString());
                if (rw.isError()) {
                    disconnect();
                    return;
                }
                if(null != mHolder && null != mHolder.heartListener) {
                    mHolder.heartListener.call(args);
                }
                mHandler.sendEmptyMessage(MSG_HEART);
            }
        }
    };

    /**
     * 主动加入频道请求的回调
     */
    private Emitter.Listener mJoinListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            synchronized (ChannelManager.this) {
                if (DEBUG) {
                    Log.d(TAG, "[join]");
                }
                ReplyWrapper rw = ReplyWrapper.parse(args[0].toString());
                if (rw.isError()) {
                    disconnect();
                    return;
                }
                mIsJoined = true;
                if(null != mHolder && null != mHolder.joinListener) {
                    mHolder.joinListener.call(args);
                }
            }
        }
    };

    private Emitter.Listener mNextMusicListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            synchronized (ChannelManager.this) {
                if (DEBUG) {
                    Log.d(TAG, "[next music]");
                }
                if(null != mHolder && null != mHolder.nextMusicListener) {
                    mHolder.nextMusicListener.call(args);
                }
            }
        }
    };

    private Emitter.Listener mPlayListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            synchronized (ChannelManager.this) {
                if (DEBUG) {
                    Log.d(TAG, "[play]");
                }
                if(null != mHolder && null != mHolder.playListener) {
                    mHolder.playListener.call(args);
                }
            }
        }
    };

    private Emitter.Listener mLikeDislikeListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            synchronized (ChannelManager.this) {
                if (DEBUG) {
                    Log.d(TAG, "[like dislike]");
                }
                if(null != mHolder && null != mHolder.likeDislikeListener) {
                    mHolder.likeDislikeListener.call(args);
                }
            }
        }
    };

    public static class Constants {

        public static final String ACTION_CONNECT_BROADCAST = "ACTION_CONNECT_BROADCAST";
        public static final String ACTION_DISCONNECT_BROADCAST = "ACTION_DISCONNECT_BROADCAST";

        /**
         * 连接成功
         */
        public static final String EVENT_CONNECTION = "connect";
        /**
         * 连接断开
         */
        public static final String EVENT_DISCONNECT = "disconnect";
        /**
         * 加入指定频道
         */
        public static final String EVENT_JOIN_CS = "join_cs";
        public static final String EVENT_JOIN_SC = "join_sc";
        /**
         * 切歌，让播放器准备好
         */
        public static final String EVENT_MUSIC_NEXT_SC = "next_sc";
        /**
         * 播放当前歌曲
         */
        public static final String EVENT_PLAY_SC = "play_sc";
        /**
         * 心跳
         */
        public static final String EVENT_HEART_CS = "heart_cs";
        public static final String EVENT_HEART_SC = "heart_sc";

        public static final String EVENT_LIKE_DISLIKE_SC = "like_dislike_sc";

    }

    public void initConnection() {
        try {
            if (null != mSocket) {
                mSocket.close();
            }
            mSocket = IO.socket(GMApi.URI_CHANNEL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.off(Constants.EVENT_CONNECTION);
        mSocket.off(Constants.EVENT_DISCONNECT);
        mSocket.on(Constants.EVENT_CONNECTION, mConnectionListener);
        mSocket.on(Constants.EVENT_DISCONNECT, mDisconnectListener);
        mSocket.on(Constants.EVENT_HEART_SC, mHeartListener);
        mSocket.on(Constants.EVENT_JOIN_SC, mJoinListener);
        mSocket.on(Constants.EVENT_MUSIC_NEXT_SC, mNextMusicListener);
        mSocket.on(Constants.EVENT_PLAY_SC, mPlayListener);
        mSocket.on(Constants.EVENT_LIKE_DISLIKE_SC, mLikeDislikeListener);
    }

    public void emitEvent(String event) {
        mSocket.emit(event);
    }

    public void emitEvent(String event, Object...args) {
        mSocket.emit(event, args);
    }

    public void setChannelListener(String event, Emitter.Listener listener) {
        mSocket.on(event, listener);
    }

    boolean forceDisconnect;

    public void disconnect() {
        forceDisconnect = true;
        mSocket.disconnect();
    }

    /**
     * 加入指定频道，并注册各个listener
     * @param cid
     * @param joinListener
     * @param nextMusicListener
     * @param playListener
     * @param heartListener
     */
    public void joinChannelWithConnect(int cid, Emitter.Listener joinListener, Emitter.Listener nextMusicListener, Emitter.Listener playListener, Emitter.Listener heartListener, Emitter.Listener likeDislikeListener, Emitter.Listener disconnectListener) {
        if (DEBUG) {
            Log.d(TAG, "[joinChannelWithConnect]");
        }
        if (mIsConnected) {
            return;
        }
        mHolder.cid = cid;
        mHolder.joinListener = joinListener;
        mHolder.nextMusicListener = nextMusicListener;
        mHolder.playListener = playListener;
        mHolder.heartListener = heartListener;
        mHolder.likeDislikeListener = likeDislikeListener;
        mHolder.disconnectListener = disconnectListener;

        mSocket.connect();

        mHandler.sendEmptyMessageDelayed(MSG_TRY_CONNECT, TRY_CONNECT_COUNT_INTERVAL);
    }

    private ServiceHolder mHolder = new ServiceHolder();

    class ServiceHolder {
        int cid;
        Emitter.Listener joinListener;
        Emitter.Listener nextMusicListener;
        Emitter.Listener playListener;
        Emitter.Listener heartListener;
        Emitter.Listener likeDislikeListener;
        Emitter.Listener disconnectListener;
    }

}
