package com.cherry.cropper.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.view.View;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class BitmapUtil {

    public static Bitmap decodeUriAsBitmap(Context context, Uri uri) {
        if (context == null || uri == null) return null;
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    /**
     * 根据原图和变长绘制圆形图片
     *
     * @param source
     * @param min
     * @return
     */
    public static Bitmap createCircleImage(Bitmap source, int min) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        /**
         * 产生一个同样大小的画布
         */
        Canvas canvas = new Canvas(target);
        /**
         * 首先绘制圆形
         */
        canvas.drawCircle(min / 2, min / 2, min / 2, paint);
        /**
         * 使用SRC_IN
         */
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        /**
         * 绘制图片
         */
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }

    /**
     * 对bitmap内指定的一组矩形区域进行填充白色处理
     *
     * @param bitmap
     * @param rects
     * @return
     */
    public static Bitmap transBitmapWhite(Bitmap bitmap, List<Rect> rects) {
        if (rects == null || rects.size() < 1) {
            return bitmap;
        }
        int nBitmapHeight;
        int nBitmapWidth;
        nBitmapWidth = bitmap.getWidth();
        nBitmapHeight = bitmap.getHeight();

        List<Rect> realRects = new ArrayList<>(rects.size());
        for (Rect rect : rects) {
            int realLeft = rect.left;
            int realRight = rect.right;
            int realTop = rect.top;
            int realBottom = rect.bottom;

            if (realLeft >= realRight || realRight <= 0.0)
                continue;
            if (realTop >= realBottom || realBottom <= 0.0)
                continue;

            if (rect.left < 0)
                realLeft = 0;
            if (rect.right > nBitmapWidth)
                realRight = nBitmapWidth;
            if (rect.top < 0)
                realTop = 0;
            if (rect.bottom > nBitmapHeight)
                realBottom = nBitmapHeight;

            Rect realRect = new Rect(realLeft, realTop, realRight, realBottom);
            realRects.add(realRect);
        }

        if (realRects == null || realRects.size() < 1) {
            return bitmap;
        }

        int nArrayColorLength;
        int[] nArrayColor;
        nArrayColorLength = bitmap.getWidth() * bitmap.getHeight();
        nArrayColor = new int[nArrayColorLength];
        int count = 0;
        for (int i = 0; i < bitmap.getHeight(); i++) {
            for (int j = 0; j < bitmap.getWidth(); j++) {
                boolean isInWhiteRect = false;
                for (Rect rect : realRects) {
                    if (j >= rect.left && j <= rect.right
                            && i >= rect.top && i <= rect.bottom) {
                        isInWhiteRect = true;
                    }
                }
                if (isInWhiteRect) {
                    nArrayColor[count] = Color.WHITE;
                } else {
                    nArrayColor[count] = bitmap.getPixel(j, i);
                }
                count++;
            }
        }
        Bitmap result = Bitmap.createBitmap(nArrayColor, nBitmapWidth, nBitmapHeight,
                Bitmap.Config.ARGB_8888);
        return result;
    }

    /**
     * 对bitmap内指定矩形区域进行填充白色处理
     * @param bitmap
     * @param rect
     * @return
     */
    public static Bitmap transBitmapWhite(Bitmap bitmap, Rect rect) {
        if (rect == null) {
            return bitmap;
        }
        int nBitmapHeight;
        int nBitmapWidth;
        nBitmapWidth = bitmap.getWidth();
        nBitmapHeight = bitmap.getHeight();

        int realLeft = rect.left;
        int realRight = rect.right;
        int realTop = rect.top;
        int realBottom = rect.bottom;

        if (realLeft >= realRight || realRight <= 0.0)
            return bitmap;
        if (realTop >= realBottom || realBottom <= 0.0)
            return bitmap;

        if (rect.left < 0)
            realLeft = 0;
        if (rect.right > nBitmapWidth)
            realRight = nBitmapWidth;
        if (rect.top < 0)
            realTop = 0;
        if (rect.bottom > nBitmapHeight)
            realBottom = nBitmapHeight;
        Rect realRect = new Rect(realLeft, realTop, realRight, realBottom);

        int nArrayColorLength;
        int[] nArrayColor;
        nArrayColorLength = bitmap.getWidth() * bitmap.getHeight();
        nArrayColor = new int[nArrayColorLength];
        int count = 0;
        for (int i = 0; i < bitmap.getHeight(); i++) {
            for (int j = 0; j < bitmap.getWidth(); j++) {
                boolean isInWhiteRect = false;
                if (j >= realRect.left && j <= realRect.right && i >= realRect.top && i <= realRect.bottom) {
                    isInWhiteRect = true;
                }
                if (isInWhiteRect) {
                    nArrayColor[count] = Color.WHITE;
                } else {
                    nArrayColor[count] = bitmap.getPixel(j, i);
                }
                count++;
            }
        }
        return Bitmap.createBitmap(nArrayColor, nBitmapWidth, nBitmapHeight, Bitmap.Config.ARGB_8888);
    }

    /**
     * 确定测量的方式, 测量的宽高取决于测量的模式
     *
     * @param measureSpecMode 测量的宽度或高度的模式。
     * @param measureSpecSize 测量的宽度或高度的大小。
     * @param desiredSize     测量宽度或高度所需的尺寸。
     * @return 宽度或高度的最终大小。
     */
    public static int getOnMeasureSpec(int measureSpecMode, int measureSpecSize, int desiredSize) {
        // Measure Width
        int spec;
        if (measureSpecMode == View.MeasureSpec.EXACTLY) {
            spec = measureSpecSize;
        } else if (measureSpecMode == View.MeasureSpec.AT_MOST) {
            // Can't be bigger than...; match_parent value
            spec = Math.min(desiredSize, measureSpecSize);
        } else {
            // Be whatever you want; wrap_content
            spec = desiredSize;
        }
        return spec;
    }
}