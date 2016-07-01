package com.nao.im.net.login;

import android.app.Activity;
import android.content.Context;

import com.tencent.tauth.IUiListener;

/**
 * Created by chaopei on 2015/9/18.
 * 单点登录接口
 */
public interface ISSOAgent {
    void login(Activity context, IUiListener listener);

    void logout();

    void getUserInfo(Context context, IUiListener listener);
}
