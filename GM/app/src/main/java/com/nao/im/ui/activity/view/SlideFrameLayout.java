package com.nao.im.ui.activity.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.nao.im.GMEnv;

/**
 * Created by chaopei on 2015/10/15.
 * 从底部滑出的面板
 */
public class SlideFrameLayout extends FrameLayout {

    private static final boolean DEBUG = GMEnv.DEBUG;
    private static final String TAG = DEBUG ? "BottomSlideFrameLayout" : SlideFrameLayout.class.getSimpleName();
    private Scroller mScroller;
    private OnScrollListener mScrollListener;
    private OnTouchListener mOnTouchListener;

    public void setDispatchTouchListener(OnTouchListener l) {
        mOnTouchListener = l;
    }

    public void setOnScrollListener(OnScrollListener scrollListener) {
        this.mScrollListener = scrollListener;
    }

    public interface OnScrollListener {
        void onScroll(int scrollX, int scrollY);
    }public interface OnTouchListener {
        boolean onTouch(MotionEvent event);
    }

    private void initScroller() {
        mScroller = new Scroller(getContext());
    }

    public SlideFrameLayout(Context context) {
        super(context);
        initScroller();
    }

    public SlideFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScroller();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
        if (null != mScrollListener) {
            mScrollListener.onScroll(getScrollX(), getScrollY());
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (null != mOnTouchListener) {
            if(mOnTouchListener.onTouch(event)) {
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
