package com.nao.im.ui.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.nao.im.GMEnv;
import com.nao.im.util.UiUtils;
import com.umeng.analytics.MobclickAgent;


/**
 * Created by zhuangqing on 2015/6/29.
 * 所有activity的base
 * 修改请慎重
 */
public class BaseActivity extends FragmentActivity {

    protected static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG_BASE_ACTIVITY = "AvBaseActivity";

    private static boolean sHasStartAllService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (!sHasStartAllService) {
            if (DEBUG) {
                Log.d(TAG_BASE_ACTIVITY, "[sHasStartAllService] startAllServerIfNeed");
            }
            sHasStartAllService = true;
        } else {
            if (DEBUG) {
                Log.d(TAG_BASE_ACTIVITY, "[sHasStartAllService] has started");
            }
        }

        if (GMEnv.IMMERSIVE) {
            if (19 <= Build.VERSION.SDK_INT) {
                UiUtils.setTranslucent(getWindow(), true, false);
            }
        }
        super.onCreate(filterSavedInstanceState(savedInstanceState));

    }

    @Override
    protected void onStart() {
        if (DEBUG) {
            Log.d(TAG_BASE_ACTIVITY, "onStart taskId = " + getTaskId() + " isTaskRoot = " + isTaskRoot() + "intent = "
                    + getIntent());
        }
        super.onStart();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(TAG_BASE_ACTIVITY, "onPostCreate taskId = " + getTaskId() + " isTaskRoot = " + isTaskRoot() + "intent = "
                    + getIntent());
        }
        super.onPostCreate(savedInstanceState);
    }


    @Override
    protected void onPostResume() {
        if (DEBUG) {
            Log.d(TAG_BASE_ACTIVITY, "onPostResume taskId = " + getTaskId() + " isTaskRoot = " + isTaskRoot() + "intent = "
                    + getIntent());
        }
        super.onPostResume();
    }


    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(TAG_BASE_ACTIVITY, "onPause taskId = " + getTaskId() + " isTaskRoot = " + isTaskRoot()
                    + "intent = " + getIntent());
        }
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        if (DEBUG) {
            Log.d(TAG_BASE_ACTIVITY, "onStop taskId = " + getTaskId() + " isTaskRoot = " + isTaskRoot()
                    + "intent = " + getIntent());
        }
        super.onStop();
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
    }

    /**
     * 因为现在绝大多数的Activity都不需要savedInstanceState状态，这里默认返回为空
     * 如果有需要该功能的子Activity可以重写该方法
     *
     * @param savedInstanceState
     * @return
     */
    protected Bundle filterSavedInstanceState(Bundle savedInstanceState) {
        return null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) {
            Log.d(TAG_BASE_ACTIVITY, "onResume taskId = " + getTaskId() + " isTaskRoot = " + isTaskRoot() + "intent = " + getIntent());
        }
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
    }

    /*
     * 不知道哪来的黑科技好像真没有什么用 不知道谁发明的 解决这个地方的办法是使用commitAllowingStateLoss() 代替 commit()
     * 为了解决以下崩溃，必须重写onBackPressed()方法。
     * java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
     * at android.support.v4.app.FragmentManagerImpl.checkStateLoss(FragmentManager.java:1299)
     * at android.support.v4.app.FragmentManagerImpl.popBackStackImmediate(FragmentManager.java:445)
     * at android.support.v4.app.FragmentActivity.onBackPressed(FragmentActivity.java:164)
     * at android.app.Activity.onKeyUp(Activity.java:2099)
     */
    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Log.d(TAG_BASE_ACTIVITY, "onDestroy taskId = " + getTaskId() + " isTaskRoot = " + isTaskRoot() + "intent = " + getIntent());
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    public static class FullScreenAdjust {

        public static void assistActivity(Activity activity) {
            if (Build.VERSION.SDK_INT >= 19) {
                new FullScreenAdjust(activity);
            }
        }

        private View mChildOfContent;
        private int usableHeightPrevious, statusBarHeight;
        private FrameLayout.LayoutParams frameLayoutParams;

        private FullScreenAdjust(Activity activity) {
            FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
            mChildOfContent = content.getChildAt(0);
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    possiblyResizeChildOfContent();
                }
            });
            frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
            statusBarHeight = Resources.getSystem().getDimensionPixelSize(Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
        }

        private void possiblyResizeChildOfContent() {
            int usableHeightNow = computeUsableHeight();
            if (usableHeightNow != usableHeightPrevious) {
                int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
                int heightDifference = usableHeightSansKeyboard - usableHeightNow - statusBarHeight;
                if (heightDifference > (usableHeightSansKeyboard / 4)) {
                    // keyboard probably just became visible
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                } else {
                    // keyboard probably just became hidden
                    frameLayoutParams.height = usableHeightSansKeyboard;
                }
                mChildOfContent.requestLayout();
                usableHeightPrevious = usableHeightNow;
            }
        }

        private int computeUsableHeight() {
            Rect r = new Rect();
            mChildOfContent.getWindowVisibleDisplayFrame(r);
            return (r.bottom - r.top);
        }

    }
}

