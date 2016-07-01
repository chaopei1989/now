package com.nao.im.player;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.nkzawa.emitter.Emitter;
import com.multiprocess.crossprocess.ServiceList;
import com.multiprocess.crossprocess.ServiceManager;
import com.nao.im.App;
import com.nao.im.GMEnv;
import com.nao.im.R;
import com.nao.im.model.Channel;
import com.nao.im.model.MusicEntity;
import com.nao.im.model.login.SocialUser;
import com.nao.im.model.play.HeartEntity;
import com.nao.im.model.play.JoinChannel;
import com.nao.im.model.play.LikeDislike;
import com.nao.im.model.play.NextPlayMusic;
import com.nao.im.net.GMDataCenter;
import com.nao.im.net.login.SocialUserManager;
import com.nao.im.net.parser.IReplyListener;
import com.nao.im.net.parser.ReplyParser;
import com.nao.im.net.parser.ReplyWrapper;
import com.nao.im.util.SysUtils;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventCenter;

/**
 * 音乐播放服务，在channel进程
 */
public class ChannelService extends Service {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "ChannelService" : ChannelService.class.getSimpleName();

    private MusicPlayer mPlayer = null;
    private PlayBroadCastReceiver mBroadCastReceiver = null;
    private PhoneCallReceiver mPhoneCallReceiver = null;
    private TelephonyManager mTelephonyManager = null;
    ChannelManager mChannelManager;
    AVIMManager mAVIMManager;
    private Channel mChannel;
    private int mUserCount, mMusicCount;

    private static ChannelService sCs;

    public static IChannelService.Stub getService() {
        return sService;
    }

