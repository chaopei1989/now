package com.nao.im.ui.activity.main;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import com.nao.im.GMEnv;
import com.nao.im.R;
import com.nao.im.ui.activity.BaseActivity;
import com.nao.im.ui.activity.player.IMFragment;
import com.nao.im.ui.activity.player.MusicPlayerFragment;
import com.nao.im.ui.activity.view.ScaleRelativeLayout;
import com.nao.im.ui.activity.view.SlideFrameLayout;
import com.nao.im.util.UiUtils;

/**
 * Created by chaopei on 2015/11/5.
 */
public class MainActivity extends BaseActivity {
    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "MainActivity" : MainActivity.class.getSimpleName();

    ScaleRelativeLayout mMainContent;

    MusicPlayerFragment mMusicPlayerFragment;

    IMFragment mIMFragment;

    SlideFrameLayout mSlidingLayout;

    static int SCREEN_WIDTH, SCREEN_HEIGHT;

    int distouchHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainContent = (ScaleRelativeLayout) findViewById(R.id.main_content);
        mSlidingLayout = (SlideFrameLayout) findViewById(R.id.chat_content);
        mMusicPlayerFragment = new MusicPlayerFragment();
        distouchHeight = UiUtils.dp2px(R.dimen.av_dp_58) + UiUtils.getStatusHeight();
        mIMFragment = new IMFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_content, mMusicPlayerFragment)
                .add(R.id.chat_content, mIMFragment)
                .commit();
        final ViewConfiguration configuration = ViewConfiguration.get(this);
        minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        SlidingTouchScrollListenerImpl impl = new SlidingTouchScrollListenerImpl();
        mSlidingLayout.setDispatchTouchListener(impl);
        mSlidingLayout.setOnScrollListener(impl);
        SCREEN_WIDTH = UiUtils.getScreenWidth(this);
        SCREEN_HEIGHT = UiUtils.getScreenHeight(this);
        mSlidingLayout.scrollBy(-SCREEN_WIDTH, 0);
    }

    public void setSoftInputVisible(boolean visible) {
        mIMFragment.setSoftInputVisible(visible);
    }

    boolean searching = false;

    public void onSearchSwitch(boolean searching) {
        this.searching = searching;
    }

    int minimumVelocity;
    int maximumVelocity;

    /**
     * 滑动的逻辑
     */
    private class SlidingTouchScrollListenerImpl implements SlideFrameLayout.OnTouchListener, SlideFrameLayout.OnScrollListener {
        private final boolean DEBUG = false;
        final int OUT = 1/*没有点到可拉动区域*/, IN = 0/*点到可拉动区域*/;
        final int MAIN = 0/*主页面*/, SUB = 1/*子页面*/;
        float sstartRawX, sstartRawY, startRawX, startRawY, lastRawX, lastRawY;
        VelocityTracker mVelocityTracker;

        private void obtainVelocityTracker(MotionEvent event) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
        }

        private void releaseVelocityTracker() {
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
        }

        @Override
        public void onScroll(int scrollX, int scrollY) {
            if (DEBUG) {
                Log.d(TAG, "scrollX="+scrollX);
            }
            mMainContent.trans(-(SCREEN_WIDTH + scrollX)/5);
        }

        int state = IN;
        int curr = MAIN;

        @Override
        public boolean onTouch(MotionEvent event) {
            if (searching) {
                return false;
            }
            float rawX = event.getRawX();
            float rawY = event.getRawY();
            int scrollX = mSlidingLayout.getScrollX();
            int scrollY = mSlidingLayout.getScrollY();
            obtainVelocityTracker(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (DEBUG) {
                        Log.d(TAG, "ACTION_DOWN");
                    }
                    if (rawY < distouchHeight) {
                        state = OUT;
                        return false;
                    } else if (MAIN == curr && SCREEN_WIDTH - rawX < 72) {
                        state = IN;
                    } else if (SUB == curr && rawX < 72) {
                        state = IN;
                    } else {
                        state = OUT;
                        return false;
                    }
                    if (state == IN) {
                        setSoftInputVisible(false);
                    }
                    sstartRawX = startRawX = lastRawX = rawX;
                    sstartRawY = startRawY = lastRawY = rawY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (state == OUT) {
                        return false;
                    }
                    float sdx = Math.abs(rawX - sstartRawX);
                    float sdy = Math.abs(rawY - sstartRawY);
                    if (sdx < 10 && sdy < 10) { // 滑动误差
                        startRawX = rawX;
                        startRawY = rawY;
                    } else {
                        float dX = rawX - lastRawX;
                        int nowScrollX = scrollX - (int) dX;
                        if (-SCREEN_WIDTH > nowScrollX) {
                            nowScrollX = -SCREEN_WIDTH;
                        } else if (0 < nowScrollX) {
                            nowScrollX = 0;
                        }
                        mSlidingLayout.scrollTo(nowScrollX, 0);
                    }
                    lastRawX = rawX;
                    lastRawY = rawY;
                    break;
                case MotionEvent.ACTION_UP:
                    if (DEBUG) {
                        Log.d(TAG, "ACTION_UP");
                    }
                    if (state == OUT) {
                        releaseVelocityTracker();
                        return false;
                    }
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity();
                    if (Math.abs(initialVelocity) > minimumVelocity) {//达到速度了
                        if (initialVelocity > 0) {
                            mSlidingLayout.startScroll(scrollX, scrollY, -scrollX - SCREEN_WIDTH, 0, 350);
                            curr = MAIN;
                        } else {
                            mSlidingLayout.startScroll(scrollX, scrollY, -scrollX, 0, 350);
                            curr = SUB;
                        }
                    } else {
                        if (scrollX > - SCREEN_WIDTH / 2) {
                            mSlidingLayout.startScroll(scrollX, scrollY, -scrollX, 0, 350);
                            curr = SUB;
                        } else {
                            mSlidingLayout.startScroll(scrollX, scrollY, -scrollX - SCREEN_WIDTH, 0, 350);
                            curr = MAIN;
                        }
                    }
                    releaseVelocityTracker();
                    break;
            }
            return true;
        }
    }
}
