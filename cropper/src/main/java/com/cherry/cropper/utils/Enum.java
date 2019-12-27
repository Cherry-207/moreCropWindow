package com.cherry.cropper.utils;

/**
 * @author pengxiaobao
 * @date 2019/1/27
 * @description
 */
public class Enum {

    /**
     * 剪切框内部分割线的显示状态
     */
    public enum Guidelines {
        /**
         * 从不显示
         */
        OFF,

        /**
         * 拖动剪切框时显示
         */
        ON_TOUCH,

        /**
         * 一直显示
         */
        ON
    }

    /**
     * 剪切框的形状
     * 若要设置方形/圆形形状，请将长宽比设置为1:1。
     */
    public enum CropShape {
        RECTANGLE,
        OVAL
    }

    /**
     * Options for scaling the bounds of cropping image to the bounds of Crop Image View.<br>
     * 将裁剪图像的边界缩放到裁剪图像视图的边界的选项。
     * 注意:如果启用自动缩放，有些选项会受到影响。
     * Note: Some options are affected by auto-zoom, if enabled.
     */
    public enum ScaleType {

        /**
         * Scale the image uniformly (maintain the image's aspect ratio) to fit in crop image view.<br>
         * 均匀缩放图像(保持图像的宽高比)以适应裁剪图像视图。
         * The largest dimension will be equals to crop image view and the second dimension will be smaller.
         */
        FIT_CENTER,

        /**
         * 将图像居中，但不执行缩放。
         * 注意:如果启用自动缩放，并且源图像比裁剪图像视图小，那么原图将放大，以适应作物图像视图。
         */
        CENTER,

        /**
         * 均匀地缩放图像(保持图像的高宽比)
         * 让图像被居中显示在视图中。
         */
        CENTER_CROP,

        /**
         * 均匀地缩放图像(保持图像的高宽比)，
         * 让图像被居中显示在视图中。
         * 注意:如果启用自动缩放，并且源图像比裁剪图像视图小，那么原图将放大，以适应作物图像视图。
         */
        CENTER_INSIDE
    }

    /**
     * 处理裁剪所需的宽度/高度的可能选项。
     */
    public enum RequestSizeOptions {

        /**
         * 除非内存管理(OOM)需要，否则不进行调整/采样。
         */
        NONE,

        /**
         * Only sample the image during loading (if image set using URI) so the smallest of the image
         * dimensions will be between the requested size and x2 requested size.<br>
         * NOTE: resulting image will not be exactly requested width/height
         * see: <a href="http://developer.android.com/training/displaying-bitmaps/load-bitmap.html">Loading Large
         * Bitmaps Efficiently</a>.
         */
        SAMPLING,

        /**
         * Resize the image uniformly (maintain the image's aspect ratio) so that both
         * dimensions (width and height) of the image will be equal to or <b>less</b> than the
         * corresponding requested dimension.<br>
         * If the image is smaller than the requested size it will NOT change.
         */
        RESIZE_INSIDE,

        /**
         * Resize the image uniformly (maintain the image's aspect ratio) to fit in the given width/height.<br>
         * The largest dimension will be equals to the requested and the second dimension will be smaller.<br>
         * If the image is smaller than the requested size it will enlarge it.
         */
        RESIZE_FIT,

        /**
         * Resize the image to fit exactly in the given width/height.<br>
         * This resize method does NOT preserve aspect ratio.<br>
         * If the image is smaller than the requested size it will enlarge it.
         */
        RESIZE_EXACT
    }
}
