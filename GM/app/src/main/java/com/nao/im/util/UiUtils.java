package com.nao.im.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.nao.im.App;
import com.nao.im.R;

import java.lang.reflect.Method;

/**
 * Created by zhuangqing on 2015/6/29.
 * 和UI相关的公共方法请放到这里。切记切记写注释
 */
public class UiUtils {

    @TargetApi(19)
    public static void setTranslucent(Window win, boolean statusBar, boolean navigationBar) {
        WindowManager.LayoutParams winParams = win.getAttributes();

        if (21 <= Build.VERSION.SDK_INT && statusBar) {
            try {
                Method method;
                win.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                winParams.flags |= 0x80000000;
                win.setAttributes(winParams);
                method = ReflectUtils.getMethod(Window.class,
                        "setStatusBarColor", int.class);
                method.invoke(win, win.getContext().getResources().getColor(R.color.transparent));
            } catch (Exception e) {
                Log.e("setTranslucent","error! the screen IMMERSIVE is failed!");
            }
        } else {
            if (statusBar) {
                winParams.flags |= 0x04000000;
            } else {
                winParams.flags &= ~0x04000000;
            }
            if (navigationBar) {
                winParams.flags |= 0x08000000;
            } else {
                winParams.flags &= ~0x08000000;
            }
            win.setAttributes(winParams);
        }
    }

    /**
     * 获得屏幕的宽高
     * @return
     */
    public static int[] getScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wmManager = (WindowManager) App.getContext().getSystemService(Context.WINDOW_SERVICE);
        wmManager.getDefaultDisplay().getMetrics(displayMetrics);

        int[] screens = new int[2];
        screens[0] = displayMetrics.widthPixels;
        screens[1] = displayMetrics.heightPixels;
        return screens;
    }

    public static int dp2px(int dpSrc) {
        return App.getContext().getResources().getDimensionPixelSize(dpSrc);
    }

    public static int getStatusHeight() {
        int statusBarHeight = Resources.getSystem().getDimensionPixelSize(
                Resources.getSystem().getIdentifier("status_bar_height",
                        "dimen", "android"));
        return statusBarHeight;
    }

    @Deprecated
    public static int getDisplayHeight(Activity activity) {
        int h = activity.getWindowManager().getDefaultDisplay().getHeight();
        return h;
    }

    @Deprecated
    public static int getDisplayWidth(Activity activity) {
        int w = activity.getWindowManager().getDefaultDisplay().getWidth();
        return w;
    }


    /**
     * Get the screen height.
     *
     * @param context
     * @return the screen height
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static int getScreenHeight(Activity context) {

        Display display = context.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            return size.y;
        }
        return display.getHeight();
    }

    /**
     * Get the screen width.
     *
     * @param context
     * @return the screen width
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static int getScreenWidth(Activity context) {

        Display display = context.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            return size.x;
        }
        return display.getWidth();
    }

    /**
     * 转换图片成圆形
     *
     * @param bitmap 传入Bitmap对象
     * @return
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);

        return output;
    }
}
