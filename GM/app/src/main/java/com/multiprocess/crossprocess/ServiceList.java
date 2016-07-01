package com.multiprocess.crossprocess;

import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;
import android.util.SparseArray;

import com.multiprocess.MPEnv;
import com.nao.im.App;
import com.nao.im.net.data.UserDataManager;
import com.nao.im.player.AVIMManager;
import com.nao.im.player.ChannelService;

public class ServiceList {

    private static final boolean DEBUG = MPEnv.DEBUG;

    private static final String TAG = ServiceList.class.getSimpleName();

    static final int MIN_ID = 0;

    static final int MAX_ID = 1024;

    public static class ID {
        public final static int MUSIC_PLAYER = 0;
        public final static int DEFAULT_SHARED_PRE = 1;
        public final static int CHANNEL_SERVICE = 2;
        public final static int USER_DATA_SERVICE = 3;
        public final static int CHAT_SERVICE = 4;
    }

    private static final SparseArray<Service> mAllServices = new SparseArray<Service>();

    private static SparseArray<IBinder> sCache = new SparseArray<IBinder>();

    static {
        if (DEBUG) {
            Log.d(TAG, "[static init]：running in process " + App.getProcessName());
        }
        // 不管哪个进程都要执行install，客户端需要靠Service的asInterface去还原IBinder
        ChannelService.INSTALLER.install();
        DefaultSharedPreference.INSTALLER.install();
        UserDataManager.INSTALLER.install();
        AVIMManager.INSTALLER.install();
    }

    /**
     * 【主进程】查询cache的Binder对象
     *
     * @param id
     * @return
     */
    static IBinder getCacheBinder(int id) {
        return sCache.get(id);
    }

    /**
     * 【主进程】往cache插入Binder对象
     *
     * @param id
     * @return
     */
    static void putCacheBinder(int id, IBinder binder) {
        sCache.put(id, binder);
    }

    /**
     * 【Server进程】获取install的Service
     *
     * @param id
     * @return
     */
    static Service getService(int id) {
        return mAllServices.get(id);
    }

    /**
     * 【Server进程】install时调用
     *
     * @param id
     * @param service
     */
    static void putService(int id, Service service) {
        mAllServices.put(id, service);
    }

    static void clearCache() {
        sCache.clear();
    }

    static IInterface getInterface(int id, IBinder binder) {
        Service service = mAllServices.get(id);
        if (service == null) {
            if (DEBUG) {
                Log.e(TAG, "[getInterface]：service is null, id=" + id);
            }
        }
        return mAllServices.get(id).asInterface(binder);
    }

    static void init() {
    }

}
