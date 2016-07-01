package com.nao.im.ui.activity.player;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nao.im.GMConstants;
import com.nao.im.R;
import com.nao.im.model.Channel;
import com.nao.im.model.MusicEntity;
import com.nao.im.model.login.SocialUser;
import com.nao.im.net.login.SocialUserManager;
import com.nao.im.player.AVIMManager;
import com.nao.im.player.ChannelService;
import com.nao.im.player.IAVIMManager;
import com.nao.im.player.IChannelService;
import com.nao.im.player.MusicPlayState;
import com.nao.im.ui.activity.main.MainActivity;
import com.nao.im.ui.activity.view.BaseAnimationListener;
import com.nao.im.ui.activity.view.CircleDrawable;
import com.nao.im.ui.activity.view.CircleTransform;
import com.nao.im.util.UiUtils;
import com.squareup.picasso.Picasso;

/**
 * Created by chaopei on 2015/11/6.
 */
public class MusicPlayerFragment extends Fragment implements View.OnClickListener {

    private static final boolean DEBUG = false;

    private static final String TAG = DEBUG ? "GmMusicPlayerFragment" : MusicPlayerFragment.class.getSimpleName();

    View mUsers, mLike, mDislike;

    MainActivity mActivity;

    private View mDebugLayout;
    private TextView mDebugStateTextView, mDebugProgressTextView, mDebugSecondProgressTextView;

    private TextView mLikeExtra, mMusicTextView, mUserName, mTitle;
    private ImageView mMusicImg, mUserIcon, mUserBg;

    int mScreenHeight;
    int mContentHeight;
    int mTitleHeight;
    int mExceptStatusHeight;

    private SearchMusicFragment mSearchMusicFragment;
    private View mRightButton;
    private boolean searchShow = false;

    private void switchSearchFragment() {
        switchSearchFragment(!searchShow);
    }

