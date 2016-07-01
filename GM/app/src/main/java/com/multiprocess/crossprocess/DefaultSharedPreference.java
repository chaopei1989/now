package com.multiprocess.crossprocess;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import com.nao.im.App;


/**
 * Created by chaopei on 2015/9/12.
 * 跨进程SharedPreference;
 */
public class DefaultSharedPreference extends ICrossProcessSharedPreference.Stub {

    private static DefaultSharedPreference instance;

    private SharedPreferences mSp;

    public static DefaultSharedPreference getInstance() {
        if (null == instance) {
            instance = new DefaultSharedPreference();
            if (App.isChannelProcess()) {
                instance.init();
            }
        }
        return instance;
    }

    private void init() {
        if(null == mSp){
            mSp = App.getContext().getSharedPreferences(App.getContext().getPackageName(), Context.MODE_PRIVATE);
        }
    }

    final public static Service INSTALLER = new Service() {

        @Override
        public int getServiceId() {
            return ServiceList.ID.DEFAULT_SHARED_PRE;
        }

        @Override
        public IBinder getService() {
            return DefaultSharedPreference.getInstance();
        }

        @Override
        public IInterface asInterface(IBinder binder) {
            return ICrossProcessSharedPreference.Stub.asInterface(binder);
        }
    };

    @Override
    synchronized public int getInt(String key, int defValue) throws RemoteException {
        if(!App.isChannelProcess()) {
            ICrossProcessSharedPreference sp = (ICrossProcessSharedPreference) ServiceManager.getService(ServiceList.ID.DEFAULT_SHARED_PRE);
            if (null == sp) {
                throw new RemoteException();
            }
            return sp.getInt(key, defValue);
        }
        return mSp.getInt(key, defValue);
    }

    @Override
    synchronized public void putInt(String key, int value) throws RemoteException {
        if(!App.isChannelProcess()) {
            ICrossProcessSharedPreference sp = (ICrossProcessSharedPreference) ServiceManager.getService(ServiceList.ID.DEFAULT_SHARED_PRE);
            if (null == sp) {
                throw new RemoteException();
            }
            sp.putInt(key, value);
            return;
        }
        SharedPreferences.Editor editor = mSp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    @Override
    synchronized public long getLong(String key, long defValue) throws RemoteException {
        if(!App.isChannelProcess()) {
            ICrossProcessSharedPreference sp = (ICrossProcessSharedPreference) ServiceManager.getService(ServiceList.ID.DEFAULT_SHARED_PRE);
            if (null == sp) {
                throw new RemoteException();
            }
            return sp.getLong(key, defValue);
        }
        return mSp.getLong(key, defValue);
    }

    @Override
    synchronized public void putLong(String key, long value) throws RemoteException {
        if(!App.isChannelProcess()) {
            ICrossProcessSharedPreference sp = (ICrossProcessSharedPreference) ServiceManager.getService(ServiceList.ID.DEFAULT_SHARED_PRE);
            if (null == sp) {
                throw new RemoteException();
            }
            sp.putLong(key, value);
            return;
        }
        SharedPreferences.Editor editor = mSp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    @Override
    synchronized public boolean getBoolean(String key, boolean defValue) throws RemoteException {
        if(!App.isChannelProcess()) {
            ICrossProcessSharedPreference sp = (ICrossProcessSharedPreference) ServiceManager.getService(ServiceList.ID.DEFAULT_SHARED_PRE);
            if (null == sp) {
                throw new RemoteException();
            }
            return sp.getBoolean(key, defValue);
        }
        return mSp.getBoolean(key, defValue);
    }

    @Override
    synchronized public void putBoolean(String key, boolean value) throws RemoteException {
        if(!App.isChannelProcess()) {
            ICrossProcessSharedPreference sp = (ICrossProcessSharedPreference) ServiceManager.getService(ServiceList.ID.DEFAULT_SHARED_PRE);
            if (null == sp) {
                throw new RemoteException();
            }
            sp.putBoolean(key, value);
            return;
        }
        SharedPreferences.Editor editor = mSp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    @Override
    synchronized public String getString(String key, String defValue) throws RemoteException {
        if(!App.isChannelProcess()) {
            ICrossProcessSharedPreference sp = (ICrossProcessSharedPreference) ServiceManager.getService(ServiceList.ID.DEFAULT_SHARED_PRE);
            if (null == sp) {
                throw new RemoteException();
            }
            return sp.getString(key, defValue);
        }
        return mSp.getString(key, defValue);
    }

    @Override
    synchronized public void putString(String key, String value) throws RemoteException {
        if(!App.isChannelProcess()) {
            ICrossProcessSharedPreference sp = (ICrossProcessSharedPreference) ServiceManager.getService(ServiceList.ID.DEFAULT_SHARED_PRE);
            if (null == sp) {
                throw new RemoteException();
            }
            sp.putString(key, value);
            return;
        }
        SharedPreferences.Editor editor = mSp.edit();
        editor.putString(key, value);
        editor.commit();
    }

}
