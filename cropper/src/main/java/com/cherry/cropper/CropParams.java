package com.cherry.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

/**
 * - 11:00 2014/10/03 Encapsulate the crop params.
 * - 13:20 2014/10/03 Put the initialization into constructor method.
 * - 14:00 2014/10/03 Make the crop as String instead of Boolean.
 * - 14:30 2014/10/03 Increase the default output size from 200 to 300.
 * - 12:20 2014/10/04 Add "scaleUpIfNeeded" crop options for scaling up cropped images if the size is too small.
 * - 23:10 2015/08/20 Add enable to toggle on/off the crop function.
 * - 13:30 2015/08/21 Add compress options to avoid OOM or other problems.
 * - 23:00 2015/09/05 Add default compress width & height.
 */
public class CropParams {

    public static final String CROP_TYPE = "image/*";
    public static final String OUTPUT_FORMAT = Bitmap.CompressFormat.JPEG.toString();

    public static final int DEFAULT_ASPECT = 1;
    public static final int DEFAULT_OUTPUT = 300;
    public static final int DEFAULT_COMPRESS_WIDTH = 640;
    public static final int DEFAULT_COMPRESS_HEIGHT = 854;
    public static final int DEFAULT_COMPRESS_QUALITY = 90;

    public Uri uri;

    public String type;
    public String outputFormat;
    public String crop;

    public boolean scale;
    public boolean returnData;
    public boolean noFaceDetection;
    public boolean scaleUpIfNeeded;

    /**
     * 默认为true，如果设置为false，只会从图库中获取图像或从相机中获取图像, 裁剪函数将不起作用。
     */
    public boolean enable;

    /**
     * 是否压缩图片, 默认值为false(压缩)，如果是相机拍摄而没有裁剪，则图像可能很大, 足够触发OOM，
     * 最好在enable为false时压缩图像
     */
    public boolean compress;

    public boolean rotateToCorrectDirection;

    public int compressWidth;
    public int compressHeight;
    public int compressQuality;

    public int aspectX;
    public int aspectY;

    public int outputX;
    public int outputY;

    public Context context;

    public CropParams(Context context) {
        this.context = context;
        type = CROP_TYPE;
        outputFormat = OUTPUT_FORMAT;
        crop = "true";
        scale = true;
        returnData = false;
        noFaceDetection = true;
        scaleUpIfNeeded = true;
        enable = true;
        rotateToCorrectDirection = false;
        compress = false;
        compressQuality = DEFAULT_COMPRESS_QUALITY;
        compressWidth = DEFAULT_COMPRESS_WIDTH;
        compressHeight = DEFAULT_COMPRESS_HEIGHT;
//        aspectX = 1;
//        aspectY = 2;
        outputX = DEFAULT_OUTPUT;
        outputY = DEFAULT_OUTPUT;
//        refreshUri();
    }

    public void refreshUri() {
        uri = CropHelper.generateUri(context);
    }
}
