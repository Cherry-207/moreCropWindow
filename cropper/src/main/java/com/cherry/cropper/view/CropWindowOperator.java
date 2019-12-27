package com.cherry.cropper.view;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.widget.ImageView;

import com.cherry.cropper.handler.BitmapUtils;
import com.cherry.cropper.handler.CropImageAnimation;
import com.cherry.cropper.utils.Enum;
import com.cherry.cropper.utils.SimpleLog;


/**
 * @author pengxiaobao
 * @date 2019/1/24
 * @description 剪切框处理器, 如: 缩放, 旋转, 调整位置等
 */
public class CropWindowOperator {

    private static final String TAG = "OCR:" + CropWindowOperator.class.getName();

//    private CropFrameLayout cropFrameLayout;
    /**
     * 用于图像矩阵变换计算
     */
    private final float[] mImagePoints = new float[8];
    // 使图片能够平滑的放大/缩小的动画类
    private CropImageAnimation mAnimation;

//    public CropWindowOperator(CropFrameLayout cropFrameLayout) {
//        this.cropFrameLayout = cropFrameLayout;
//    }

    public CropWindowOperator() {}

    /**
     * 用来处理剪切框缩放和原图缩放
     * 当用户将剪切框调整的足够小的时候, 会对原图和剪切框进行放大
     * @param inProgress is the crop window change is still in progress by the user
     * @param animate if to animate the change to the image matrix, or set it directly
     */
//    public void handleCropWindowChanged(CropOverlayView cropOverlayView,
//                                        Bitmap bitmap, Matrix mImageMatrix, Matrix mImageInverseMatrix,
//                                        ImageView mImageView, int mDegreesRotated, Enum.ScaleType mScaleType,
//                                        float mZoomOffsetX, float mZoomOffsetY, int mLoadedSampleSize,
//                                        float mZoom, int mMaxZoom, boolean mAutoZoomEnabled,
//                                        boolean inProgress, boolean animate, float width, float height) {
////        int width = cropFrameLayout.getAvailWidth();
////        int height = cropFrameLayout.getAvailHeight();
//        if (bitmap != null && width > 0 && height > 0) {
//
//            RectF cropRect = cropOverlayView.getCropWindowRect();
//            if (inProgress) {
//                if (cropRect.left < 0 || cropRect.top < 0 || cropRect.right > width || cropRect.bottom > height) {
//                    Log.i(TAG, "applyImageMatrix: 1");
//                    applyImageMatrix(bitmap, mImageMatrix, mImageInverseMatrix,
//                            cropOverlayView, mDegreesRotated, mScaleType, mAutoZoomEnabled, mZoom,
//                            mZoomOffsetX, mZoomOffsetY, mImageView, width, height, false, mLoadedSampleSize, false);
//                }
//            } else if (mAutoZoomEnabled || mZoom > 1) {
//                float newZoom = 0;
//                // keep the cropping window covered area to 50%-65% of zoomed sub-area
//                if (mZoom < mMaxZoom && cropRect.width() < width * 0.5f && cropRect.height() < height * 0.5f) {
//                    newZoom = Math.min(mMaxZoom, Math.min(width / (cropRect.width() / mZoom / 0.64f), height / (cropRect.height() / mZoom / 0.64f)));
//                }
//                if (mZoom > 1 && (cropRect.width() > width * 0.65f || cropRect.height() > height * 0.65f)) {
//                    newZoom = Math.max(1, Math.min(width / (cropRect.width() / mZoom / 0.51f), height / (cropRect.height() / mZoom / 0.51f)));
//                }
//                if (!mAutoZoomEnabled) {
//                    newZoom = 1;
//                }
//
//                if (newZoom > 0 && newZoom != mZoom) {
//                    if (animate) {
//                        if (mAnimation == null) {
//                            // lazy create animation single instance
//                            mAnimation = new CropImageAnimation(mImageView, cropOverlayView);
//                        }
//                        // 设置动画开始的状态
//                        mAnimation.setStartState(mImagePoints, mImageMatrix);
//                    }
//                    mZoom = newZoom;
//                    Log.i("CropImageView", "applyImageMatrix: 2");
//                    applyImageMatrix(bitmap, mImageMatrix, mImageInverseMatrix,
//                            cropOverlayView, mDegreesRotated, mScaleType, mAutoZoomEnabled, mZoom,
//                            mZoomOffsetX, mZoomOffsetY, mImageView, width, height, true, mLoadedSampleSize, animate);
//                }
//            }
//        }
//    }