    private static IChannelService.Stub sService = new IChannelService.Stub() {

        @Override
        public boolean isEnable() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                return service.isEnable();
            }
            if (DEBUG) {
                Log.d(TAG, "[isEnable]");
            }
            if (null == sCs) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public void connectAndJoinChannel() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                service.connectAndJoinChannel();
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "[connectAndJoinChannel]", new Throwable());
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
            } else {
                sCs.connectAndJoinChannel();
            }
        }

        @Override
        public Channel getChannel() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                return service.getChannel();
            }
            if (DEBUG) {
                Log.d(TAG, "[getChannel]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
                return null;
            } else {
                return sCs.mChannel;
            }
        }

        @Override
        public int fetchState() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                return service.fetchState();
            }
            if (DEBUG) {
                Log.d(TAG, "[fetchState]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
                return MusicPlayState.MPS_UNEXPECTED;
            } else {
                return sCs.mPlayer.fetchState();
            }
        }

        @Override
        public MusicEntity fetchCurrentMusic() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                return service.fetchCurrentMusic();
            }
            if (DEBUG) {
                Log.d(TAG, "[fetchCurrentMusic]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
                return null;
            } else {
                return sCs.mPlayer.fetchCurrentMusic();
            }
        }

        @Override
        public int fetchBufferedPercent() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                return service.fetchBufferedPercent();
            }
            if (DEBUG) {
                Log.d(TAG, "[fetchBufferedPercent]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
                return -1;
            } else {
                return sCs.mPlayer.fetchBufferedPercent();
            }
        }

        @Override
        public boolean isChannelConnected() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                return service.isChannelConnected();
            }
            if (DEBUG) {
                Log.d(TAG, "[isChannelConnected]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
                return false;
            } else {
                return sCs.mChannelManager.isConnected();
            }
        }

        @Override
        public boolean isChannelJoined() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                return service.isChannelJoined();
            }
            if (DEBUG) {
                Log.d(TAG, "[isChannelJoined]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
                return false;
            } else {
                return sCs.mChannelManager.isJoined();
            }
        }

        @Override
        public void disconnectChannel() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                service.disconnectChannel();
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "[disconnectChannel]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
            } else {
                sCs.mChannelManager.disconnect();
                sCs.mAVIMManager.disconnect();
            }
        }

        @Override
        public void toast(String msg) throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                service.toast(msg);
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "[toast]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
            } else {
                sCs.mH.obtainMessage(MSG_TOAST, 0, 0, msg).sendToTarget();
            }
        }

        @Override
        public void setCommend(int commend) throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                service.setCommend(commend);
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "[setCommend]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
            } else {
                sCs.mPlayer.setCommendType(commend);
            }
        }

        @Override
        public int getCommend() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                return service.getCommend();
            }
            if (DEBUG) {
                Log.d(TAG, "[getCommend]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
                return 0;
            } else {
                return sCs.mPlayer.getCommendType();
            }
        }

        @Override
        public void loveClick(int type, int userId, String openId) throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                service.loveClick(type, userId, openId);
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "[loveClick]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
            } else {
                sCs.loveClick(type, userId, openId);
            }
        }

        @Override
        public int getUserCount() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                return service.getUserCount();
            }
            if (DEBUG) {
                Log.d(TAG, "[getUserCount]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
                return 0;
            } else {
                return sCs.mUserCount;
            }
        }

        @Override
        public int getMusicCount() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                return service.getMusicCount();
            }
            if (DEBUG) {
                Log.d(TAG, "[getMusicCount]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
                return 0;
            } else {
                return sCs.mMusicCount;
            }
        }

        @Override
        public SocialUser getCurrUser() throws RemoteException {
            if (!App.isChannelProcess()) {
                IChannelService service = (IChannelService) ServiceManager.getService(ServiceList.ID.CHANNEL_SERVICE);
                if (null == service) {
                    throw new RemoteException();
                }
                return service.getCurrUser();
            }
            if (DEBUG) {
                Log.d(TAG, "[getCurrUser]");
            }
            if (null == sCs) {
                Log.e(TAG, "ChannelService not init.", new Exception());
                return null;
            } else {
                return sCs.mPlayer.fetchCurrentUser();
            }
        }

    };

    private void loveClick(final int type, int userId, String openId) {
        MusicEntity music = mPlayer.fetchCurrentMusic();
        if (null != music) {
            int currCommend = mPlayer.getCommendType();
            if (MusicPlayer.Constants.COMMEND_NEW != currCommend) {
                mH.obtainMessage(MSG_TOAST, 0, 0, getString(R.string.commend_duplicated)).sendToTarget();
                return;
            }
            //只有当new时才能置为wait
            mPlayer.setCommendType(MusicPlayer.Constants.COMMEND_WAIT);
            String json = String.format(
                    "{\"userId\":%d,\"openId\":\"%s\",\"musicId\":%d,\"type\":%d}",
                    userId, openId, music.getMusicId(), type);
            GMDataCenter.postChannelCommend(this, new IReplyListener<Object>() {
                @Override
                public void onReplyParserResponse(List<Object> response) {
                    if (DEBUG) {
                        Log.d(TAG, "[onReplyParserResponse]");
                    }
                    mH.obtainMessage(MSG_TOAST, 0, 0, getString(R.string.commend_success)).sendToTarget();
                    //只有当wait时才能置为赞踩状态
                    if (MusicPlayer.Constants.COMMEND_WAIT == mPlayer.getCommendType()) {
                        mPlayer.setCommendType(type);
                    }
                }

                @Override
                public void onReplyParserError(ReplyWrapper<Object> wrapper) {
                    if (DEBUG) {
                        Log.e(TAG, "[onReplyParserError] : status=" + wrapper.getStatus());
                        Log.e(TAG, "[onReplyParserError] : status=" + wrapper.getMsg());
                        mH.obtainMessage(MSG_TOAST, 0, 0, "点赞失败，status=" + wrapper.getStatus() + ", msg=" + wrapper.getMsg()).sendToTarget();
                    }
                    if (ReplyParser.Constants.ERR_DUPLICATED == wrapper.getStatus()) {
                        mH.obtainMessage(MSG_TOAST, 0, 0, getString(R.string.commend_duplicated)).sendToTarget();
                        if (MusicPlayer.Constants.COMMEND_WAIT == mPlayer.getCommendType()) {
                            mPlayer.setCommendType(type);
                        }
                    } else {
                        mH.obtainMessage(MSG_TOAST, 0, 0, getString(R.string.commend_later)).sendToTarget();
                        mPlayer.setCommendType(MusicPlayer.Constants.COMMEND_NEW);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (DEBUG) {
                        Log.e(TAG, "[onErrorResponse]", error);
                    }
                    mPlayer.setCommendType(MusicPlayer.Constants.COMMEND_NEW);
                }
            }, json);
        } else {
            mH.obtainMessage(MSG_TOAST, 0, 0, getString(R.string.commend_no_music)).sendToTarget();
        }
    }

    private final static int MSG_TOAST = 2;

    Handler mH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TOAST:
                    Toast.makeText(ChannelService.this, msg.obj + "", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    final public static com.multiprocess.crossprocess.Service INSTALLER = new com.multiprocess.crossprocess.Service() {

        @Override
        public int getServiceId() {
            return ServiceList.ID.CHANNEL_SERVICE;
        }

        @Override
        public IBinder getService() {
            return sService;
        }

        @Override
        public IInterface asInterface(IBinder binder) {
            return IChannelService.Stub.asInterface(binder);
        }

    };

    public class Constants {
        public static final String ACTION_CHANNEL_CHANGED_BROADCAST = "action_channel_changed_broadcast";
        public static final String ACTION_LIKE_DISLIKE_CHANGED_BROADCAST = "action_like_dislike_changed_broadcast";
        public static final String KEY_CHANNEL = "key_channel";
        public static final String KEY_LIKE = "key_like";
        public static final String KEY_DISLIKE = "key_dislike";
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initMusicPlayer();

        initPlayReceiver();

        initPhoneCallListener();

        initChannel();

        sCs = this;
    }

    /**
     * 初始化播放器
     */
    void initMusicPlayer() {
        mPlayer = MusicPlayer.getInstance();
        MusicEntity entity = new MusicEntity();
        entity.setUrl("http://sc1.111ttt.com/2015/1/09/07/102071336273.mp3");
        mPlayer.addMusic(entity);
    }

    /**
     * 来去电监听
     */
    private void initPhoneCallListener() {
        mPhoneCallReceiver = new PhoneCallReceiver();

        IntentFilter phoneCallFilter = new IntentFilter();
        phoneCallFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);

        registerReceiver(mPhoneCallReceiver, phoneCallFilter);

        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(new PhoneStateChangedListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * 初始化接收播放相关的各种命令的BroadcastReceiver
     */
    private void initPlayReceiver() {
        mBroadCastReceiver = new PlayBroadCastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlayer.Constants.ACTION_MUSIC_NEXT);
        filter.addAction(MusicPlayer.Constants.ACTION_MUSIC_PAUSE);
        filter.addAction(MusicPlayer.Constants.ACTION_MUSIC_PLAY);
        filter.addAction(MusicPlayer.Constants.ACTION_MUSIC_PREV);
        filter.addAction(MusicPlayer.Constants.ACTION_MUSIC_REPLAY);
        filter.addAction(MusicPlayer.Constants.ACTION_MUSIC_STOP);
        filter.addAction(MusicPlayer.Constants.ACTION_EXIT);
        filter.addAction(MusicPlayer.Constants.ACTION_SEEK_TO);

        registerReceiver(mBroadCastReceiver, filter);
    }

    private void initChannel() {
        mChannelManager = ChannelManager.getInstance();
        mChannelManager.initConnection();
        mAVIMManager = new AVIMManager();
    }

    private void connectAndJoinChannel() {
        if (!SocialUserManager.getInstance().isUserLogin()) {
            return;
        }
        mChannelManager.joinChannelWithConnect(1024, mJoinListener, mNextMusicListener, mPlayListener, mHeartListener, mLikeDislikeListener, mDisconnectListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadCastReceiver);
        unregisterReceiver(mPhoneCallReceiver);

        mTelephonyManager.listen(null, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (MusicPlayState.MPS_LIST_FULL == mPlayer.getPlayState() && null != intent && intent.getBooleanExtra("play_now", false)) {
            mPlayer.playFirst();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private class PlayBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MusicPlayer.Constants.ACTION_MUSIC_NEXT)) {
                mPlayer.playNext();
            } else if (action.equals(MusicPlayer.Constants.ACTION_MUSIC_PAUSE)) {
                mPlayer.pause();
            } else if (action.equals(MusicPlayer.Constants.ACTION_MUSIC_PLAY)) {
                mPlayer.playFirst();
            } else if (action.equals(MusicPlayer.Constants.ACTION_MUSIC_PREV)) {
                mPlayer.playPrev();
            } else if (action.equals(MusicPlayer.Constants.ACTION_MUSIC_REPLAY)) {
                mPlayer.replay();
            } else if (action.equals(MusicPlayer.Constants.ACTION_MUSIC_STOP)) {
                mPlayer.stop();
            } else if (action.equals(MusicPlayer.Constants.ACTION_EXIT)) {
                mPlayer.exit();
            } else if (action.equals(MusicPlayer.Constants.ACTION_SEEK_TO)) {
                int progress = intent.getIntExtra(MusicPlayer.Constants.KEY_PLAYER_SEEK_TO_PROGRESS, 0);
                mPlayer.seekTo(progress);
            }
        }

    }

    private class PhoneCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) {
                return;
            }

            String action = intent.getAction();

            if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                EventBus.getDefault().post(new EventCenter(MusicPlayer.Constants.EVENT_STOP_PLAY_MUSIC));
            }
        }
    }

    private class PhoneStateChangedListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                case TelephonyManager.CALL_STATE_RINGING:
                    EventBus.getDefault().post(new EventCenter(MusicPlayer.Constants.EVENT_STOP_PLAY_MUSIC));

                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    EventBus.getDefault().post(new EventCenter(MusicPlayer.Constants.EVENT_START_PLAY_MUSIC));

                    break;
            }
        }
    }

    private Emitter.Listener mJoinListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (DEBUG) {
                Log.d(TAG, "[mJoinListener joined] : " + args[0]);
            }
            ReplyWrapper<JoinChannel> rw = ReplyWrapper.parse(args[0] + "", JoinChannel.LIST_PARSER);
            JoinChannel channel = rw.getData();
            mChannel = channel.channel;
            try {
                if (!mAVIMManager.isLogin() || !mAVIMManager.isJoined()) {
                    mAVIMManager.loginAndJoinChat(mChannel.getChannelId());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_CHANNEL_CHANGED_BROADCAST);
            intent.putExtra(Constants.KEY_CHANNEL, mChannel);
            SysUtils.sendBroadcast(ChannelService.this, intent);
            ChannelNotification.getInstance().frontService(ChannelService.this);
            if (null != channel.curr && null != channel.currUser) {
                MusicEntity readyMusic = mPlayer.fetchCurrentMusic();
                SocialUser currUser = mPlayer.fetchCurrentUser();
                if (!channel.curr.equals(readyMusic) || !channel.currUser.equals(currUser)) {
                    if (DEBUG) {
                        Log.d(TAG, "[mJoinListener] : 服务端已经开始播放了，立即切歌播放");
                    }
                    readyMusic = channel.curr;
                    mPlayer.refreshMusic(readyMusic, channel.currUser);
                    mPlayer.localPlay();
                    mPlayer.remotePlay();
                } else {
                    if (channel.state == HeartEntity.PLAYING && MusicPlayState.MPS_PLAYING != mPlayer.fetchState() && MusicPlayState.MPS_STOP != mPlayer.fetchState()) {
                        if (DEBUG) {
                            Log.d(TAG, "[mJoinListener] : 歌曲正确，但状态不对");
                        }
                        mPlayer.localPlay();
                        mPlayer.remotePlay();
                    } else if (DEBUG) {
                        Log.d(TAG, "[mJoinListener] : 播放正确，无视此心跳");
                    }
                    MusicEntity me = mPlayer.fetchCurrentMusic();
                    if (null == me) {
                        return;
                    }
                    if (me.getLike() != channel.curr.getLike()) {
                        me.setLike(channel.curr.getLike());
                    }
                    if (me.getDislike() != channel.curr.getDislike()) {
                        me.setDislike(channel.curr.getDislike());
                    }
                    if (me.getRequest() != channel.curr.getRequest()) {
                        me.setRequest(channel.curr.getRequest());
                    }
                }
            }
        }
    };

    private Emitter.Listener mHeartListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (DEBUG) {
                Log.d(TAG, "[mHeartListener] : " + args[0]);
            }
            try {
                if (!mAVIMManager.isLogin() || !mAVIMManager.isJoined()) {
                    mAVIMManager.loginAndJoinChat(mChannel.getChannelId());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            String msg = args[0].toString();
            ReplyWrapper<HeartEntity> rw = ReplyWrapper.parse(msg, HeartEntity.LIST_PARSER);
            HeartEntity entity = rw.getData();
            if (null != entity) {
                mUserCount = entity.userCount;
                mMusicCount = entity.musicCount;
                if (DEBUG) {
                    Log.d(TAG, "[mHeartListener] : mUserCount= " + mUserCount);
                    Log.d(TAG, "[mHeartListener] : mUserCount= " + mMusicCount);
                }
            }
            if (null != entity && null != entity.music && null != entity.currUser) {
                if (DEBUG) {
                    Log.d(TAG, "[mHeartListener] : entity.music.title = " + entity.music.getTitle());
                }
                MusicEntity readyMusic = mPlayer.fetchCurrentMusic();
                SocialUser currUser = mPlayer.fetchCurrentUser();
                if (!entity.music.equals(readyMusic) || !entity.currUser.equals(currUser)) {
                    if (DEBUG) {
                        Log.d(TAG, "[mHeartListener] : 服务端已经开始播放了，立即切歌播放");
                    }
                    readyMusic = entity.music;
                    mPlayer.refreshMusic(readyMusic, currUser);
                    mPlayer.localPlay();
                    mPlayer.remotePlay();
                } else {
                    if (entity.state == HeartEntity.PLAYING && MusicPlayState.MPS_PLAYING != mPlayer.fetchState() && MusicPlayState.MPS_STOP != mPlayer.fetchState()) {
                        if (DEBUG) {
                            Log.d(TAG, "[mHeartListener] : 歌曲正确，但状态不对");
                        }
                        mPlayer.localPlay();
                        mPlayer.remotePlay();
                    } else if (DEBUG) {
                        Log.d(TAG, "[mHeartListener] : 播放正确，无视此心跳");
                    }
                    MusicEntity me = mPlayer.fetchCurrentMusic();
                    if (null == me) {
                        return;
                    }
                    if (me.getLike() != entity.music.getLike()) {
                        me.setLike(entity.music.getLike());
                    }
                    if (me.getDislike() != entity.music.getDislike()) {
                        me.setDislike(entity.music.getDislike());
                    }
                    if (me.getRequest() != entity.music.getRequest()) {
                        me.setRequest(entity.music.getRequest());
                    }
                }
            } else {
                if (DEBUG) {
                    Log.d(TAG, "[mHeartListener] : entity == null");
                }
            }
        }
    };
    private Emitter.Listener mNextMusicListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (DEBUG) {
                Log.d(TAG, "[mNextMusicListener] : " + args[0]);
            }
            String msg = args[0].toString();
            ReplyWrapper<NextPlayMusic> rw = ReplyWrapper.parse(msg, NextPlayMusic.LIST_PARSER);
            NextPlayMusic readyMusic = rw.getData();
            if (null != readyMusic && null != readyMusic.curr && null != readyMusic.currUser) {
                if (DEBUG) {
                    Log.d(TAG, "[mNextMusicListener] : entity.title = " + readyMusic.curr.getTitle());
                    Log.d(TAG, "[mNextMusicListener] : 服务端切歌");
                }
                mPlayer.refreshMusic(readyMusic.curr, readyMusic.currUser);
                mPlayer.localPlay();
            } else {
                Log.d(TAG, "[mNextMusicListener] : entity == null");
            }
        }
    };
    private Emitter.Listener mPlayListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (DEBUG) {
                Log.d(TAG, "[mPlayListener] : " + args[0]);
            }
            String msg = args[0].toString();
            ReplyWrapper<NextPlayMusic> rw = ReplyWrapper.parse(msg, NextPlayMusic.LIST_PARSER);
            NextPlayMusic entity = rw.getData();
            if (null != entity) {
                MusicEntity readyMusic = mPlayer.fetchCurrentMusic();
                if (!entity.curr.equals(readyMusic)) {
                    if (DEBUG) {
                        Log.d(TAG, "[mPlayListener] : 没收到切歌指令，只收到了此播放指令，立即切歌播放");
                    }
                    mPlayer.refreshMusic(entity.curr, entity.currUser);
                    mPlayer.localPlay();
                } else if (DEBUG) {
                    Log.d(TAG, "[mPlayListener] : 播放指令，歌曲正确，立即播放");
                }
                mPlayer.remotePlay();
            } else {
                Log.d(TAG, "[mPlayListener] : entity == null");
            }
        }
    };
    private Emitter.Listener mLikeDislikeListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (DEBUG) {
                Log.d(TAG, "[mLikeDislikeListener] : " + args[0]);
            }
            String msg = args[0].toString();
            ReplyWrapper<LikeDislike> rw = ReplyWrapper.parse(msg, LikeDislike.LIST_PARSER);
            LikeDislike entity = rw.getData();
            if (null == entity) {
                if (DEBUG) {
                    Log.d(TAG, "[mLikeDislikeListener] : entity == null");
                }
                return;
            }
            MusicEntity music = entity.music;
            SocialUser user = entity.user;
            if (null == music || null == user) {
                if (DEBUG) {
                    Log.d(TAG, "[mLikeDislikeListener] : entity.music == null");
                }
                return;
            }
            MusicEntity me = mPlayer.fetchCurrentMusic();
            if (null == me) {
                if (DEBUG) {
                    Log.d(TAG, "[mLikeDislikeListener] : No local music playing now.");
                }
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "[mLikeDislikeListener] : entity.like = " + music.getLike());
                Log.d(TAG, "[mLikeDislikeListener] : entity.dislike = " + music.getDislike());
            }
            if (music.getLike() != me.getLike()) {
                me.setLike(music.getLike());
            }
            if (music.getDislike() != me.getDislike()) {
                me.setDislike(music.getDislike());
            }
            if (entity.type > 0) {
                mH.obtainMessage(MSG_TOAST, 0, 0, getString(R.string.like_music, user.getUserName())).sendToTarget();
            } else if (entity.type < 0) {
                mH.obtainMessage(MSG_TOAST, 0, 0, getString(R.string.dislike_music, user.getUserName())).sendToTarget();
            }
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_LIKE_DISLIKE_CHANGED_BROADCAST);
            intent.putExtra(Constants.KEY_LIKE, music.getLike());
            intent.putExtra(Constants.KEY_DISLIKE, music.getDislike());
            SysUtils.sendBroadcast(ChannelService.this, intent);
        }
    };

    private Emitter.Listener mDisconnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (DEBUG) {
                Log.d(TAG, "[mDisconnectListener] : " + args[0]);
            }
            mPlayer.clearMusic();
            ChannelNotification.getInstance().serviceStopFront(ChannelService.this);
        }
    };

}
