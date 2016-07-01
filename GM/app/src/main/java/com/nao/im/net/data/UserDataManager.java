package com.nao.im.net.data;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.RemoteException;

import com.multiprocess.crossprocess.ServiceList;
import com.multiprocess.crossprocess.ServiceManager;
import com.nao.im.App;
import com.nao.im.model.login.SocialUser;
import com.nao.im.util.WorkQueuedExecutor;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chaopei on 2015/10/23.
 * 用户数据cache管理类
 */
public class UserDataManager implements IUserDataManager {

    private static UserDataManager instance;

    private UserDataManager(){
        if (App.isChannelProcess()) {
            executor = new WorkQueuedExecutor(MAX_MSG_ID, QUIT_DELAY, THREAD_NAME) {
                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_DB:
                            synchronized (this) {
                                SocialUser user = (SocialUser) msg.obj;
                                if (null != user) {
                                    if (0 == user.updateAll(String.format("userId=%d", user.getUserId()))) {
                                        user.save();
                                    }
                                }
                            }
                            break;
                    }
                    return false;
                }
            };
            mSocialUserCache = new HashMap<Integer, SocialUser>();
        }
    }

    public static UserDataManager getInstance() {
        if (null == instance) {
            instance = new UserDataManager();
        }
        return instance;
    }

    Map<Integer, SocialUser> mSocialUserCache;

    final int MSG_DB = 0;
    final int MAX_MSG_ID = 100;
    final long QUIT_DELAY = 10000;
    final String THREAD_NAME = "USER_CACHE";

    WorkQueuedExecutor executor;

    @Override
    public SocialUser getUserByUserId(int userId) throws RemoteException {
        if (!App.isChannelProcess()) {
            return ((IUserDataManager) ServiceManager.getService(INSTALLER.getServiceId())).getUserByUserId(userId);
        }
        synchronized (this) {
            SocialUser ret = mSocialUserCache.get(userId);
            if (null == ret) {
                List<SocialUser> users = DataSupport.where(String.format("userId=%d", userId))
                        .find(SocialUser.class);
                if (0 < users.size()) {
                    ret = users.get(0);
                    mSocialUserCache.put(userId, ret);
                }
            }
            return ret;
        }

    }

    @Override
    public void cache(SocialUser data) throws RemoteException {
        if (!App.isChannelProcess()) {
            ((IUserDataManager) ServiceManager.getService(INSTALLER.getServiceId())).cache(data);
            return;
        }
        synchronized (this) {
            mSocialUserCache.put(data.getUserId(), data);
            executor.sendMessage(MSG_DB, 0, 0, data);
        }
    }

    @Override
    public void cacheList(List<SocialUser> data) throws RemoteException {
        if (!App.isChannelProcess()) {
            ((IUserDataManager) ServiceManager.getService(INSTALLER.getServiceId())).cacheList(data);
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            synchronized (this) {
                SocialUser user = data.get(i);
                cache(user);
            }
        }
    }

    @Override
    public IBinder asBinder() {
        return this.asBinder();
    }

    final public static com.multiprocess.crossprocess.Service INSTALLER = new com.multiprocess.crossprocess.Service() {

        @Override
        public int getServiceId() {
            return ServiceList.ID.USER_DATA_SERVICE;
        }

        @Override
        public IBinder getService() {
            return UserDataManager.getInstance().asBinder();
        }

        @Override
        public IInterface asInterface(IBinder binder) {
            return IUserDataManager.Stub.asInterface(binder);
        }

    };
}
