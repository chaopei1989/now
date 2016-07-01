package com.nao.im;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.avos.avoscloud.AVOSCloud;
import com.nao.im.net.data.RequestManager;
import com.nao.im.player.ChannelService;
import com.nao.im.util.SysUtils;
import com.umeng.analytics.MobclickAgent;

import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;


/**
 * Created by chaopei on 2015/8/27.
 * 自定义 Application
 */
public class App extends LitePalApplication {

    /** 未知进程 */
    public static final int PROCESS_TYPE_UNKNOWN = 0;
    /** 频道服务进程 */
    public static final int PROCESS_TYPE_CHANNEL = 1;
    /** UI进程 */
    public static final int PROCESS_TYPE_UI = 2;
    /** chat进程 */
    public static final int PROCESS_TYPE_CHAT = 3;

    private static int sCurProcessType;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        MobclickAgent.setDebugMode(true);
        MobclickAgent.setCatchUncaughtExceptions(true);
    }

    public static boolean isChannelProcess() {
        return sCurProcessType == PROCESS_TYPE_CHANNEL;
    }

    public static boolean isChatProcess() {
        return sCurProcessType == PROCESS_TYPE_CHAT;
    }

    public static boolean isUIProcess() {
        return sCurProcessType == PROCESS_TYPE_UI;
    }

    private void init() {
        if (isUIProcess()) {
            startService(new Intent(getContext(), ChannelService.class));
        } else {
            SQLiteDatabase db = Connector.getDatabase();
            db.close();
            // Leancloud 初始化
            AVOSCloud.initialize(this, "HU6DLhnggRcDVLtXBg81PgOx", "T5JUsNE7nwyW26pr0GghW9Lj");
        }
        // Volley初始化，所有线程都要
        RequestManager.init(getContext());
    }


    static {
        String process = SysUtils.getCurrentProcessName();
        if (TextUtils.isEmpty(process)
                || !process.startsWith("com.nao.im")) {
            sCurProcessType = PROCESS_TYPE_UNKNOWN;
            System.exit(0);
        } else if (process.endsWith(":channel")) {
            sCurProcessType = PROCESS_TYPE_CHANNEL;
        } else if (process.endsWith(":chat")) {
            sCurProcessType = PROCESS_TYPE_CHAT;
        } else {
            sCurProcessType = PROCESS_TYPE_UI;
        }
    }

    public static int getProcessType() {
        return sCurProcessType;
    }

    public static String getProcessName() {
        switch (sCurProcessType) {
            case PROCESS_TYPE_CHANNEL:
                return "PROCESS___CHANNEL";
            case PROCESS_TYPE_UI:
                return "PROCESS___UI";
            case PROCESS_TYPE_CHAT:
                return "PROCESS___CHAT";
            default:
                return "PROCESS___UNKNOWN";
        }
    }

}
