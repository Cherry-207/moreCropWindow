package com.cherry.cropper.handler;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;


import com.cherry.cropper.utils.CropResult;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import com.cherry.cropper.utils.Enum;



/**
 * @author pengxiaobao
 * @date 2019/3/1
 * @description 处理剪切工作的工具类
 */
public class CropImageTask {

    private static final String TAG = "OCR:" + CropImageTask.class.getName();

    public static CropResult cropBaseImage(final Context context,
                                           final Uri uri,
                                           final Bitmap bitmap,
                                           final float[] cropPoint,
                                           final int degreesRotated,
                                           final boolean fixAspectRatio,
                                           final int aspectRatioX,
                                           final int aspectRatioY,
                                           final int reqWidth,
                                           final int reqHeight,
                                           final Enum.RequestSizeOptions options,
                                           final Bitmap.CompressFormat saveCompressFormat,
                                           final int saveCompressQuality) {

        BitmapUtils.BitmapSampled bitmapSampled;
        if (bitmap != null) {
            Log.d(TAG, "crop Base Image, create bitmapSampled");
            bitmapSampled = BitmapUtils.cropBitmapObjectHandleOOM(bitmap, cropPoint,
                    degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY, false, false);
        } else {
            Log.d(TAG, "crop Base Image, Bitmap == null");
            return new CropResult((Bitmap) null, 1);
        }
        Bitmap resizeBitmap = BitmapUtils.resizeBitmap(bitmapSampled.bitmap, reqWidth, reqHeight, options);
        if (uri != null && resizeBitmap != null) {
            try {
                Log.d(TAG, "crop Base Image ,uri != null");
//                Bitmap preproBitmap = bitmapPreprocessing(resizeBitmap);
                BitmapUtils.writeBitmapToUri(context, resizeBitmap, uri, saveCompressFormat, saveCompressQuality);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            resizeBitmap.recycle();
            return new CropResult(uri, bitmapSampled.sampleSize);
        } else {
            Log.d(TAG, "crop Base Image ,uri == null");
            return new CropResult(resizeBitmap, bitmapSampled.sampleSize);
        }
    }

    // 剪切拆题的题目和圈图的图片
    public static ArrayList<CropResult> cropSplitAndEncloseImage(final Context context,
                                                                 final Bitmap bitmap,
                                                                 final List<Uri> splitUriList,
                                                                 final List<Uri> enCloseUriList,
                                                                 final List<float[]> splitPoints,
                                                                 final List<float[]> enClosePoints,
                                                                 final int degreesRotated,
                                                                 final boolean fixAspectRatio,
                                                                 final int aspectRatioX,
                                                                 final int aspectRatioY,
                                                                 final int reqWidth,
                                                                 final int reqHeight,
                                                                 final Enum.RequestSizeOptions options,
                                                                 final Bitmap.CompressFormat saveCompressFormat,
                                                                 final int saveCompressQuality) {

        // 用于保存剪切结果的集合, 几个剪切框就有几条数据
        final ArrayList<CropResult> results = new ArrayList<CropResult>();
        // 剪切并保存圈图的截图
        if (enClosePoints != null && enCloseUriList != null) {
            for (int i = 0; i < enClosePoints.size(); i++) {
                BitmapUtils.BitmapSampled bitmapSampled;
                CropResult result;
                float[] cropPoint = enClosePoints.get(i);
                Uri uri = enCloseUriList.get(i);
                if (bitmap != null) {
                    Log.d(TAG, "crop enclose Image , create bitmapSampled");
                    bitmapSampled = BitmapUtils.cropBitmapObjectHandleOOM(bitmap, cropPoint,
                            degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY, true, false);
                } else {
                    Log.d(TAG, "crop enclose Image, Bitmap == null");
                    result = new CropResult((Bitmap) null, 1);
                    results.add(result);
                    return results;
                }
                Bitmap resizeBitmap = BitmapUtils.resizeBitmap(bitmapSampled.bitmap, reqWidth, reqHeight, options);
                if (resizeBitmap != null) {
                    if (uri != null) {
                        Log.d(TAG, "crop enclose Image, uri:" + i + uri);
                        try {
                            BitmapUtils.writeBitmapToUri(context, resizeBitmap, uri, saveCompressFormat, saveCompressQuality);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        resizeBitmap.recycle();
                        result = new CropResult(uri, bitmapSampled.sampleSize);
                    } else {
                        Log.d(TAG, "crop enclose Image, uri == null");
                        result = new CropResult(resizeBitmap, bitmapSampled.sampleSize);
                    }
                    results.add(result);
                }
            }
        }

        // 剪切并保存拆题之后的截图
        for (int i = 0; i < splitPoints.size(); i++) {
            BitmapUtils.BitmapSampled bitmapSampled;
            CropResult result;
            float[] cropPoint = splitPoints.get(i);
            Uri uri = splitUriList.get(i);
            if (bitmap != null) {
                Log.d(TAG, "crop split Image, create bitmapSampled");
                bitmapSampled = BitmapUtils.cropBitmapObjectHandleOOM(bitmap, cropPoint,
                        degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY, false, true);
            } else {
                Log.d(TAG, "crop split Image, Bitmap == null");
                result = new CropResult((Bitmap) null, 1);
                results.add(result);
                return results;
            }
            Bitmap resizeBitmap = BitmapUtils.resizeBitmap(bitmapSampled.bitmap, reqWidth, reqHeight, options);
            if (resizeBitmap != null) {
                if (uri != null) {
                    Log.d(TAG, "crop split Image, uri:" + i + uri);
                    try {
                        BitmapUtils.writeBitmapToUri(context, resizeBitmap, uri, saveCompressFormat, saveCompressQuality);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    resizeBitmap.recycle();
                    result = new CropResult(uri, bitmapSampled.sampleSize);
//                    CropHelper.notificationAlbumRefresh(uri, context); // 通知相册刷新
                } else {
                    Log.d(TAG, "crop split Image, uri == null");
                    result = new CropResult(resizeBitmap, bitmapSampled.sampleSize);
                }
                results.add(result);
            }
        }
        return results;
    }

// 图片预处理
//    private static Bitmap bitmapPreprocessing(Bitmap bitmap) {
//        Bitmap equalBitmap = OpenCVUtil.autoHistEqualize(bitmap);
//        if (equalBitmap != null) {
//            Bitmap correctBitmap = OpenCVUtil.correct2(equalBitmap);
//            if (correctBitmap != null) {
//                return correctBitmap;
//            }
//            return equalBitmap;
//        }
//        return bitmap;
//    }
}
