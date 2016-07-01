package com.nao.im.net.login;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.nao.im.App;
import com.nao.im.GMEnv;
import com.nao.im.model.login.SocialUser;

public class TencentSSOAgent implements ISSOAgent {

    //TODO 文档说明需要设置onActivityResult，当前没有设置，貌似没有问题呢 :)
    //TODO 可能引起的问题：在某些低端机上调用登录后，由于内存紧张导致APP被系统回收，登录成功后无法成功回传数据。
    private static final boolean DEBUG = GMEnv.DEBUG;
    private static final String TAG = DEBUG ? "TencentSSOAgent" : TencentSSOAgent.class.getSimpleName();
    public static final String TENCENT_APP_ID = "1104839786";
    public static final String TENCENT_SCOPE = "all";

    private static TencentSSOAgent sInstance;

    private Tencent mTencent;

    private TencentSSOAgent(){}

    synchronized public static TencentSSOAgent getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new TencentSSOAgent();
            sInstance.mTencent = Tencent.createInstance(TENCENT_APP_ID, context);
        }
        return sInstance;
    }

    public void login(Activity activity, IUiListener listener) {
        if (DEBUG) {
            Log.i(TAG, "[login] : isSupportSSO: " + (mTencent.isSupportSSOLogin(activity)));
        }
        if (!SocialUserManager.getInstance().isUserLoginAndTokenValid()) {
            if (DEBUG) {
                Log.i(TAG, "[login] : token is not valid");
            }
            mTencent.login(activity, TENCENT_SCOPE, listener);
        }
    }

    public void logout() {
        if (DEBUG) {
            Log.i(TAG, "[logout]");
        }
        if (SocialUserManager.getInstance().isUserLoginAndTokenValid()) {
            if (DEBUG) {
                Log.i(TAG, "[logout] : token is valid");
            }
            mTencent.logout(App.getContext());
        }
        SocialUserManager.getInstance().clear();
    }

    public void getUserInfo(Context context, IUiListener listener) {
        if (DEBUG) {
            Log.d(TAG, "[getUserInfo]");
        }
        if (SocialUserManager.getInstance().isTokenValid()) {
            if (DEBUG) {
                Log.d(TAG, "[getUserInfo] : token is valid");
            }
            QQToken token = new QQToken(TENCENT_APP_ID);
            //3600是随意定义的，不影响token的使用
            SocialUser user = SocialUserManager.getInstance().getSocialUser();
            token.setAccessToken(user.getToken(), "3600");
            token.setOpenId(user.getOpenId());
            UserInfo info = new UserInfo(context, token);
            info.getUserInfo(listener);
        } else {
            if (DEBUG) {
                Log.e(TAG, "[getUserInfo] : token is invalid");
            }
            listener.onError(null);
        }
    }

}
