package com.nao.im.ui.activity.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.nao.im.GMEnv;
import com.nao.im.R;
import com.nao.im.model.login.SocialUser;
import com.nao.im.net.GMDataCenter;
import com.nao.im.net.data.RequestManager;
import com.nao.im.net.login.ISSOAgent;
import com.nao.im.net.login.ISocialLoginCallback;
import com.nao.im.net.login.SocialUserManager;
import com.nao.im.net.login.TencentSSOAgent;
import com.nao.im.net.parser.IReplyListener;
import com.nao.im.net.parser.ReplyWrapper;
import com.nao.im.player.ChannelService;
import com.nao.im.ui.activity.view.MarqueeTextView;
import com.squareup.picasso.Picasso;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by fanxiafei on 2015/9/2.
 * <p/>
 * 加载数据和登录，在数据加载完毕后开始动画
 * <p/>
 * <p/>
 * SocialSSOAgent.isUserLogin() 判断用户是否已经登录的接口
 * SocialUser.getInstance().getUserId 获取用户ID
 * SocialUser.getInstance().getIconUrl 获取用户头像的URL
 * SocialUser.getInstance().getUserName  获取用户名字
 */
public class SplashActivity extends Activity implements View.OnClickListener {

    private static final boolean DEBUG = GMEnv.DEBUG;
    private static final String TAG = DEBUG ? "SplashActivity" : SplashActivity.class.getSimpleName();
    private static final int MSG_WHAT_START_ANIMATIAN = 0;//开始移动 此刻now
    private static final int MSG_WHAT_TOAST_LOGIN_FAILED = 1;//登录失败
    private static final int MSG_WHAT_TOAST_LOGIN_SUCCESS = 2;//登录成功
    private static final int MSG_WHAT_TOAST_LOGIN_CANCEL = 3;//登录成功

    private Button mEnterBar;//登录和进入频道按钮
    private String iconUrl = "http://diy.qqjay.com/u2/2013/0910/73209c6b16e7956bbe66215776df7548.jpg";

    private boolean isLogging = false;

    private View mLogo, mDesc, mBg;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_START_ANIMATIAN:
                    break;
                case MSG_WHAT_TOAST_LOGIN_FAILED:
                    refreshUI();
                    Toast.makeText(SplashActivity.this, R.string.social_sso_login_failed, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_WHAT_TOAST_LOGIN_SUCCESS:
                    refreshUI();
                    Toast.makeText(SplashActivity.this, R.string.social_sso_login_success, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_WHAT_TOAST_LOGIN_CANCEL:
                    refreshUI();
                    Toast.makeText(SplashActivity.this, R.string.social_sso_login_failed, Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
        initAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //已经认证的，跳转到其他页面
    }

    private void initView() {
        mEnterBar = (Button) findViewById(R.id.splash_enter_bottom);
        mLogo = findViewById(R.id.splash_logo);
        mDesc = findViewById(R.id.splash_desc);
        mBg = findViewById(R.id.splash_bg);
    }

    public void initAnimation() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.splash_sliding_up);
        anim.setStartOffset(1000);
        anim.setFillAfter(true);
        mLogo.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.splash_sliding_up);
        anim.setStartOffset(1200);
        anim.setFillAfter(true);
        mDesc.startAnimation(anim);

        anim = new AlphaAnimation(0, 1.0f);
        anim.setDuration(500);
        anim.setStartOffset(1700);
        anim.setFillAfter(true);
        mEnterBar.startAnimation(anim);