    /**
     * 应用矩阵对裁剪框矩形进行处理
     *
     * @param width 原图的宽度
     * @param height 原图的高度
     */
    public void applyImageMatrix(Bitmap bitmap, RectF rect, Matrix mImageMatrix, Matrix mImageInverseMatrix,
                                 CropOverlayView cropOverlayView, int mDegreesRotated,
                                 Enum.ScaleType mScaleType, boolean mAutoZoomEnabled,
                                 float mZoom, float mZoomOffsetX, float mZoomOffsetY,
                                 ImageView mImageView, float width, float height, float padding, boolean center,
                                 int mLoadedSampleSize, boolean animate) {
        if (bitmap != null && width > 0 && height > 0) {
            SimpleLog.i(TAG, "width: " + width);
            SimpleLog.i(TAG, "height: " + height);

            // TODO 注释掉以下第一行和第三行，避免在一个 CropOverlayView 中创建多个裁剪框时，相互造成干扰，尚不清楚这两句之前的作用！！！
//            mImageMatrix.invert(mImageInverseMatrix);
            RectF cropRect = cropOverlayView.getCropWindowRect();
            if (rect != null) {
                cropOverlayView.setInitRect(rect);
                cropRect.set(rect);
            }
//            mImageInverseMatrix.mapRect(cropRect);

//            float windowPadding = cropFrameLayout.getCropLayoutPadding();
            float windowPadding = padding;

            mImageMatrix.reset();

            // 首先将图像移动到图像视图的中心，这样我们就可以从那里对其进行操作
            float transX = (width - bitmap.getWidth()) / 2;
            float transY = (height - bitmap.getHeight()) / 2;
            mImageMatrix.postTranslate(transX, transY);
            mapImagePointsByImageMatrix(bitmap, mImageMatrix);

            // 从图像中心旋转所需的角度
            if (mDegreesRotated > 0) {
                mImageMatrix.postRotate(mDegreesRotated, BitmapUtils.getRectCenterX(mImagePoints), BitmapUtils.getRectCenterY(mImagePoints));
                mapImagePointsByImageMatrix(bitmap, mImageMatrix);
            }

            // 算出要缩放到的宽和高, 对裁剪框矩形进行缩放
            float scale = Math.min(width / BitmapUtils.getRectWidth(mImagePoints), height / BitmapUtils.getRectHeight(mImagePoints));
            if (mScaleType == Enum.ScaleType.FIT_CENTER || (mScaleType == Enum.ScaleType.CENTER_INSIDE && scale < 1) || (scale > 1 && mAutoZoomEnabled)) {
                mImageMatrix.postScale(scale, scale, BitmapUtils.getRectCenterX(mImagePoints), BitmapUtils.getRectCenterY(mImagePoints));
                mapImagePointsByImageMatrix(bitmap, mImageMatrix);
            }

            // 按当前缩放比例缩放
            mImageMatrix.postScale(mZoom, mZoom, BitmapUtils.getRectCenterX(mImagePoints), BitmapUtils.getRectCenterY(mImagePoints));
            mapImagePointsByImageMatrix(bitmap, mImageMatrix);

            mImageMatrix.mapRect(cropRect);

            float rectWidth = BitmapUtils.getRectWidth(mImagePoints);
            float rectHeight = BitmapUtils.getRectHeight(mImagePoints);
            if (center) {
                // 将缩放区域设置为裁剪窗口的中心位置
                mZoomOffsetX = width > rectWidth ? 0
//                        : Math.max(Math.min(width / 2 - cropRect.centerX(), -BitmapUtils.getRectLeft(mImagePoints)), cropFrameLayout.getAvailWidth() - BitmapUtils.getRectRight(mImagePoints)) / mZoom;
                        : Math.max(Math.min(width / 2 - cropRect.centerX(), -BitmapUtils.getRectLeft(mImagePoints)), width - BitmapUtils.getRectRight(mImagePoints)) / mZoom;
                mZoomOffsetY = height > rectHeight ? 0
//                        : Math.max(Math.min(height / 2 - cropRect.centerY(), -BitmapUtils.getRectTop(mImagePoints)), cropFrameLayout.getAvailHeight() - BitmapUtils.getRectBottom(mImagePoints)) / mZoom;
                        : Math.max(Math.min(height / 2 - cropRect.centerY(), -BitmapUtils.getRectTop(mImagePoints)), height - BitmapUtils.getRectBottom(mImagePoints)) / mZoom;
            } else {
                // 调整缩小后的区域，使裁剪窗口矩形将在区域内，以防它被移动到外部
                mZoomOffsetX = Math.min(Math.max(mZoomOffsetX * mZoom, -cropRect.left), -cropRect.right + width) / mZoom;
                mZoomOffsetY = Math.min(Math.max(mZoomOffsetY * mZoom, -cropRect.top), -cropRect.bottom + height) / mZoom;
            }

            // 应用缩放偏移量去调整剪切框
            mZoomOffsetX += windowPadding;
            mZoomOffsetY += windowPadding;
            float offsetX = mZoomOffsetX * mZoom;
            float offsetY = mZoomOffsetY * mZoom;
            mImageMatrix.postTranslate(offsetX, offsetY);
            cropRect.offset(offsetX, offsetY);
//            cropOverlayView.setCropWindowRect(cropRect);
            mapImagePointsByImageMatrix(bitmap, mImageMatrix);

            // 应用矩阵集合
            if (animate) {
                // 开启动画并设置动画结束的状态
                mAnimation.setEndState(mImagePoints, mImageMatrix);
                mImageView.startAnimation(mAnimation);
            } else {
                mImageView.setImageMatrix(mImageMatrix);
            }
            // 更新剪切框视图
            updateImageBounds(bitmap, mLoadedSampleSize, cropOverlayView, width, height);
        }
    }


