package com.nao.im.ui.activity.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.nao.im.GMEnv;
import com.nao.im.R;
import com.nao.im.util.UiUtils;


/**
 * Created by chaopei on 2015/7/22.
 * 上传界面的底部button背景
 */
public class PlayStateDrawable extends Drawable {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "UploadProgressDrawable" : PlayStateDrawable.class.getSimpleName();

    Context mContext;

    Paint mPaint;

    int WIDTH, SPACE;

    float FIRST, SECOND, THIRD, FORTH;

    public PlayStateDrawable(Context context) {
        this.mContext = context;
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(context.getResources().getColor(R.color.color_e1));
        WIDTH = UiUtils.dp2px(R.dimen.av_dp_2);
        SPACE = UiUtils.dp2px(R.dimen.av_dp_1);
        FIRST = WIDTH / 2;
        SECOND = 1.5f * WIDTH + SPACE;
        THIRD = 2 * SPACE + 2.5f * WIDTH;
        FORTH = 3.5f * WIDTH + 3 * SPACE;
    }


    @Override
    public void draw(Canvas canvas) {
        float w = getBounds().width();
        float h = getBounds().height();
        mPaint.setStrokeWidth(WIDTH);
        canvas.drawLine(FIRST, 0, FIRST, h, mPaint);
        canvas.drawLine(SECOND, 0, SECOND, h, mPaint);
        canvas.drawLine(THIRD, 0, THIRD, h, mPaint);
        canvas.drawLine(FORTH, 0, FORTH, h, mPaint);
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