        anim = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(8000);
        anim.setFillAfter(true);
        mBg.startAnimation(anim);

    }


    private void refreshUI() {
        if (isLogging) {
            return;
        }
        mEnterBar.setEnabled(true);
        if (SocialUser.TYPE_UNKNOWN == SocialUserManager.getInstance().getSocialUser().getType()) {
            mEnterBar.setText(getResources().getString(R.string.splash_enter_message));//没有登录的显示  使用qq账号登录
            mEnterBar.setOnClickListener(SplashActivity.this);
        } else {
            //登录过的验证token是否有效
            if (SocialUserManager.getInstance().isTokenValid()) {
                mEnterBar.setText(getResources().getString(R.string.splash_enter_message_have_login));//已经登录的显示 进入频道
                mEnterBar.setOnClickListener(SplashActivity.this);
            } else {
                Toast.makeText(SplashActivity.this, getResources().getString(R.string.social_sso_login_token_invalid), Toast.LENGTH_LONG).show();
                mEnterBar.setText(getResources().getString(R.string.splash_enter_message_login));//token过期的显示  重新登录
                mEnterBar.setOnClickListener(SplashActivity.this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mEnterBar) {
            if (!SocialUserManager.getInstance().isUserLoginAndTokenValid()) {
                isLogging = true;
                mEnterBar.setText("正在登录...");
                mEnterBar.setEnabled(false);
                login(TencentSSOAgent.getInstance(SplashActivity.this), SplashActivity.this, mLoginCallback);
            } else {
                loadUserInfo(SocialUserManager.getInstance().getSocialUser());
            }
        }
    }

    /**
     * 获取用户登录信息的回调
     */
    private ISocialLoginCallback mLoginCallback = new ISocialLoginCallback() {

        @Override
        public void onGetUserInfoSuccess(final SocialUser user) {
            isLogging = false;
            loadUserInfo(user);
            mHandler.sendEmptyMessage(MSG_WHAT_TOAST_LOGIN_SUCCESS);
        }

        @Override
        public void onGetTokenSuccess() {
        }

        @Override
        public void onFailure() {
            isLogging = false;
            mHandler.sendEmptyMessage(MSG_WHAT_TOAST_LOGIN_FAILED);
        }

        @Override
        public void onCancel() {
            isLogging = false;
            mHandler.sendEmptyMessage(MSG_WHAT_TOAST_LOGIN_CANCEL);
        }
    };

    /**
     * 拉自己的头像和昵称信息
     * 拉成功后跳转到musicplayerActivity界面
     *
     * @param user
     */
    private void loadUserInfo(final SocialUser user) {
        if (DEBUG) {
            Log.d(TAG, "[loadUserInfo] : " + user.toString());
        }
        Picasso.with(this).load(user.getIconUrl()).fetch();
        startMusicActivity();
    }

    public void startMusicActivity() {
        try {
            while (!ChannelService.getService().isEnable()) ;
            ChannelService.getService().connectAndJoinChannel();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RequestManager.cancelAll(this);
    }

    /**
     * 看名字就知道是干嘛的
     *
     * @param activity
     * @param callback
     */
    public static void login(final ISSOAgent agent, final Activity activity, final ISocialLoginCallback callback) {
        agent.logout();
        agent.login(activity, new IUiListener() {
            @Override
            public void onComplete(Object o) {
                if (DEBUG) {
                    Log.i(TAG, "[login] : onComplete info=" + o.toString());
                }
                try {
                    JSONObject info = new JSONObject(o.toString());
                    final String openId = info.getString("openid");
                    final String token = info.getString("access_token");
                    final long expiresIn = info.getLong("expires_in");
                    final SocialUser userTencent = SocialUserManager.getInstance().getSocialUser();
                    userTencent.setToken(token);
                    userTencent.setOpenId(openId);
                    userTencent.setExpiresTime(System.currentTimeMillis() + expiresIn * 1000L);
                    callback.onGetTokenSuccess();

                    agent.getUserInfo(activity, new IUiListener() {
                        @Override
                        public void onComplete(Object o) {
                            try {
                                JSONObject user = new JSONObject(o.toString());
                                String name = user.getString("nickname");
                                String iconUrl = user.getString("figureurl_qq_2").replace("\\", "");
                                if (DEBUG) {
                                    Log.d(TAG, "[login] : getSocialUser  userTencent=" + userTencent);
                                }
                                userTencent.setUserName(name);
                                userTencent.setIconUrl(iconUrl);
                                userTencent.setType(SocialUser.TYPE_QQ);
                                Gson gson = new Gson();
                                GMDataCenter.postRegisterOrLogin(activity, new IReplyListener<SocialUser>() {
                                    @Override
                                    public void onReplyParserResponse(List<SocialUser> response) { // 此时获得的回应只有userId
                                        if (DEBUG) {
                                            Log.d(TAG, "[onReplyParserResponse]");
                                        }
                                        if (null != response && 1 == response.size()) {
                                            if (DEBUG) {
                                                Log.d(TAG, "[onReplyParserResponse] : userId = " + response.get(0).getUserId());
                                            }
                                            userTencent.setUserID(response.get(0).getUserId());
                                            SocialUserManager.getInstance().persistLocal();
                                            callback.onGetUserInfoSuccess(userTencent);
                                        } else {
                                            if (DEBUG) {
                                                Log.d(TAG, "[onReplyParserResponse] : response.size=" + response.size());
                                            }
                                            agent.logout();
                                            callback.onFailure();
                                        }
                                    }

                                    @Override
                                    public void onReplyParserError(ReplyWrapper<SocialUser> wrapper) {
                                        if (DEBUG) {
                                            Log.e(TAG, "[onReplyParserError]");
                                        }
                                        agent.logout();
                                        callback.onFailure();
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (DEBUG) {
                                            Log.e(TAG, "[onErrorResponse]", error);
                                        }
                                        agent.logout();
                                        callback.onFailure();
                                    }
                                }, gson.toJson(userTencent));
                            } catch (JSONException e) {
                                if (DEBUG) {
                                    e.printStackTrace();
                                }
                                callback.onFailure();
                            }
                        }

                        @Override
                        public void onError(UiError uiError) {
                            callback.onFailure();
                        }

                        @Override
                        public void onCancel() {
                            callback.onCancel();
                        }
                    });
                } catch (JSONException e) {
                    if (DEBUG) {
                        e.printStackTrace();
                    }
                    callback.onFailure();
                }
            }

            @Override
            public void onError(UiError uiError) {
                callback.onFailure();
            }

            @Override
            public void onCancel() {
                callback.onCancel();
            }
        });
    }
}