    /**
     * 通过图像变换矩阵调整给定的图像矩形，得到图像的最终矩形。
     * 要得到正确的矩形，首先必须将其重置为原始图像矩形。
     */
    private void mapImagePointsByImageMatrix(Bitmap bitmap, Matrix mImageMatrix) {
        mImagePoints[0] = 0;
        mImagePoints[1] = 0;
        mImagePoints[2] = bitmap.getWidth();
        mImagePoints[3] = 0;
        mImagePoints[4] = bitmap.getWidth();
        mImagePoints[5] = bitmap.getHeight();
        mImagePoints[6] = 0;
        mImagePoints[7] = bitmap.getHeight();
        mImageMatrix.mapPoints(mImagePoints);
    }

    // 设置剪切框的位置
    public void updateImageBounds(Bitmap bitmap, int mLoadedSampleSize, CropOverlayView cropOverlayView, float width, float height) {
        Log.i(TAG, "mBitmap: " + (bitmap != null) + " mLoadedSampleSize: " + mLoadedSampleSize);
        if (bitmap != null) {
            // 获取实际位图尺寸和显示的宽度/高度尺寸之间的比例
            float scaleFactorWidth = bitmap.getWidth() * mLoadedSampleSize / BitmapUtils.getRectWidth(mImagePoints);
            float scaleFactorHeight = bitmap.getHeight() * mLoadedSampleSize / BitmapUtils.getRectHeight(mImagePoints);
//            cropOverlayView.setCropWindowLimits(cropFrameLayout.getAvailWidth(), cropFrameLayout.getAvailHeight(), scaleFactorWidth, scaleFactorHeight);
            cropOverlayView.setCropWindowLimits(width, height, scaleFactorWidth, scaleFactorHeight);
        }
//        for (int i = 0; i < mImagePoints.length; i++) {
//            SimpleLog.i(TAG, "mImagePoints" + i + ": " + mImagePoints[i]);
//        }
        // 设置剪切框矩形图片，并在设置比例后更新剪切框的位置
//        cropOverlayView.setBounds(clear ? null : mImagePoints, cropFrameLayout.getAvailWidth(), cropFrameLayout.getAvailHeight());
        cropOverlayView.setBounds(mImagePoints, (int)BitmapUtils.getRectRight(mImagePoints), (int)BitmapUtils.getRectBottom(mImagePoints));
    }
}
