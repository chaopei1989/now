package com.nao.im.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;

import com.nao.im.App;
import com.nao.im.GMEnv;
import com.nao.im.model.MusicEntity;
import com.nao.im.model.login.SocialUser;
import com.nao.im.ui.activity.view.CircleTransform;
import com.nao.im.util.SysUtils;
import com.squareup.picasso.Picasso;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicPlayer implements OnCompletionListener, OnErrorListener, OnBufferingUpdateListener, OnPreparedListener, MediaPlayer.OnInfoListener {

    private final static boolean DEBUG = GMEnv.DEBUG;

    private final static String TAG = DEBUG ? "MusicPlayer" : MusicPlayer.class.getSimpleName();

    private final static long SLEEP_TIME = 1000;

    public static class Constants {
        /**
         * 播放服务接收此广播，开始播放当前音乐
         */
        public static final String ACTION_MUSIC_PLAY = "ACTION_MUSIC_PLAY"; // 开始播放
        /**
         * 播放服务接收此广播，重新播放当前音乐
         */
        public static final String ACTION_MUSIC_REPLAY = "ACTION_MUSIC_REPLAY"; // 重新播放
        /**
         * 播放服务接收此广播，暂停播放当前音乐
         */
        public static final String ACTION_MUSIC_PAUSE = "ACTION_MUSIC_PAUSE"; // 暂停播放
        /**
         * 播放服务接收此广播，停止播放
         */
        public static final String ACTION_MUSIC_STOP = "ACTION_MUSIC_STOP"; // 停止播放
        /**
         * 播放服务接收此广播，播放下一首
         */
        public static final String ACTION_MUSIC_NEXT = "ACTION_MUSIC_NEXT"; // 播放下一曲
        /**
         * 播放服务接收此广播，播放上一首
         */
        public static final String ACTION_MUSIC_PREV = "ACTION_MUSIC_PREV"; // 播放上一曲
        /**
         * 播放服务接收此广播，进度调整
         */
        public static final String ACTION_SEEK_TO = "ACTION_SEEK_TO"; // 调节进度
        /**
         * 播放服务接收此广播，停止播放，清空列表
         */
        public static final String ACTION_EXIT = "ACTION_EXIT";
        public static final String KEY_MUSIC_PARCELABLE_DATA = "KEY_MUSIC_PARCELABLE_DATA";
        public static final String KEY_MUSIC_TOTAL_DURATION = "KEY_MUSIC_TOTAL_DURATION";
        public final static String KEY_MUSIC_CURRENT_DURATION = "KEY_MUSIC_CURRENT_DURATION";
        public final static String KEY_MUSIC_DURATION = "KEY_MUSIC_DURATION";
        public final static String KEY_MUSIC_SECOND_PROGRESS = "KEY_MUSIC_SECOND_PROGRESS";
        public final static String KEY_PLAYER_SEEK_TO_PROGRESS = "KEY_PLAYER_SEEK_TO_PROGRESS";
        public final static String KEY_MUSIC_INFO = "KEY_MUSIC_INFO";
        public final static String KEY_USER_INFO = "KEY_USER_INFO";

        /**
         * 播放服务发送的广播，指明当前播放的音乐和总长度
         */
        public static final String ACTION_MUSIC_BUNDLE_BROADCAST = "ACTION_MUSIC_BUNDLE_BROADCAST";
        /**
         * 播放服务发送的广播，指明当前播放的进度
         */
        public final static String ACTION_MUSIC_CURRENT_PROGRESS_BROADCAST = "ACTION_MUSIC_CURRENT_PROGRESS_BROADCAST";
        /**
         * 播放服务发送的广播，指明当前播放的音乐的下载进度
         */
        public final static String ACTION_MUSIC_SECOND_PROGRESS_BROADCAST = "ACTION_MUSIC_SECOND_PROGRESS_BROADCAST";
        public final static String ACTION_MUSIC_CHANGE_BROADCAST = "action_music_change_broadcast";

        public static final int EVENT_BEGIN = 0X100;
        public static final int EVENT_REFRESH_DATA = EVENT_BEGIN + 10;
        public static final int EVENT_LOAD_MORE_DATA = EVENT_BEGIN + 20;
        public static final int EVENT_START_PLAY_MUSIC = EVENT_BEGIN + 30;
        public static final int EVENT_STOP_PLAY_MUSIC = EVENT_BEGIN + 40;

        public static final int COMMEND_NEW = 0, COMMEND_WAIT = 10, COMMEND_ERR = -10;
    }

    private static MusicPlayer instance;

    public static MusicPlayer getInstance() {
        if (null == instance) {
            instance = new MusicPlayer(App.getContext());
        }
        return instance;
    }

    private MediaPlayer mMediaPlayer;

    /**
     * 当前点歌人
     */
    private SocialUser mCurrUser;

    private List<MusicEntity> mMusicList;

    /**
     * 当前的歌是否点过赞
     * 0-新歌，10-等待结果，其他-赞或者踩
     */
    private int commendType;

    public void setCommendType(int commendType) {
        this.commendType = commendType;
    }

    public int getCommendType() {
        return commendType;
    }

    private int mCurPlayIndex;

    private int mPlayState;

    private int mPLayMode;

    private Random mRandom;

    private Context mContext;

    private MusicPlayer(Context context) {
        initParameter(context);
    }

    /**
     * 创建一个自定义缓冲区的MediaPlayer
     *
     * @param context
     * @param stream
     * @return
     */
    private MediaPlayer create(Context context, InputStream stream) {
        MediaPlayer mediaplayer = null;
        try {
            File temp = File.createTempFile("mediaplayertmp", "temp");
            String tempPath = temp.getAbsolutePath();
            FileOutputStream out = new FileOutputStream(temp);
            //用BufferdOutputStream速度快
            BufferedOutputStream bis = new BufferedOutputStream(out);
            byte buf[] = new byte[128];
            do {
                int numread = stream.read(buf);
                if (numread <= 0)
                    break;
                bis.write(buf, 0, numread);
            } while (true);
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(tempPath);
            mp.prepare();
            mediaplayer = mp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaplayer;
    }

    private void initParameter(Context context) {
        mContext = context;

        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);

        mMusicList = new ArrayList<MusicEntity>();

        mCurPlayIndex = -1;
        mPlayState = MusicPlayState.MPS_LIST_EMPTY;
        mPLayMode = MusicPlayMode.MPM_ONLY_ONE_PLAY;

        mRandom = new Random();
        mRandom.setSeed(System.currentTimeMillis());
    }

    synchronized public void exit() {
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMusicList.clear();
        mCurPlayIndex = -1;
        mPlayState = MusicPlayState.MPS_LIST_EMPTY;
    }

    /**
     * 用指定音乐实例替换播放列表
     *
     * @param entity
     */
    public void refreshMusic(MusicEntity entity, SocialUser currUser) {
        setCommendType(Constants.COMMEND_NEW);
        mMusicList.clear();
        mMusicList.add(entity);
        mCurrUser = currUser;
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_MUSIC_CHANGE_BROADCAST);
        intent.putExtra(Constants.KEY_MUSIC_INFO, entity);
        intent.putExtra(Constants.KEY_USER_INFO, currUser);
        SysUtils.sendBroadcast(mContext, intent);
        try {
            Bitmap bitmap = Picasso.with(App.getContext()).load(entity.getCover()).resize(100, 100).transform(new CircleTransform()).get();
            ChannelNotification.getInstance().setInfo(entity.getTitle(), bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPlayState = MusicPlayState.MPS_LIST_FULL;
    }

    synchronized public void clearMusic() {
        if (0 <= mCurPlayIndex && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mCurPlayIndex = -1;
        mMusicList.clear();
        mPlayState = MusicPlayState.MPS_LIST_EMPTY;
    }

    public void addMusic(MusicEntity entity) {
        if (entity == null) {
            return;
        }

        mMusicList.add(entity);

        if (mMusicList.size() == 0) {
            mPlayState = MusicPlayState.MPS_LIST_EMPTY;
            mCurPlayIndex = -1;
            return;
        }

        mPlayState = MusicPlayState.MPS_LIST_FULL;
    }

    public int getMusicListCount() {
        return null == mMusicList || mMusicList.isEmpty() ? 0 : mMusicList.size();
    }

    public int getPlayState() {
        return mPlayState;
    }

    public void playFirst() {
        preparedMusic(0);
    }

    public void localPlay() {
        preparedMusic(0);
    }

    public void play(int position) {
        preparedMusic(position);
    }

    public void replay() {
        if (mPlayState == MusicPlayState.MPS_LIST_EMPTY) {
            return;
        }

        mMediaPlayer.start();
        mPlayState = MusicPlayState.MPS_PLAYING;
        sendPlayCurrentPosition();
    }

    public void pause() {
        if (mPlayState != MusicPlayState.MPS_PLAYING) {
            return;
        }

        mMediaPlayer.pause();
        mPlayState = MusicPlayState.MPS_PAUSE;
    }

    public void stop() {
        if (mPlayState != MusicPlayState.MPS_PLAYING && mPlayState != MusicPlayState.MPS_PAUSE) {
            return;
        }

        mMediaPlayer.stop();
        mPlayState = MusicPlayState.MPS_STOP;
    }

    public void playNext() {
        mCurPlayIndex++;
        mCurPlayIndex = reviceIndex(mCurPlayIndex);

        preparedMusic(mCurPlayIndex);
    }

    /**
     * 准备播放前一首
     */
    public void playPrev() {
        mCurPlayIndex--;
        mCurPlayIndex = reviceIndex(mCurPlayIndex);

        preparedMusic(mCurPlayIndex);
    }

    public void seekTo(float rate) {
        if (mPlayState == MusicPlayState.MPS_LIST_EMPTY) {
            return;
        }

        float r = reviceSeekValue(rate);
        int time = mMediaPlayer.getDuration();
        int curTime = (int) (r * time);

        mMediaPlayer.seekTo(curTime);
    }

    public void seekTo(int time) {
        if (mPlayState == MusicPlayState.MPS_LIST_EMPTY) {
            return;
        }
        if (time < 0) {
            time = 0;
        }
        if (time > mMediaPlayer.getDuration()) {
            time = mMediaPlayer.getDuration();
        }
        mMediaPlayer.seekTo(time);
    }

    public int getCurPosition() {
        if (mPlayState == MusicPlayState.MPS_PLAYING || mPlayState == MusicPlayState.MPS_PAUSE) {
            return mMediaPlayer.getCurrentPosition();
        }

        return 0;
    }

    public int getDuration() {
        if (mPlayState == MusicPlayState.MPS_LIST_EMPTY) {
            return 0;
        }

        return mMediaPlayer.getDuration();
    }

    public void setPlayMode(int mode) {
        switch (mode) {
            case MusicPlayMode.MPM_SINGLE_LOOP_PLAY:
            case MusicPlayMode.MPM_ORDER_PLAY:
            case MusicPlayMode.MPM_LIST_LOOP_PLAY:
            case MusicPlayMode.MPM_RANDOM_PLAY:
                mPLayMode = mode;
                break;
        }
    }

    public int getPlayMode() {
        return mPLayMode;
    }

    private int reviceIndex(int index) {
        if (index < 0) {
            index = mMusicList.size() - 1;
        }

        if (index >= mMusicList.size()) {
            index = 0;
        }

        return index;
    }

    private float reviceSeekValue(float value) {
        if (value < 0) {
            value = 0;
        }

        if (value > 1) {
            value = 1;
        }

        return value;
    }

    private int getRandomIndex() {
        int size = mMusicList.size();
        if (size == 0) {
            return -1;
        }
        return Math.abs(mRandom.nextInt() % size);
    }

    private void preparedMusic(int index) {
        if (mPlayState == MusicPlayState.MPS_LIST_EMPTY || index < 0 || index >= getMusicListCount()) {
            return;
        }
        resetReadyFlags();
        mCurPlayIndex = index;

        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        String dataSource = mMusicList.get(mCurPlayIndex).getUrl();
        try {
            mMediaPlayer.setDataSource(dataSource);
            mMediaPlayer.prepareAsync();
            mPlayState = MusicPlayState.MPS_PREPARED;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (100 > mBufferedPercent) {
            if (DEBUG) {
                Log.d(TAG, "[onCompletion] : 音乐没有缓冲完导致的结束");
            }
            pendingPosition = getCurPosition();
            play(mCurPlayIndex);
        } else {
            Intent intent = new Intent(Constants.ACTION_MUSIC_CURRENT_PROGRESS_BROADCAST);
            intent.putExtra(Constants.KEY_MUSIC_CURRENT_DURATION, getDuration());
            intent.putExtra(Constants.KEY_MUSIC_SECOND_PROGRESS, mBufferedPercent);
            intent.putExtra(Constants.KEY_MUSIC_DURATION, getDuration());
            SysUtils.sendBroadcast(mContext, intent);
        }
        switch (mPLayMode) {
            case MusicPlayMode.MPM_SINGLE_LOOP_PLAY:
                if (DEBUG) {
                    Log.d(TAG, "[onCompletion] : MPM_SINGLE_LOOP_PLAY, 重新播放当前音乐");
                }
                play(mCurPlayIndex);
                break;
            case MusicPlayMode.MPM_ORDER_PLAY:
                if (mCurPlayIndex != mMusicList.size() - 1) {
                    if (DEBUG) {
                        Log.d(TAG, "[onCompletion] : MPM_ORDER_PLAY, 播放下一首");
                    }
                    playNext();
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "[onCompletion] : MPM_ORDER_PLAY, 播放到列表最后了，停止");
                    }
                    stop();
                }
                break;
            case MusicPlayMode.MPM_LIST_LOOP_PLAY:
                if (mCurPlayIndex != mMusicList.size() - 1) {
                    if (DEBUG) {
                        Log.d(TAG, "[onCompletion] : MPM_LIST_LOOP_PLAY, 播放下一首");
                    }
                    playNext();
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "[onCompletion] : MPM_LIST_LOOP_PLAY, 循环从第一首播放");
                    }
                    playFirst();
                }
                break;
            case MusicPlayMode.MPM_RANDOM_PLAY:
                if (DEBUG) {
                    Log.d(TAG, "[onCompletion] : MPM_RANDOM_PLAY, 随机播放");
                }
                int index = getRandomIndex();
                if (index != -1) {
                    mCurPlayIndex = index;
                } else {
                    mCurPlayIndex++;
                }
                mCurPlayIndex = reviceIndex(mCurPlayIndex);

                play(mCurPlayIndex);
                break;
            case MusicPlayMode.MPM_ONLY_ONE_PLAY:
                if (DEBUG) {
                    Log.d(TAG, "[onCompletion] : MPM_ONLY_ONE_PLAY，停止");
                }
                stop();
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (DEBUG) {
            Log.e(TAG, String.format("[onError]!!!  what=%d, extra=%d", what, extra));
            Log.e(TAG, "[onError] : state=" + MusicPlayState.getName(getPlayState()));
            Log.e(TAG, "[onError] : pendingPosition=" + pendingPosition);
        }
        play(mCurPlayIndex);
        return true;
    }

    /**
     * 缓冲百分比
     */
    private int mBufferedPercent;

    /**
     * 当由于网络问题缓冲一半失败时，记住播放位置，不断尝试继续播放
     */
    private int pendingPosition;

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (DEBUG) {
            Log.d(TAG, "[onInfo] : what=" + what + ", extra=" + extra);
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (DEBUG) {
            Log.d(TAG, "second percent --> " + percent);
        }
        mBufferedPercent = percent;
//        if (percent <= 100) {
//            Intent intent = new Intent();
//            intent.setAction(Constants.ACTION_MUSIC_SECOND_PROGRESS_BROADCAST);
//            intent.putExtra(Constants.KEY_MUSIC_SECOND_PROGRESS, percent);
//            SysUtils.sendBroadcast(mContext, intent);
//        }
    }

    boolean isRemoteReady = false, isLocalReady = false;

    private void resetReadyFlags() {
        isRemoteReady = isLocalReady = false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (DEBUG) {
            Log.d(TAG, "[onPrepared], isLocalReady=" + isLocalReady);
            Log.d(TAG, "[onPrepared], isRemoteReady=" + isRemoteReady);
        }
        isLocalReady = true;
        if (isRemoteReady) {
            playNowWithoutPrepare();
        }
    }

    public void remotePlay() {
        if (DEBUG) {
            Log.d(TAG, "[remotePlay], isLocalReady=" + isLocalReady);
            Log.d(TAG, "[remotePlay], isRemoteReady=" + isRemoteReady);
        }
        isRemoteReady = true;
        if (isLocalReady) {
            playNowWithoutPrepare();
        }
    }

    public void playNowWithoutPrepare() {
        if (DEBUG) {
            Log.d(TAG, "[playNowWithoutPrepare]");
        }
        resetReadyFlags();
        seekTo(pendingPosition);
        pendingPosition = 0;
        mMediaPlayer.start();
        mPlayState = MusicPlayState.MPS_PLAYING;
        sendPlayBundle();
        sendPlayCurrentPosition();
    }

    private void sendPlayBundle() {
        Intent intent = new Intent(Constants.ACTION_MUSIC_BUNDLE_BROADCAST);
        Bundle extras = new Bundle();
        extras.putInt(Constants.KEY_MUSIC_TOTAL_DURATION, getDuration());
        extras.putParcelable(Constants.KEY_MUSIC_PARCELABLE_DATA, mMusicList.get(mCurPlayIndex));
        intent.putExtras(extras);
        SysUtils.sendBroadcast(mContext, intent);
    }

    Thread positionThread;

    private void sendPlayCurrentPosition() {
        if(null != positionThread) {
            positionThread.interrupt();
        }
        positionThread = new Thread() {
            public void run() {
                try {
                    Intent intent = new Intent(Constants.ACTION_MUSIC_CURRENT_PROGRESS_BROADCAST);
                    while (mMediaPlayer.isPlaying() && !isInterrupted()) {
                        if (DEBUG) {
                            Log.d(TAG, "primary percent --> " + getCurPosition() + "/" + getDuration());
                        }
                        intent.putExtra(Constants.KEY_MUSIC_CURRENT_DURATION, getCurPosition());
                        intent.putExtra(Constants.KEY_MUSIC_SECOND_PROGRESS, mBufferedPercent);
                        intent.putExtra(Constants.KEY_MUSIC_DURATION, getDuration());
                        SysUtils.sendBroadcast(mContext, intent);
                        sleep(SLEEP_TIME);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        positionThread.start();
    }

    /**
     * 获取播放状态
     *
     * @return
     */
    public int fetchState() {
        if (!App.isChannelProcess()) {
            throw new RuntimeException();
        }
        return mPlayState;
    }

    /**
     * 获取音乐缓冲百分比
     *
     * @return
     */
    public int fetchBufferedPercent() {
        if (!App.isChannelProcess()) {
            throw new RuntimeException();
        }
        return mBufferedPercent;
    }

    /**
     * 获取当前播放的音乐信息
     *
     * @return
     */
    public MusicEntity fetchCurrentMusic() {
        if (!App.isChannelProcess()) {
            throw new RuntimeException();
        }
        return mCurPlayIndex < 0 ? null : mMusicList.get(mCurPlayIndex);
    }

    /**
     * 获取当前点歌人信息
     *
     * @return
     */
    public SocialUser fetchCurrentUser() {
        if (!App.isChannelProcess()) {
            throw new RuntimeException();
        }
        return mCurrUser;
    }
}