    private void switchSearchFragment(final boolean show) {
        mRightButton.setEnabled(false);
        searchShow = show;
        mActivity.onSearchSwitch(show);
        float start, end;
        if (!searchShow) {
            start = 45;
            end = 0;
        } else {
            start = 0;
            end = 45;
        }
        RotateAnimation animation = new RotateAnimation(start, end, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
        animation.setDuration(200);
        animation.setAnimationListener(new BaseAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mRightButton.setEnabled(true);
            }

            @Override
            public void onAnimationStart(Animation animation) {
                FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        android.R.anim.fade_in, android.R.anim.fade_out);
                if (show) {
                    if (null == mSearchMusicFragment) {
                        mSearchMusicFragment = new SearchMusicFragment();
                        transaction
                                .add(R.id.search_content, mSearchMusicFragment)
                                .commit();
                    } else {
                        transaction
                                .show(mSearchMusicFragment)
                                .commit();
                    }
                } else {
                    transaction
                            .hide(mSearchMusicFragment)
                            .commit();
                }
            }
        });
        mRightButton.startAnimation(animation);
    }

    private final static int MSG_TOAST = 2, MSG_UI_CURR_USER = 1, MSG_MUSIC_INFO = 0, MSG_ALL_USER_ONLINE = 3;
    Handler mH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mActivity.isFinishing()) {
                return;
            }
            switch (msg.what) {
                case MSG_MUSIC_INFO:
                    refreshInfo();
                    break;
                case MSG_TOAST:
                    Toast.makeText(mActivity, msg.obj + "", Toast.LENGTH_LONG).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
        mScreenHeight = UiUtils.getScreenHeight(mActivity);
        mExceptStatusHeight = mScreenHeight - UiUtils.getStatusHeight();
        mTitleHeight = UiUtils.dp2px(R.dimen.av_dp_58);
        mContentHeight = mExceptStatusHeight - mTitleHeight;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_music_player, container, false);
        initViews(v);
        refreshInfo();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initViews(View parent) {
        parent.getLayoutParams().height = UiUtils.getDisplayHeight(mActivity) - UiUtils.getStatusHeight();
        mMusicTextView = (TextView) parent.findViewById(R.id.music_name);
        mUserName = (TextView) parent.findViewById(R.id.user_name);
        mUserIcon = (ImageView) parent.findViewById(R.id.user_icon);
        mLike = parent.findViewById(R.id.btn_like);
        mLike.setOnClickListener(this);
        mDislike = parent.findViewById(R.id.btn_dislike);
        mDislike.setOnClickListener(this);
        mUserBg = (ImageView) parent.findViewById(R.id.user_icon_bg);
        mUserBg.setImageDrawable(new CircleDrawable(mActivity, getResources().getColor(R.color.color_1ca50), getResources().getColor(R.color.color_0fc60)));
        mTitle = (TextView) parent.findViewById(R.id.title);
        mRightButton = parent.findViewById(R.id.search);
        mRightButton.setOnClickListener(this);
//        parent.findViewById(R.id.play_state).setBackgroundDrawable(new PlayStateDrawable(mActivity));
        if (DEBUG) {
            mDebugStateTextView = (TextView) parent.findViewById(R.id.state);
            mDebugProgressTextView = (TextView) parent.findViewById(R.id.progress);
            mDebugSecondProgressTextView = (TextView) parent.findViewById(R.id.second_progress);
            mDebugLayout = parent.findViewById(R.id.debug_layout);
            mDebugLayout.setVisibility(View.VISIBLE);
        }
    }

    private void refreshInfo() {
        try {
            IChannelService service = ChannelService.getService();
            if (!service.isEnable()) {
                return;
            }
            Channel channel = ChannelService.getService().getChannel();
            if (null == channel) {
                return;
            }
            String channelName = channel.getChannelName();
            mTitle.setText(channelName);
//            mActivity.setMusicCount(service.getMusicCount());
//            mUserCountText.setText(String.valueOf(service.getUserCount()));
            MusicEntity music = service.fetchCurrentMusic();
            if (DEBUG) {
                mDebugStateTextView.setText(getDebugStateString());
            }
            if (null != music) {
                mMusicTextView.setText(music.getTitle() + " - " + music.getArtist());
                int wh = UiUtils.dp2px(R.dimen.av_dp_200);
//                Picasso.with(mActivity).load(music.getCover()).resize(wh, wh).into(mMusicImg);
//                mLikeExtra.setText("" + (music.getLike() - music.getDislike()));
//                int commendType = service.getCommend();
//                if (commendType > MusicPlayer.Constants.COMMEND_NEW) {
//                    mLike.setSelected(true);
//                    mNextMusic.setSelected(false);
//                } else if (commendType < MusicPlayer.Constants.COMMEND_NEW) {
//                    mLike.setSelected(false);
//                    mNextMusic.setSelected(true);
//                } else {
//                    mLike.setSelected(false);
//                    mNextMusic.setSelected(false);
//                }
                if (DEBUG) {
                    String state = MusicPlayState.getName(service.fetchState());
                    int bp = service.fetchBufferedPercent();
                    mDebugStateTextView.setText(getDebugStateString() + "\n" + music.toString() + "\nstate:" + state + "\n在线人数:" + service.getUserCount() + "\n还有歌曲数:" + service.getMusicCount());
                    mDebugSecondProgressTextView.setText("buffer:" + bp + "/100");
                }
                SocialUser user = service.getCurrUser();
                if (null != user) {
                    mUserName.setText(user.getUserName());
                    Picasso.with(mActivity).load(user.getIconUrl()).transform(new CircleTransform()).into(mUserIcon);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            mH.sendEmptyMessageDelayed(MSG_MUSIC_INFO, 2000);
        }
    }

    private String getDebugStateString() throws RemoteException {
        IChannelService service = ChannelService.getService();
        IAVIMManager manager = AVIMManager.getInstance();
        if (!service.isEnable()) {
            return null;
        }
        String connState = service.isChannelConnected() ? "channel connected" : "channel Disconnected";
        String joinState = service.isChannelJoined() ? "channel joined" : "channel NOT joined";
        String imLoginState = manager.isLogin() ? "IM Login" : "IM NOT Login";
        String imJoinState = manager.isJoined() ? "IM joined" : "IM NOT joined";
        return connState + "\n" + joinState + "\n" + imLoginState + "\n" + imJoinState + "\nbuild " + GMConstants.BUILD;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dislike:
                loveClick(-5);
                break;
            case R.id.btn_like:
                loveClick(5);
                break;
            case R.id.search:
                switchSearchFragment();
                break;
        }
    }

    private void loveClick(final int type) {
        if (SocialUserManager.getInstance().isUserLogin()) {
            final IChannelService service = ChannelService.getService();
            if (null == service) {
                return;
            }
            SocialUser user = SocialUserManager.getInstance().getSocialUser();
            try {
                service.loveClick(type, user.getUserId(), user.getOpenId());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            mH.obtainMessage(MSG_TOAST, 0, 0, "没有登录").sendToTarget();
        }
    }

}
