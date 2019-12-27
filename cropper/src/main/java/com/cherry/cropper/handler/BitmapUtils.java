package com.cherry.cropper.handler;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.cherry.cropper.utils.Enum;
import com.cherry.cropper.utils.SimpleLog;


import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * 对图片进行剪切, 旋转等处理的工具类
 */
public final class BitmapUtils {

    public static final Rect EMPTY_RECT = new Rect();

    public static final RectF EMPTY_RECT_F = new RectF();

    /**
     * 可重用矩形, 一般内部使用
     */
    public static final RectF RECT = new RectF();

    /**
     * 一般内部使用的可重用点
     */
    public static final float[] POINTS = new float[6];

    /**
     * Reusable point for general internal usage
     */
    public static final float[] POINTS2 = new float[6];

    /**
     * Used to know the max texture size allowed to be rendered
     */
    private static int mMaxTextureSize;

//    public static ArrayList<Rect> encloseRestList = new ArrayList<Rect>();

    /**
     * 通过读取图像(uri)的Exif值来旋转给定的图像。
     * 创建新的位图，并回收旧的位图。
     */
    public static RotateBitmapResult rotateBitmapByExif(Bitmap bitmap, Context context, Uri uri) {
        try {
            File file = getFileFromUri(context, uri);
            if (file.exists()) {
                Log.i("BitmapUtils", "base image AbsolutePath:" + file.getAbsolutePath());
                ExifInterface ei = new ExifInterface(file.getAbsolutePath());
                return rotateBitmapByExif(bitmap, ei);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return new RotateBitmapResult(bitmap, 0);
    }

    /**
     * 根据给定的Exif值旋转给定的图片
     * 创建新的位图，并回收旧的位图。
     */
    public static RotateBitmapResult rotateBitmapByExif(Bitmap bitmap, ExifInterface exif) {
        int degrees;
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degrees = 270;
                break;
            default:
                degrees = 0;
                break;
        }
        return new RotateBitmapResult(bitmap, degrees);
    }

    /**
     * Decode bitmap from stream using sampling to get bitmap with the requested limit.
     */
    public static BitmapSampled decodeSampledBitmap(Context context, Uri uri, int reqWidth, int reqHeight) {
        try {
            ContentResolver resolver = context.getContentResolver();
            // 首先用inJustDecodeBounds=true进行解码，检查尺寸
            BitmapFactory.Options options = decodeImageForOption(resolver, uri);
            // 计算实例大小
            options.inSampleSize = Math.max(
                    calculateInSampleSizeByReqestedSize(options.outWidth, options.outHeight, reqWidth, reqHeight),
                    calculateInSampleSizeByMaxTextureSize(options.outWidth, options.outHeight));
            // 解码位图与inSampleSize集
            Bitmap bitmap = decodeImage(resolver, uri, options);
            return new BitmapSampled(bitmap, options.inSampleSize);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sampled bitmap: " + uri + "\r\n" + e.getMessage(), e);
        }
    }

    /**
     * 针对带有图片选项的题目, 进行图片选项的剪切
     */
    public static BitmapSampled cropBitmapObjectHandleOOM(Bitmap bitmap, float[] points, int degreesRotated,
                                                          boolean fixAspectRatio, int aspectRatioX,
                                                          int aspectRatioY, boolean isEnclose, boolean isSplit) {
        int scale = 1;
        while (true) {
            try {
                Bitmap cropBitmap = cropBitmapObjectWithScale(bitmap, points, degreesRotated, fixAspectRatio,
                        aspectRatioX, aspectRatioY, 1 / (float) scale, isEnclose, isSplit);
                return new BitmapSampled(cropBitmap, scale);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 针对带有图片选项的题目, 进行图片选项的剪切
     */
    private static Bitmap cropBitmapObjectWithScale(Bitmap bitmap, float[] points, int degreesRotated,
                                                    boolean fixAspectRatio, int aspectRatioX, int aspectRatioY,
                                                    float scale, boolean isEnclose, boolean isSplit) {

        try {
            // 获取原始图像中包含所需裁剪区域的矩形(对于非矩形裁剪区域较大)
            Rect rect = getRectFromPoints(points, bitmap.getWidth(), bitmap.getHeight(), fixAspectRatio, aspectRatioX, aspectRatioY);

//            if (isEnclose) {
//                encloseRestList.add(rect);
//            }
//            if (isSplit) {
//                if (!encloseRestList.isEmpty()) {
//                    for (Rect encloseRect : encloseRestList) {
//                        if (rect.contains(encloseRect)) {
//                            bitmap = BitmapUtil.transBitmapWhite(bitmap, encloseRect); // 对原来的图片进行区域填充白色处理
//                        }
//                    }
//                }
//            }
            // 在一个操作中裁剪并旋转裁剪后的图像
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postRotate(degreesRotated, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            Bitmap result = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), matrix, true);
            if (result == bitmap) {
                // corner case when all bitmap is selected, no worth optimizing for it
                result = bitmap.copy(bitmap.getConfig(), false);
            }
            // 旋转0、90、180或270度不需要额外裁剪
            if (degreesRotated % 90 != 0) {
                // 额外的裁剪，因为非矩形裁剪不能直接在图像上完成，除非先旋转
                result = cropForRotatedImage(result, points, rect, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Crop image bitmap from URI by decoding it with specific width and height to down-sample if required.<br>
     * Additionally if OOM is thrown try to increase the sampling (2,4,8).
     */
    public static BitmapSampled cropBitmap(Context context, Uri loadedImageUri, float[] points,
                                           int degreesRotated, int orgWidth, int orgHeight, boolean fixAspectRatio,
                                           int aspectRatioX, int aspectRatioY, int reqWidth, int reqHeight) {
        int sampleMulti = 1;
        while (true) {
            try {
                // 如果成功，就返回结果位图
                return cropBitmap(context, loadedImageUri, points,
                        degreesRotated, orgWidth, orgHeight, fixAspectRatio,
                        aspectRatioX, aspectRatioY, reqWidth, reqHeight,
                        sampleMulti);
            } catch (OutOfMemoryError e) {
                // 如果OOM试图增加实例以降低内存使用
                sampleMulti *= 2;
                if (sampleMulti > 16) {
                    throw new RuntimeException("Failed to handle OOM by sampling (" + sampleMulti + "): " + loadedImageUri + "\r\n" + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Get left value of the bounding rectangle of the given points.
     */
    public static float getRectLeft(float[] points) {
        return Math.min(Math.min(Math.min(points[0], points[2]), points[4]), points[6]);
    }

    /**
     * Get top value of the bounding rectangle of the given points.
     */
    public static float getRectTop(float[] points) {
        return Math.min(Math.min(Math.min(points[1], points[3]), points[5]), points[7]);
    }

    /**
     * Get right value of the bounding rectangle of the given points.
     */
    public static float getRectRight(float[] points) {
        return Math.max(Math.max(Math.max(points[0], points[2]), points[4]), points[6]);
    }

    /**
     * Get bottom value of the bounding rectangle of the given points.
     */
    public static float getRectBottom(float[] points) {
        return Math.max(Math.max(Math.max(points[1], points[3]), points[5]), points[7]);
    }

    /**
     * Get width of the bounding rectangle of the given points.
     */
    public static float getRectWidth(float[] points) {
        return getRectRight(points) - getRectLeft(points);
    }

    /**
     * Get heightof the bounding rectangle of the given points.
     */
    public static float getRectHeight(float[] points) {
        return getRectBottom(points) - getRectTop(points);
    }

    /**
     * Get horizontal center value of the bounding rectangle of the given points.
     */
    public static float getRectCenterX(float[] points) {
        return (getRectRight(points) + getRectLeft(points)) / 2f;
    }

    /**
     * Get verical center value of the bounding rectangle of the given points.
     */
    public static float getRectCenterY(float[] points) {
        return (getRectBottom(points) + getRectTop(points)) / 2f;
    }

    /**
     * Get a rectangle for the given 4 points (x0,y0,x1,y1,x2,y2,x3,y3) by finding the min/max 2 points that
     * contains the given 4 points and is a stright rectangle.
     */
    public static Rect getRectFromPoints(float[] points, int imageWidth, int imageHeight, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY) {
        int left = Math.round(Math.max(0, getRectLeft(points)));
        int top = Math.round(Math.max(0, getRectTop(points)));
        int right = Math.round(Math.min(imageWidth, getRectRight(points)));
        int bottom = Math.round(Math.min(imageHeight, getRectBottom(points)));
        for (int i = 0; i < points.length; i++) {
            SimpleLog.i("BitMapUtils", "points" + i + ": " + points[i]);
        }
        Rect rect = new Rect(left, top, right, bottom);
        if (fixAspectRatio) {
            fixRectForAspectRatio(rect, aspectRatioX, aspectRatioY);
        }
        return rect;
    }

    /**
     * Fix the given rectangle if it doesn't confirm to aspect ration rule.<br>
     * Make sure that width and height are equal if 1:1 fixed aspect ratio is requested.
     */
    private static void fixRectForAspectRatio(Rect rect, int aspectRatioX, int aspectRatioY) {
        if (aspectRatioX == aspectRatioY && rect.width() != rect.height()) {
            if (rect.height() > rect.width()) {
                rect.bottom -= rect.height() - rect.width();
            } else {
                rect.right -= rect.width() - rect.height();
            }
        }
    }

    /**
     * Write the given bitmap to the given uri using the given compression.
     */
    public static void writeBitmapToUri(Context context, Bitmap bitmap, Uri uri, Bitmap.CompressFormat compressFormat, int compressQuality) throws FileNotFoundException {
        OutputStream outputStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(uri);
            bitmap.compress(compressFormat, compressQuality, outputStream);
        } finally {
            closeSafe(outputStream);
        }
    }

    /**
     * 根据给定的选项将给定的位图调整到给定的宽度/高度。
     */
    static Bitmap resizeBitmap(Bitmap bitmap, int reqWidth, int reqHeight, Enum.RequestSizeOptions options) {
        try {
            if (reqWidth > 0 && reqHeight > 0 && (options == Enum.RequestSizeOptions.RESIZE_FIT ||
                    options == Enum.RequestSizeOptions.RESIZE_INSIDE ||
                    options == Enum.RequestSizeOptions.RESIZE_EXACT)) {

                Bitmap resized = null;
                if (options == Enum.RequestSizeOptions.RESIZE_EXACT) {
                    resized = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
                } else {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    float scale = Math.max(width / (float) reqWidth, height / (float) reqHeight);
                    if (scale > 1 || options == Enum.RequestSizeOptions.RESIZE_FIT) {
                        resized = Bitmap.createScaledBitmap(bitmap, (int) (width / scale), (int) (height / scale), false);
                    }
                }
                if (resized != null) {
                    if (resized != bitmap) {
                        bitmap.recycle();
                    }
                    return resized;
                }
            }
        } catch (Exception e) {
            Log.w("AIC", "Failed to resize cropped image, return bitmap before resize", e);
        }
        return bitmap;
    }

    //region: Private methods

    /**
     * Crop image bitmap from URI by decoding it with specific width and height to down-sample if required.
     *
     * @param orgWidth    used to get rectangle from points (handle edge cases to limit rectangle)
     * @param orgHeight   used to get rectangle from points (handle edge cases to limit rectangle)
     * @param sampleMulti used to increase the sampling of the image to handle memory issues.
     */
    private static BitmapSampled cropBitmap(Context context, Uri loadedImageUri, float[] points,
                                            int degreesRotated, int orgWidth, int orgHeight, boolean fixAspectRatio,
                                            int aspectRatioX, int aspectRatioY, int reqWidth, int reqHeight, int sampleMulti) {

        // 获取原始图像中包含所需裁剪区域的矩形(对于非矩形裁剪区域较大)
        Rect rect = getRectFromPoints(points, orgWidth, orgHeight, fixAspectRatio, aspectRatioX, aspectRatioY);
        int width = reqWidth > 0 ? reqWidth : rect.width();
        int height = reqHeight > 0 ? reqHeight : rect.height();
        Bitmap result = null;
        int sampleSize = 1;
        try {
            // 只从URI解码所需的图像，如果给定reqWidth/reqHeight，则可以选择子采样。
            BitmapSampled bitmapSampled = decodeSampledBitmapRegion(context, loadedImageUri, rect, width, height, sampleMulti);
            result = bitmapSampled.bitmap;
            sampleSize = bitmapSampled.sampleSize;
        } catch (Exception ignored) {
        }
        if (result != null) {
            try {
                // rotate the decoded region by the required amount
                result = rotateBitmapInt(result, degreesRotated);

                // rotating by 0, 90, 180 or 270 degrees doesn't require extra cropping
                if (degreesRotated % 90 != 0) {

                    // extra crop because non rectangular crop cannot be done directly on the image without rotating first
                    result = cropForRotatedImage(result, points, rect, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY);
                }
            } catch (OutOfMemoryError e) {
                if (result != null) {
                    result.recycle();
                }
                throw e;
            }
            return new BitmapSampled(result, sampleSize);
        } else {
            // failed to decode region, may be skia issue, try full decode and then crop
            return cropBitmap(context, loadedImageUri, points, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY, sampleMulti, rect, width, height);
        }
    }

    /**
     * Crop bitmap by fully loading the original and then cropping it, fallback in case cropping region failed.
     */
    private static BitmapSampled cropBitmap(Context context, Uri loadedImageUri, float[] points,
                                            int degreesRotated, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY,
                                            int sampleMulti, Rect rect, int width, int height) {
        Bitmap result = null;
        int sampleSize;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize = sampleMulti * calculateInSampleSizeByReqestedSize(rect.width(), rect.height(), width, height);

            Bitmap fullBitmap = decodeImage(context.getContentResolver(), loadedImageUri, options);
            if (fullBitmap != null) {
                try {
                    // adjust crop points by the sampling because the image is smaller
                    float[] points2 = new float[points.length];
                    System.arraycopy(points, 0, points2, 0, points.length);
                    for (int i = 0; i < points2.length; i++) {
                        points2[i] = points2[i] / options.inSampleSize;
                    }

                    result = cropBitmapObjectWithScale(fullBitmap, points2, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY, 1, false, false);
                } finally {
                    if (result != fullBitmap) {
                        fullBitmap.recycle();
                    }
                }
            }
        } catch (OutOfMemoryError e) {
            if (result != null) {
                result.recycle();
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sampled bitmap: " + loadedImageUri + "\r\n" + e.getMessage(), e);
        }
        return new BitmapSampled(result, sampleSize);
    }

    /**
     * Decode image from uri using "inJustDecodeBounds" to get the image dimensions.
     * 使用“inJustDecodeBounds”从uri解码图像以获得图像的尺寸。
     */
    private static BitmapFactory.Options decodeImageForOption(ContentResolver resolver, Uri uri) throws FileNotFoundException {
        InputStream stream = null;
        try {
            stream = resolver.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, EMPTY_RECT, options);
            options.inJustDecodeBounds = false;
            return options;
        } finally {
            closeSafe(stream);
        }
    }

    /**
     * Decode image from uri using given "inSampleSize", but if failed due to out-of-memory then raise
     * the inSampleSize until success.
     */
    private static Bitmap decodeImage(ContentResolver resolver, Uri uri, BitmapFactory.Options options) throws FileNotFoundException {
        do {
            InputStream stream = null;
            try {
                stream = resolver.openInputStream(uri);
                return BitmapFactory.decodeStream(stream, EMPTY_RECT, options);
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2;
            } finally {
                closeSafe(stream);
            }
        } while (options.inSampleSize <= 512);
        throw new RuntimeException("Failed to decode image: " + uri);
    }

    /**
     * Decode specific rectangle bitmap from stream using sampling to get bitmap with the requested limit.
     *
     * @param sampleMulti used to increase the sampling of the image to handle memory issues.
     */
    private static BitmapSampled decodeSampledBitmapRegion(Context context, Uri uri, Rect rect, int reqWidth, int reqHeight, int sampleMulti) {
        InputStream stream = null;
        BitmapRegionDecoder decoder = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleMulti * calculateInSampleSizeByReqestedSize(rect.width(), rect.height(), reqWidth, reqHeight);

            stream = context.getContentResolver().openInputStream(uri);
            decoder = BitmapRegionDecoder.newInstance(stream, false);
            do {
                try {
                    return new BitmapSampled(decoder.decodeRegion(rect, options), options.inSampleSize);
                } catch (OutOfMemoryError e) {
                    options.inSampleSize *= 2;
                }
            } while (options.inSampleSize <= 512);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sampled bitmap: " + uri + "\r\n" + e.getMessage(), e);
        } finally {
            closeSafe(stream);
            if (decoder != null) {
                decoder.recycle();
            }
        }
        return new BitmapSampled(null, 1);
    }

    /**
     * Special crop of bitmap rotated by not stright angle, in this case the original crop bitmap contains parts
     * beyond the required crop area, this method crops the already cropped and rotated bitmap to the final
     * rectangle.<br>
     * Note: rotating by 0, 90, 180 or 270 degrees doesn't require extra cropping.
     */
    private static Bitmap cropForRotatedImage(Bitmap bitmap, float[] points, Rect rect, int degreesRotated,
                                              boolean fixAspectRatio, int aspectRatioX, int aspectRatioY) {
        if (degreesRotated % 90 != 0) {

            int adjLeft = 0, adjTop = 0, width = 0, height = 0;
            double rads = Math.toRadians(degreesRotated);
            int compareTo = degreesRotated < 90 || (degreesRotated > 180 && degreesRotated < 270) ? rect.left : rect.right;
            for (int i = 0; i < points.length; i += 2) {
                if (points[i] >= compareTo - 1 && points[i] <= compareTo + 1) {
                    adjLeft = (int) Math.abs(Math.sin(rads) * (rect.bottom - points[i + 1]));
                    adjTop = (int) Math.abs(Math.cos(rads) * (points[i + 1] - rect.top));
                    width = (int) Math.abs((points[i + 1] - rect.top) / Math.sin(rads));
                    height = (int) Math.abs((rect.bottom - points[i + 1]) / Math.cos(rads));
                    break;
                }
            }

            rect.set(adjLeft, adjTop, adjLeft + width, adjTop + height);
            if (fixAspectRatio) {
                fixRectForAspectRatio(rect, aspectRatioX, aspectRatioY);
            }

            Bitmap bitmapTmp = bitmap;
            bitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
            if (bitmapTmp != bitmap) {
                bitmapTmp.recycle();
            }
        }
        return bitmap;
    }

    /**
     * Calculate the largest inSampleSize value that is a power of 2 and keeps both
     * height and width larger than the requested height and width.
     */
    private static int calculateInSampleSizeByReqestedSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            while ((height / 2 / inSampleSize) > reqHeight && (width / 2 / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Calculate the largest inSampleSize value that is a power of 2 and keeps both
     * height and width smaller than max texture size allowed for the device.
     */
    private static int calculateInSampleSizeByMaxTextureSize(int width, int height) {
        int inSampleSize = 1;
        if (mMaxTextureSize == 0) {
            mMaxTextureSize = getMaxTextureSize();
        }
        if (mMaxTextureSize > 0) {
            while ((height / inSampleSize) > mMaxTextureSize || (width / inSampleSize) > mMaxTextureSize) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Get {@link File} object for the given Android URI.<br>
     * Use content resolver to get real path if direct path doesn't return valid file.
     */
    public static File getFileFromUri(Context context, Uri uri) {

        // first try by direct path
        File file = new File(uri.getPath());
        if (file.exists()) {
            return file;
        }

        // try reading real path from content resolver (gallery images)
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String realPath = cursor.getString(column_index);
                file = new File(realPath);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return file;
    }

    /**
     * Rotate the given bitmap by the given degrees.<br>
     * New bitmap is created and the old one is recycled.
     */
    private static Bitmap rotateBitmapInt(Bitmap bitmap, int degrees) {
        if (degrees > 0) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees);
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            if (newBitmap != bitmap) {
                bitmap.recycle();
            }
            return newBitmap;
        } else {
            return bitmap;
        }
    }

    /**
     * Get the max size of bitmap allowed to be rendered on the device.<br>
     * http://stackoverflow.com/questions/7428996/hw-accelerated-activity-how-to-get-opengl-texture-size-limit.
     */
    private static int getMaxTextureSize() {
        // Safe minimum default size
        final int IMAGE_MAX_BITMAP_DIMENSION = 2048;

        try {
            // Get EGL Display
            EGL10 egl = (EGL10) EGLContext.getEGL();
            EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            // Initialise
            int[] version = new int[2];
            egl.eglInitialize(display, version);

            // Query total number of configurations
            int[] totalConfigurations = new int[1];
            egl.eglGetConfigs(display, null, 0, totalConfigurations);

            // Query actual list configurations
            EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
            egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

            int[] textureSize = new int[1];
            int maximumTextureSize = 0;

            // Iterate through all the configurations to located the maximum texture size
            for (int i = 0; i < totalConfigurations[0]; i++) {
                // Only need to check for width since opengl textures are always squared
                egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

                // Keep track of the maximum texture size
                if (maximumTextureSize < textureSize[0]) {
                    maximumTextureSize = textureSize[0];
                }
            }

            // Release
            egl.eglTerminate(display);

            // Return largest texture size found, or default
            return Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
        } catch (Exception e) {
            return IMAGE_MAX_BITMAP_DIMENSION;
        }
    }

    /**
     * Close the given closeable object (Stream) in a safe way: check if it is null and catch-log
     * exception thrown.
     *
     * @param closeable 要关闭的可关闭对象
     */
    private static void closeSafe(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Holds bitmap instance and the sample size that the bitmap was loaded/cropped with.
     * 保存位图实例和加载/裁剪位图所用的样本大小。
     */
    public static final class BitmapSampled {

        public final Bitmap bitmap;

        /**
         * 用于降低位图大小的实例大小(1,2,4,8，…)
         */
        public final int sampleSize;

        BitmapSampled(Bitmap bitmap, int sampleSize) {
            this.bitmap = bitmap;
            this.sampleSize = sampleSize;
        }
    }

    /**
     * The result of {@link #rotateBitmapByExif(Bitmap, ExifInterface)}.
     */
    public static final class RotateBitmapResult {

        public Bitmap bitmap;

        /**
         * 图像旋转的角度
         */
        public final int degrees;

        RotateBitmapResult(Bitmap bitmap, int degrees) {
            this.bitmap = bitmap;
            this.degrees = degrees;
        }
    }
}