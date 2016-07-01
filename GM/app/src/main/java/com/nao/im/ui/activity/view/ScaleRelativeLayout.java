package com.nao.im.ui.activity.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by chaopei on 2015/11/6.
 * 可以放缩内容的RelativeLayout
 */
public class ScaleRelativeLayout extends RelativeLayout {

    private float mScale = 1.0f;
    private float mTransX = 0;

    public void setScale(float scale) {
        if (scale > 1) {
            throw new RuntimeException();
        }
        mScale = scale;
        postInvalidate();
    }

    public void trans(int transX) {
        mTransX = transX;
        postInvalidate();
    }

    public ScaleRelativeLayout(Context context) {
        super(context);
    }

    public ScaleRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScaleRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mTransX, 0);
        canvas.translate(getWidth() / 2 * (1 - mScale), 0);
        canvas.scale(mScale, mScale);
        super.dispatchDraw(canvas);
        canvas.restore();
    }
}
