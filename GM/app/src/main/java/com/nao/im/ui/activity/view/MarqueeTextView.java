package com.nao.im.ui.activity.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by chaopei on 2015/11/13.
 * 跑马灯TextView
 */
public class MarqueeTextView extends TextView {
    private final static boolean DEBUG = true;
    public final static String TAG = DEBUG ? "MarqueeTextView" : MarqueeTextView.class.getSimpleName();

    private float textLength = 0f;//文本长度
    private float step = 0f;//文字的横坐标
    private float y = 0f;//文字的纵坐标
    private Paint paint = null;//绘图样式
    private String mText = "";//文本内容

    public MarqueeTextView(Context context) {
        super(context);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        refresh(text.toString());
    }

    /** */
    /**
     * 文本初始化，每次更改文本内容或者文本效果等之后都需要重新初始化一下
     */
    public void init() {
        refresh(getText().toString());
    }

    public void refresh(String text) {
        if (DEBUG) {
            Log.d(TAG, "[refresh] : text=" + text);
        }
        paint = getPaint();
        this.mText = text;
        textLength = paint.measureText(text);
        step = 0;
        y = getTextSize() + getPaddingTop() + 15;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mText != null) {
            if (DEBUG)Log.i("info", "[onDraw] : step=" + step + ", textLength=" + textLength);
            canvas.drawText(mText, step, y, paint);
        } else {
            if (DEBUG)Log.i("info", "**************CustomTextView.mText  is  null************");
        }
        if (textLength < canvas.getWidth()) {
            return;
        }
        step -= 1.0f;
        if (-step > textLength - canvas.getWidth() / 2) {
            canvas.drawText(mText, step + textLength + canvas.getWidth() / 2, y, paint);
        }

        if (-step > textLength) {
            step = step + textLength + canvas.getWidth() / 2;
        }
        invalidate();

    }

}