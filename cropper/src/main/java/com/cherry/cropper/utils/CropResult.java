package com.cherry.cropper.utils;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author pengxiaobao
 * @date 2019/1/24
 * @description 剪切图片结果类
 */
public class CropResult implements Parcelable {

    /**
     * The cropped image bitmap result.<br>
     * Null if save cropped image was executed, no output requested or failure.
     */
    private Bitmap mBitmap;

    /**
     * The Android uri of the saved cropped image result.<br>
     * Null if get cropped image was executed, no output requested or failure.
     */
    private Uri mUri;

    /**
     * The error that failed the loading/cropping (null if successful)
     */
    private Exception mError;

    /**
     * The 4 points of the cropping window in the source image
     */
    private float[] mCropPoints;

    /**
     * The rectangle of the cropping window in the source image
     */
    private Rect mCropRect;

    /**
     * The final rotation of the cropped image relative to source
     */
    private int mRotation;

    /**
     * is the cropping request was to get a bitmap or to save it to uri
     */
    public boolean isSave;

    /**
     * sample size used creating the crop bitmap to lower its size
     */
    private int mSampleSize;

    public CropResult(Uri uri) {
        mUri = uri;
    }

//    public CropResult(Bitmap bitmap, Uri uri, Exception error, Rect cropRect, int rotation, int sampleSize) {
//        mBitmap = bitmap;
//        mUri = uri;
//        mError = error;
//        mCropRect = cropRect;
//        mRotation = rotation;
//        mSampleSize = sampleSize;
//    }

    public CropResult(Bitmap bitmap, int sampleSize) {
        this.mBitmap = bitmap;
        this.mUri = null;
        this.mError = null;
        this.isSave = false;
        this.mSampleSize = sampleSize;
    }

    public CropResult(Uri uri, int sampleSize) {
        this.mBitmap = null;
        this.mUri = uri;
        this.mError = null;
        this.isSave = true;
        this.mSampleSize = sampleSize;
    }

//    public CropResult(Exception error, boolean isSave) {
//        this.mBitmap = null;
//        this.mUri = null;
//        this.mError = error;
//        this.isSave = isSave;
//        this.mSampleSize = 1;
//    }

    protected CropResult(Parcel in) {
        this((Uri) in.readParcelable(Uri.class.getClassLoader()));

    }

    /**
     * Is the result is success or error.
     */
    public boolean isSuccessful() {
        return mError == null;
    }

    /**
     * The cropped image bitmap result.<br>
     * Null if save cropped image was executed, no output requested or failure.
     */
    public Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * The Android uri of the saved cropped image result
     * Null if get cropped image was executed, no output requested or failure.
     */
    public Uri getUri() {
        return mUri;
    }

    /**
     * The error that failed the loading/cropping (null if successful)
     */
    public Exception getError() {
        return mError;
    }

    /**
     * The 4 points of the cropping window in the source image
     */
    public float[] getCropPoints() {
        return mCropPoints;
    }

    /**
     * The rectangle of the cropping window in the source image
     */
    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * The final rotation of the cropped image relative to source
     */
    public int getRotation() {
        return mRotation;
    }

    /**
     * sample size used creating the crop bitmap to lower its size
     */
    public int getSampleSize() {
        return mSampleSize;
    }

    public boolean isSave() {
        return isSave;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(getUri(), flags);
//        dest.writeSerializable(getError());
//        dest.writeParcelable(getCropRect(), flags);
//        dest.writeInt(getRotation());
//        dest.writeInt(getSampleSize());
    }

    public static final Creator<CropResult> CREATOR = new Creator<CropResult>() {

        @Override
        public CropResult createFromParcel(Parcel parcel) {
            return new CropResult(parcel);
        }

        @Override
        public CropResult[] newArray(int i) {
            return new CropResult[i];
        }
    };

    @Override
    public String toString() {
        return "CropResult:{" +
                "bitmap:" + mBitmap + "," +
                "uri:" + mUri + "," +
                "error:" + mError + "," +
                "cropRect:" + mCropRect + "," +
                "rotation:" + mRotation + "," +
                "sampleSize:" + mSampleSize + "," +
                "}";
    }
}
