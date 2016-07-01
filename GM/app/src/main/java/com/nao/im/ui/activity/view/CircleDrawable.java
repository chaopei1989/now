package com.nao.im.ui.activity.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.nao.im.GMEnv;


/**
 * Created by chaopei on 2015/7/22.
 * 上传界面的底部button背景
 */
public class CircleDrawable extends Drawable {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "UploadProgressDrawable" : CircleDrawable.class.getSimpleName();

    Context mContext;

    Paint mPaint;

    int colorStroke, colorFill;

    public CircleDrawable(Context context) {
        this.mContext = context;
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }


    public CircleDrawable(Context context, int colorStroke, int colorFill) {
        this.mContext = context;
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.colorStroke = colorStroke;
        this.colorFill = colorFill;
    }


    @Override
    public void draw(Canvas canvas) {
        int w = getBounds().width();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(colorFill);
        canvas.drawCircle(w / 2, w / 2, w / 2, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(colorStroke);
        canvas.drawCircle(w/2, w/2, w/2, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
