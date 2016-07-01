package com.nao.im.ui.activity.view;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.nao.im.GMEnv;
import com.nao.im.util.UiUtils;


public class ImmersiveView extends View {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "ImmersiveView"
            : ImmersiveView.class.getSimpleName();

    private Context context;

    public ImmersiveView(Context context) {
        super(context);
        this.context = context;
    }

    public ImmersiveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initImmersive();
    }

    private void initImmersive() {
        if (GMEnv.IMMERSIVE) {
            if (Build.VERSION.SDK_INT < 19) {
                if (DEBUG) {
                    Log.d(TAG, "Build.VERSION.SDK_INT < 19");
                }
                setVisibility(View.GONE);
                return;
            }
            if (!(context instanceof Activity)) {
                if (DEBUG) {
                    Log.d(TAG, "context=" + context);
                }
                setVisibility(View.GONE);
                return;
            }
            int statusBarHeight = UiUtils.getStatusHeight();
            if (DEBUG) {
                Log.d(TAG, "statusBarHeight=" + statusBarHeight);
            }
            if (0 >= statusBarHeight) {
                setVisibility(View.GONE);
            } else {
                setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) getLayoutParams();
                params.height = statusBarHeight;
                setLayoutParams(params);
            }
        } else {
            setVisibility(View.GONE);
        }
    }
}
