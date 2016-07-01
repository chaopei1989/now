package com.nao.im;

import android.util.Log;



/**
 * Created by chaopei on 2015/10/29.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler INSTANCE = new CrashHandler();

    public static void init() {
        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex) {
        Log.e("uncaughtException", "", ex);
        Throwable t = new Throwable("(BUILD " + GMConstants.BUILD + ") " + ex.getMessage(), ex);
        System.exit(0);
    }
}
