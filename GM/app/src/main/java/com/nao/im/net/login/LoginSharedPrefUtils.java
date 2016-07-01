package com.nao.im.net.login;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.multiprocess.crossprocess.DefaultSharedPreference;
import com.nao.im.GMEnv;
import com.nao.im.model.login.SocialUser;

/**
 * Created by zhanghailong-ms on 2015/7/11.
 * 存取用户登录信息的工具类
 */
public class LoginSharedPrefUtils {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "LoginSharedPrefUtils" : LoginSharedPrefUtils.class.getSimpleName();

    private static final String PREFERENCE_NAME = "social_user";

    private static final String SOCIAL_USER = "social_user";

    /**
     * 将用户登录信息存在本地
     * @param user
     */
    public static void writeSocialUser(SocialUser user) {
        if (null == user)
            return;
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        try {
            DefaultSharedPreference.getInstance().putString(SOCIAL_USER, userJson);
        } catch (RemoteException e) {
        }
    }

    /**
     * 从本地读取用户登录信息
     * @return
     */
    public static SocialUser readSocialUser() {
        SocialUser user;
        try {
            String userJson = DefaultSharedPreference.getInstance().getString(SOCIAL_USER, null);
            if (TextUtils.isEmpty(userJson)) {
                user = null;
            } else {
                Gson gson = new Gson();
                user = gson.fromJson(userJson, SocialUser.class);
            }
        } catch (Exception e) {
            Log.e(TAG, "[readSocialUser]", e);
            user = null;
        }
        return  user;
    }

    /**
     * 清除登录痕迹
     */
    public static void clear() {
        try {
            DefaultSharedPreference.getInstance().putString(SOCIAL_USER, null);
        } catch (RemoteException e) {
            Log.e(TAG, "[clear]", e);
            e.printStackTrace();
        }
    }
}