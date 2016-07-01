package com.nao.im.ui.activity.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by chaopei on 2015/10/15.
 * 从底部滑出的面板
 */
public class BottomSlideLinearLayout extends LinearLayout {

    private Scroller mScroller;

    private void initScroller() {
        mScroller = new Scroller(getContext());
    }

    public BottomSlideLinearLayout(Context context) {
        super(context);
        initScroller();
    }

    public BottomSlideLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScroller();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    public void fling(int vy, int minY, int maxY) {
        mScroller.fling(getScrollX(), getScrollY(), 0, vy, 0, 0, minY, maxY);
        invalidate();
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mScroller.startScroll(startX, startY, dx, dy, duration);
        invalidate();
    }
}
