package com.cherry.cropper.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * @author pengxiaobao
 * @date 2019/2/25
 * @description 提供一些自定义view的工具
 */
public class CustomViewUtil {

    /**
     * 创建指定颜色的Paint
     */
    public static Paint getNewPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        return paint;
    }

    /**
     * 创建给定高度和颜色的Paint对象，如果厚度< 0返回null
     */
    public static Paint getNewPaintOrNull(float thickness, int color) {
        if (thickness > 0) {
            Paint borderPaint = new Paint();
            borderPaint.setColor(color);
            borderPaint.setStrokeWidth(thickness);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setAntiAlias(true);
            return borderPaint;
        } else {
            return null;
        }
    }

    /**
     * 画剪切区域的边框
     */
    public static void drawBorders(Canvas canvas, Paint borderPaint, RectF rect, Enum.CropShape cropShape) {
        if (borderPaint != null) {
            float w = borderPaint.getStrokeWidth();
            rect.inset(w / 2, w / 2);

            if (cropShape == Enum.CropShape.RECTANGLE) {
                // 绘制矩形裁剪窗口边框
                canvas.drawRect(rect, borderPaint);
            } else {
                // 绘制圆形裁剪窗口边框
                canvas.drawOval(rect, borderPaint);
            }
        }
    }

    /**
     * 在裁剪区域内画两条垂直和两条水平的线，将其分成9等份。
     */
    public static void drawGuidelines(Paint guidelinePaint, Paint borderPaint, Canvas canvas, RectF rect, Enum.CropShape cropShape) {
        if (guidelinePaint != null) {
            float sw = borderPaint != null ? borderPaint.getStrokeWidth() : 0;
            rect.inset(sw, sw);

            float oneThirdCropWidth = rect.width() / 3;
            float oneThirdCropHeight = rect.height() / 3;

            if (cropShape == Enum.CropShape.OVAL) {

                float w = rect.width() / 2 - sw;
                float h = rect.height() / 2 - sw;

                // Draw vertical guidelines.
//                float x1 = rect.left + oneThirdCropWidth;
//                float x2 = rect.right - oneThirdCropWidth;
//                float yv = (float) (h * Math.sin(Math.acos((w - oneThirdCropWidth) / w)));
//                canvas.drawLine(x1, rect.top + h - yv, x1, rect.bottom - h + yv, guidelinePaint);
//                canvas.drawLine(x2, rect.top + h - yv, x2, rect.bottom - h + yv, guidelinePaint);

                // Draw horizontal guidelines.
                float y1 = rect.top + oneThirdCropHeight;
                float y2 = rect.bottom - oneThirdCropHeight;
                float xv = (float) (w * Math.cos(Math.asin((h - oneThirdCropHeight) / h)));
                canvas.drawLine(rect.left + w - xv, y1, rect.right - w + xv, y1, guidelinePaint);
                canvas.drawLine(rect.left + w - xv, y2, rect.right - w + xv, y2, guidelinePaint);
            } else {

                // Draw vertical guidelines.
//                float x1 = rect.left + oneThirdCropWidth;
//                float x2 = rect.right - oneThirdCropWidth;
//                canvas.drawLine(x1, rect.top, x1, rect.bottom, guidelinePaint);
//                canvas.drawLine(x2, rect.top, x2, rect.bottom, guidelinePaint);

                // Draw horizontal guidelines.
                float y1 = rect.top + oneThirdCropHeight;
                float y2 = rect.bottom - oneThirdCropHeight;
                canvas.drawLine(rect.left, y1, rect.right, y1, guidelinePaint);
                canvas.drawLine(rect.left, y2, rect.right, y2, guidelinePaint);
            }
        }
    }
}
