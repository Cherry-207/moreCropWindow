package com.cherry.cropper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

import static com.cherry.cropper.CropFileUtils.CAMERA_PATH;


public class CropHelper {

    public static final String TAG = "OCR:CropHelper";

    public static final int REQUEST_CROP = 127;  // 截图请求
    public static final int REQUEST_CAMERA = 128;// 拍照请求
    public static final int REQUEST_PICK = 129;  // 选取照片请求
    public static File mediaFile; // 生成的图片文件名称


    /**
     * 生成图片File
     *
     * @param context
     * @return
     */
    public static File generateFile(Context context) {
        return CropFileUtils.createMediaFile(context, "BASE");
    }

    /**
     * 生成图片Uri
     */
    public static Uri generateUri(Context context) {
        Uri imageUri;
        mediaFile = generateFile(context);
        imageUri = generateUri(context, mediaFile);
        return imageUri;
    }

    /**
     * 生成图片Uri
     */
    public static Uri generateUri(Context context, File file) {
        Uri imageUri;
        if (file == null) {
            file = generateFile(context);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //通过FileProvider创建一个content:// 类型的Uri
            imageUri = FileProvider.getUriForFile(context, "com.okay.studentocr", file);
        } else {
            // 生成File://开头的uri
            imageUri = Uri.fromFile(file);
        }
        Log.d(TAG, "imageUri: " + imageUri);
        Log.d(TAG, "cameraPath: " + file.getAbsolutePath());
        return imageUri;
    }

    /**
     * 保存完截图, 通知相册刷新
     *
     * @param uri
     * @param context
     */
    public static void notificationAlbumRefresh(Uri uri, Context context) {
        if (uri != null && uri != Uri.EMPTY) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(uri);
            context.sendBroadcast(intent);
        }
    }

    public static Intent buildGalleryIntent(CropParams params) {
        Intent intent;
        if (params.enable) {
            intent = buildCropIntent(Intent.ACTION_PICK, params);
        } else {
//            intent = new Intent(Intent.ACTION_GET_CONTENT)
//                    .setType("image/*")
//                    .putExtra(MediaStore.EXTRA_OUTPUT, params.uri);
            intent = new Intent(Intent.ACTION_PICK);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, params.uri);
            intent.setType("image/*");
//            intent = new Intent(Intent.ACTION_PICK, null);
//            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        }
        return intent;
    }

    private static Intent buildCropFromUriIntent(CropParams params) {
        return buildCropIntent("com.android.camera.action.CROP", params);
    }

    private static Intent buildCropIntent(String action, CropParams params) {
        return new Intent(action)
                .setDataAndType(params.uri, params.type)
                .putExtra("crop", "true")
                .putExtra("scale", params.scale)
                .putExtra("aspectX", params.aspectX)
                .putExtra("aspectY", params.aspectY)
                .putExtra("outputX", params.outputX)
                .putExtra("outputY", params.outputY)
                .putExtra("return-data", params.returnData)
                .putExtra("outputFormat", params.outputFormat)
                .putExtra("noFaceDetection", params.noFaceDetection)
                .putExtra("scaleUpIfNeeded", params.scaleUpIfNeeded)
                .putExtra(MediaStore.EXTRA_OUTPUT, params.uri);
    }

    /**
     * 删除相册中保存的拆题图片
     */
    public static void clearCacheSplitImage() {
        File cacheFolder = new File(Environment.getExternalStorageDirectory() + File.separator + CAMERA_PATH);
        if (cacheFolder.exists() && cacheFolder.listFiles() != null) {
            for (File file : cacheFolder.listFiles()) {
                if (file.getName().contains("SPLIT")) {
                boolean result = file.delete();
                Log.d(TAG, "Delete " + file.getAbsolutePath() + (result ? " succeeded" : " failed"));
                }
            }
        }
    }

    /**
     * 清理指定的图片
     *
     * @param uri
     * @return
     */
    public static boolean clearFile(Uri uri) {
        if (uri == null) return false;
        File file = new File(uri.getPath());
        if (file.exists()) {
            boolean result = file.delete();
            Log.d(TAG, "Delete " + file.getAbsolutePath() + (result ? " succeeded" : " failed"));
            return result;
        }
        return false;
    }
}
