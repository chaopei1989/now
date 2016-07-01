package com.nao.im.player;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.nao.im.App;
import com.nao.im.R;
import com.nao.im.ui.activity.NotificationActivity;
import com.nao.im.ui.activity.main.MainActivity;

import java.util.Random;

/**
 * Created by chaopei on 2015/10/10.
 * 常驻通知栏
 */
public class ChannelNotification {

    private static ChannelNotification instance;

    private int NOTIFICATION_ID = -1;

    private Notification mN;

    private Random random = new Random(System.currentTimeMillis());

    private ChannelNotification(){
        NOTIFICATION_ID = random.nextInt(1000);
        mN = new Notification(R.drawable.ic_status, "闹，一起听音乐",
                System.currentTimeMillis());

        int requestCode = random.nextInt(32);
        mN.contentIntent = PendingIntent.getActivity(App.getContext(), requestCode, new Intent(App.getContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mN.contentView = new RemoteViews(App.getContext().getPackageName(), R.layout.channel_notification);

        mN.contentView.setOnClickPendingIntent(R.id.next, notificationIntent(NotificationActivity.Constants.KEY_NOTIFICATION_NEXT, true));
        mN.contentView.setOnClickPendingIntent(R.id.like, notificationIntent(NotificationActivity.Constants.KEY_NOTIFICATION_LIKE, true));
        mN.contentView.setOnClickPendingIntent(R.id.exit, notificationIntent(NotificationActivity.Constants.KEY_NOTIFICATION_EXIT, true));
    }

    private PendingIntent notificationIntent(String key, boolean value) {
        int requestCode = random.nextInt(32);
        Intent intent = new Intent(App.getContext(), NotificationActivity.class);
        intent.putExtra(key, value);
        PendingIntent settingIntent = PendingIntent.getActivity(App.getContext(), requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return settingIntent;
    }

    public static ChannelNotification getInstance() {
        if(null == instance) {
            instance = new ChannelNotification();
        }
        return instance;
    }

    public void frontService(Service service) {
        service.startForeground(NOTIFICATION_ID, mN);
    }

    public void serviceStopFront(Service service) {
        service.stopForeground(true);
    }

    public void setInfo(String text, Bitmap bitmap) {
        mN.contentView.setTextViewText(R.id.title, text);
        mN.contentView.setImageViewBitmap(R.id.img, bitmap);
        NotificationManager nm = (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, mN);
    }
}
