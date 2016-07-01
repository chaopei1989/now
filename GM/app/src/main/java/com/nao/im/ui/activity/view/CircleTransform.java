package com.nao.im.ui.activity.view;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Created by chaopei on 2015/10/6.
 */
public class CircleTransform implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        try {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            if (squaredBitmap != source) {
                source.recycle();
            }

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return source;
        } catch (Error e) {
            e.printStackTrace();
            return source;
        }
    }

    @Override
    public String key() {
        return "circle";
    }
}