package com.nao.im.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * Created by chaopei on 2015/10/23.
 */
public abstract class WorkQueuedExecutor implements Handler.Callback {
    private final int mMaxMsgWhatId;
    private HandlerThread mHandlerThread;
    private Handler mWorkHandler;
    private final long mQuitDelay;
    private final String mThreadName;

    private static final int MSG_WORK_QUIT_LOOPER = -1000;

    protected WorkQueuedExecutor(int maxMsgId, long quitDelay, String threadName) {
        mMaxMsgWhatId = maxMsgId;
        mQuitDelay = quitDelay;
        mThreadName = threadName;
    }

    private synchronized void ensureWorkThreadStarted() {
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread(mThreadName);
            mHandlerThread.start();
            mWorkHandler = new WorkHandler(mHandlerThread.getLooper(), this);
        }
    }

    private synchronized void quitLooper() {
        Looper looper = mHandlerThread.getLooper();
        if (looper != null) {
            looper.quit();
        }
        mHandlerThread = null;
        mWorkHandler = null;
    }

    public synchronized final boolean sendMessage(Message msg) {
        ensureWorkThreadStarted();
        return mWorkHandler.sendMessageDelayed(msg, 0);
    }


    public synchronized final boolean sendMessage(int what, int arg1, int arg2, Object obj) {
        ensureWorkThreadStarted();
        return mWorkHandler.sendMessageDelayed(mWorkHandler.obtainMessage(what, arg1, arg2, obj), 0);
    }


    public synchronized final boolean sendMessage(int what, int arg1, int arg2) {
        ensureWorkThreadStarted();
        return mWorkHandler.sendMessageDelayed(mWorkHandler.obtainMessage(what, arg1, arg2), 0);
    }

    public synchronized final boolean sendEmptyMessage(int what) {
        ensureWorkThreadStarted();
        return mWorkHandler.sendEmptyMessageDelayed(what, 0);
    }

    public synchronized final boolean sendMessageDelayed(Message msg, long delayMillis) {
        ensureWorkThreadStarted();
        return mWorkHandler.sendMessageDelayed(msg, delayMillis);
    }

    public synchronized final boolean sendMessageDelayed(int what, long delayMillis) {
        ensureWorkThreadStarted();
        return mWorkHandler.sendMessageDelayed(mWorkHandler.obtainMessage(what), delayMillis);
    }

    public synchronized final boolean sendMessageAtFrontOfQueue(int what) {
        ensureWorkThreadStarted();
        return mWorkHandler.sendMessageAtFrontOfQueue(mWorkHandler.obtainMessage(what));
    }

    public synchronized final void removeMessages(int what) {
        ensureWorkThreadStarted();
        mWorkHandler.removeMessages(what);
    }

    public synchronized final boolean hasMessages(int what) {
        ensureWorkThreadStarted();
        return mWorkHandler.hasMessages(what);
    }


    protected void onQuitLoop() {
        System.gc();
    }

    @Override
    public abstract boolean handleMessage(Message msg);



    private class WorkHandler extends Handler {
        private WorkHandler(Looper looper, Callback callback) {
            super(looper, callback);
        }

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);

            if (msg.what == MSG_WORK_QUIT_LOOPER) {
                synchronized (WorkQueuedExecutor.this) {
                    if (quitLoopIfNeed()) {
                        onQuitLoop();

                        quitLooper();
                        //able go here?
                        return;
                    }
                }
            }
            sendEmptyMessageDelayed(MSG_WORK_QUIT_LOOPER, mQuitDelay);
        }

        private boolean quitLoopIfNeed() {
            for (int i = 0; i <= mMaxMsgWhatId; i++) {
                if (hasMessages(i)) {
                    return false;
                }
            }
            return true;
        }

    }

}