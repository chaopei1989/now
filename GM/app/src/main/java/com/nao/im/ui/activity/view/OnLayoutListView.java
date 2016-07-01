package com.nao.im.ui.activity.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * Created by chaopei on 2015/10/23.
 * 布局变化时有回调的ListView
 */
public class OnLayoutListView extends ListView {

    public interface OnLayoutChangedListener {
        void onChanged(View view, boolean changed, int l, int t, int r, int b);
    }

    private OnLayoutChangedListener onLayoutChangedListener;

    public void setOnLayoutChangedListener(OnLayoutChangedListener onLayoutChangedListener) {
        this.onLayoutChangedListener = onLayoutChangedListener;
    }

    public OnLayoutListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public OnLayoutListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OnLayoutListView(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (null != onLayoutChangedListener) {
            onLayoutChangedListener.onChanged(this, changed, l, t, r, b);
        }
        super.onLayout(changed, l, t, r, b);
    }
}
