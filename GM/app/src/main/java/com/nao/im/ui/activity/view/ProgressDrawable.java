package com.nao.im.ui.activity.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.nao.im.GMEnv;
import com.nao.im.R;


/**
 * Created by chaopei on 2015/7/22.
 * 上传界面的底部button背景
 */
public class ProgressDrawable extends Drawable {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "UploadProgressDrawable" : ProgressDrawable.class.getSimpleName();

    Context mContext;

    Paint mPaint;

    double mProgress, mSecondProgress, mThirdProgress = 1.0;

    public void setProgressFull() {
        this.mProgress = 100;
        invalidateSelf();
    }

    /**
     * 设置进度，并重绘
     * @param progress 0-100
     */
    public void setProgress(int progress) {
        this.mProgress = progress / 100.0;
        invalidateSelf();
    }

    /**
     * 设置进度，并重绘
     * @param progress 0-100.0
     */
    public void setProgress(double progress) {
        this.mProgress = progress;
        invalidateSelf();
    }

    /**
     * 设置进度，并重绘
     * @param progress 0-100
     */
    public void setSecondProgress(int progress) {
        this.mSecondProgress = progress / 100.0;
        invalidateSelf();
    }

    /**
     * 设置进度，并重绘
     * @param progress 0-100.0
     */
    public void setSecondProgress(double progress) {
        this.mSecondProgress = progress;
        invalidateSelf();
    }

    /**
     * 设置进度，并重绘
     * @param progress 0-100
     */
    public void setThirdProgress(int progress) {
        this.mThirdProgress = progress / 100.0;
        invalidateSelf();
    }

    /**
     * 设置进度，并重绘
     * @param progress 0-100.0
     */
    public void setThirdProgress(double progress) {
        this.mThirdProgress = progress;
        invalidateSelf();
    }

    public ProgressDrawable(Context context) {
        this.mContext = context;
        this.mPaint = new Paint();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = new Rect(getBounds());
        int w = rect.width();
        mPaint.setColor(mContext.getResources().getColor(android.R.color.white));
        rect.right = rect.left + (int) (w * mThirdProgress);
        canvas.drawRect(rect, mPaint);
        mPaint.setColor(mContext.getResources().getColor(R.color.color_progress_grey));
        rect.right = rect.left + (int) (w * mSecondProgress);
        canvas.drawRect(rect, mPaint);
        mPaint.setColor(mContext.getResources().getColor(R.color.color_now_blue));
        rect.right = rect.left + (int) (w * mProgress);
        canvas.drawRect(rect, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
