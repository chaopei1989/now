package com.nao.im.net.login;

import android.util.Log;

import com.nao.im.GMEnv;
import com.nao.im.model.login.SocialUser;

/**
 * Created by chaopei on 2015/8/14.
 * 帐号管理类
 */
public class SocialUserManager {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "SocialUserManager" : SocialUserManager.class.getSimpleName();

    private static SocialUserManager sum;
    private SocialUser mUser;

    private SocialUserManager() {
    }

    synchronized public static SocialUserManager getInstance() {
        if (null == sum) {
            sum = new SocialUserManager();
            sum.mUser = LoginSharedPrefUtils.readSocialUser();
            if (null == sum.mUser) {
                sum.mUser = new SocialUser();
            }
        }
        return sum;
    }

    public SocialUser getSocialUser() {
        return mUser;
    }

    /**
     * 只检查用户登录信息是否完备，不检查token过期
     *
     * @return
     */
    public boolean isUserLogin() {
        if (null == mUser) {
            if (DEBUG) {
                Log.e(TAG, "[isUserLogin] : false, because mUser is null");
            }
            return false;
        }
        if (0 >= mUser.getType()) {
            if (DEBUG) {
                Log.e(TAG, "[isUserLogin] : false, because mUser.type=" + mUser.getType());
            }
            return false;
        }
        if (DEBUG) {
            Log.d(TAG, "[isUserLogin] : true");
        }
        return true;
    }

    /**
     * 检查token是否可用
     *
     * @return
     */
    public boolean isTokenValid() {
        if (null == mUser.getToken()) {
            if (DEBUG) {
                Log.e(TAG, "[isTokenValid] : false, because token is null");
            }
            return false;
        }
        if (System.currentTimeMillis() >= mUser.getExpiresTime()) {
            if (DEBUG) {
                Log.e(TAG, "[isTokenValid] : false, because token is expired");
            }
            return false;
        }
        if (DEBUG) {
            Log.e(TAG, "[isTokenValid] : true");
        }
        return true;
    }

    /**
     * 不仅检查用户登录信息是否完备，还会检查token是否过期
     *
     * @return
     */
    public boolean isUserLoginAndTokenValid() {
        return isUserLogin() && isTokenValid();
    }

    public void persistLocal() {
        if (DEBUG) {
            Log.d(TAG, "[persistLocal] : mUser=" + mUser);
        }
        LoginSharedPrefUtils.writeSocialUser(mUser);
    }

    /**
     * 去除所有登录信息，包括内存和本地
     */
    public void clear() {
        LoginSharedPrefUtils.clear();
        mUser = new SocialUser();
    }

}
