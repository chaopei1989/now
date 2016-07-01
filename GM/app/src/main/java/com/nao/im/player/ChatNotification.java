package com.nao.im.player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;

import com.nao.im.App;
import com.nao.im.R;
import com.nao.im.ui.activity.main.MainActivity;

import java.util.Random;

/**
 * Created by chaopei on 2015/10/23.
 */
public class ChatNotification {

    private static ChatNotification instance;

    private ChatNotification() {}

    public static ChatNotification getInstance() {
        if (null == instance) {
            instance = new ChatNotification();
        }
        return instance;
    }

    private int mId = new Random(System.currentTimeMillis()).nextInt(1000) + 1000;

    Notification notification;

    int count = 0;

    public void show(String title, String desc, long time) {
        ++count;
        int requestCode = new Random(System.currentTimeMillis()).nextInt(30);
        Intent intent = new Intent(App.getContext(), MainActivity.class);
        intent.putExtra("close", true);
        PendingIntent switchIntent = PendingIntent.getActivity(App.getContext(), requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        if (null == notification) {
            notification = new Notification();
        }
        Notification.Builder builder = new Notification.Builder(App.getContext())
                .setSmallIcon(R.drawable.ic_status)
                .setLargeIcon(BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.ic_launcher))
                .setContentInfo(""+count)
                .setAutoCancel(true)
                .setContentIntent(switchIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{0, 150, 300, 150})
                .setTicker(title + ":" + desc)
                .setWhen(time)
                .setContentTitle(title)
                .setContentText(desc);
        if (Build.VERSION.SDK_INT >= 17) {
            builder.setShowWhen(true);
        }
        builder.buildInto(notification);
        NotificationManager manager = (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(mId, notification);
    }

//    public void show(String title, String desc, String timeDesc) {
//        if (null == notification) {
//            notification = new Notification(R.mipmap.ic_launcher, null, System.currentTimeMillis());
//        }
//        notification.tickerText = title+":"+desc;
//        int requestCode = new Random(System.currentTimeMillis()).nextInt(30);
//
//        Intent intent = new Intent(App.getContext(), ChatActivity.class);
//        intent.putExtra("close", true);
//        PendingIntent switchIntent = PendingIntent.getActivity(App.getContext(), requestCode, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        notification.contentView = new RemoteViews(App.getContext().getPackageName(),
//                R.layout.notification);
//        notification.contentIntent = switchIntent;
//        notification.contentView.setCharSequence(R.id.noti_msg_title,
//                "setText", title);
//        notification.contentView.setCharSequence(R.id.noti_msg_desc,
//                "setText", desc);
//        notification.contentView.setCharSequence(R.id.noti_msg_time,
//                "setText", timeDesc);
//        NotificationManager manager = (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(mId, notification);
//    }

    public void clear() {
        count = 0;
        NotificationManager manager = (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(mId);
    }
}
