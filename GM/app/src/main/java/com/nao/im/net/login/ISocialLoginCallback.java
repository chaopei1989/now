package com.nao.im.net.login;

import com.nao.im.model.login.SocialUser;

/**
 * Created by fanxiafei on 2015/9/1.
 * 单点登录+业务登录的回调
 */
public interface ISocialLoginCallback {

    void onGetUserInfoSuccess(SocialUser user);

    void onGetTokenSuccess();

    void onFailure();

    void onCancel();
}
