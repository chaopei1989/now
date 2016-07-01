package com.nao.im.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

import com.nao.im.GMConstants;
import com.nao.im.GMEnv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by chaopei on 2015/8/29.
 */
public class SysUtils {

    private static final String TAG = GMEnv.DEBUG ? "SysUtils" : SysUtils.class.getSimpleName();

    /** 返回当前的进程名 */
    public static String getCurrentProcessName() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/self/cmdline")));
            String line = null;
            while ((line = reader.readLine()) != null) {
                return line.trim();
            }
        } catch (Exception e) {
            if (GMEnv.DEBUG)
                Log.e(TAG, "[getCurrentProcessName]: ", e);
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (Exception e) {
                }
        }
        return null;
    }

    public static void sendBroadcast(Context context, Intent broadcast) {
        context.sendBroadcast(broadcast, GMConstants.PERMISSION);
    }

    public static void registerBroadcastReceiver(Context context, BroadcastReceiver receiver, IntentFilter filter) {
        context.registerReceiver(receiver, filter, GMConstants.PERMISSION, null);
    }

    public static void unregisterBroadcastReceiver(Context context, BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

    public static void bellsAndVibrate(Context ctx) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone rt = RingtoneManager.getRingtone(ctx, uri);
        rt.play();

        Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[] { 0, 180, 350, 180}, -1);
    }
}














