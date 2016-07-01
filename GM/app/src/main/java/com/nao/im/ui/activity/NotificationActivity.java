package com.nao.im.ui.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;

import com.nao.im.GMEnv;
import com.nao.im.model.login.SocialUser;
import com.nao.im.net.login.SocialUserManager;
import com.nao.im.player.ChannelService;
import com.nao.im.player.IChannelService;

/**
 * 通知中转Activity
 *
 * @author chaopei
 */
public class NotificationActivity extends Activity {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "NotificationActivity" : NotificationActivity.class.getSimpleName();

    public static class Constants {
        public static final String KEY_NOTIFICATION_NEXT = "KEY_NOTIFICATION_NEXT";
        public static final String KEY_NOTIFICATION_LIKE = "KEY_NOTIFICATION_LIKE";
        public static final String KEY_NOTIFICATION_EXIT = "KEY_NOTIFICATION_EXIT";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Intent intent = getIntent();
            if (null != intent && intent.getBooleanExtra(Constants.KEY_NOTIFICATION_EXIT, false)) {
                ChannelService.getService().disconnectChannel();
            } else if (null != intent && intent.getBooleanExtra(Constants.KEY_NOTIFICATION_LIKE, false)) {
                loveClick(5);
            } else if (null != intent && intent.getBooleanExtra(Constants.KEY_NOTIFICATION_NEXT, false)) {
                loveClick(-5);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        finish();
        super.onCreate(savedInstanceState);
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
            try {
                ChannelService.getService().toast("没有登录");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
