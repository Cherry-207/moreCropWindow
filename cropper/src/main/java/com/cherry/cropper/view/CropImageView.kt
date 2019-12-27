package com.cherry.cropper.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.cherry.cropper.CropFileUtils
import com.cherry.cropper.R
import com.cherry.cropper.handler.CropImageOptions
import com.cherry.cropper.handler.CropImageTask
import com.cherry.cropper.utils.CropResult
import com.cherry.cropper.utils.Enum
import com.cherry.cropper.utils.SimpleLog
import com.cherry.cropper.view.CropOverlayView.CREATE_ENCLOSE_CROP_WINDOW
import kotlinx.android.synthetic.main.crop_image_view.view.*
import java.util.*

/**
 * @author pengxiaobao
 * @date 2019/9/5
 * @description
 */
class CropImageView(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    private var mOptions: CropImageOptions? = null
    /**
     * 裁剪图像视图中图像的初始缩放类型
     */
    private val mScaleType: Enum.ScaleType
    /**
     * 是否使用自动缩放功能
     * 默认是true
     */
    private var mAutoZoomEnabled = true
    /**
     * 剪切时允许的最大缩放
     */
    private val mMaxZoom: Int
    /**
     * 是否在异步加载图片或者剪切图片时显示进度条
     * 默认是true(显示), 同时禁用自定义进度条
     */
    private var mShowProgressBar = true
    /**
     * 是否在原图的上方显示剪切框视图
     * 默认是true, 可以禁用动画或画面转换。
     */
    private var mShowCropOverlay = true
    /**
     * 视图边距
     */
    private val mCropLayoutPadding: Int
    /**
     * The sample size the image was loaded by if was loaded by URI
     */
    private var mLoadedSampleSize = 1
    /**
     * 用于在图像视图中转换裁剪图像的矩阵
     */
    private val mBaseImageMatrix = Matrix()
    /**
     * 重用矩阵实例进行反向矩阵计算。
     */
    private val mBaseImageInverseMatrix = Matrix()
    /**
     * 当前被裁剪图像可缩放等级
     */
    private var mZoom = 1f
    /**
     * 缩放后平移裁剪图像的X轴偏移量
     */
    private var mZoomOffsetX: Float = 0.toFloat()
    /**
     * 缩放后平移裁剪图像的Y轴偏移量
     */
    private var mZoomOffsetY: Float = 0.toFloat()

    private val mCropWindowOperator: CropWindowOperator

    private var maxWidth: Int? = 0
    private var maxHeight: Int? = 0

    init {
        mOptions = CropImageOptions()
        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.CropFrameLayout, 0, 0)
        try {
            mOptions!!.fixAspectRatio = ta.getBoolean(R.styleable.CropFrameLayout_cropFixAspectRatio, mOptions!!.fixAspectRatio)
            mOptions!!.aspectRatioX = ta.getInteger(R.styleable.CropFrameLayout_cropAspectRatioX, mOptions!!.aspectRatioX)
            mOptions!!.aspectRatioY = ta.getInteger(R.styleable.CropFrameLayout_cropAspectRatioY, mOptions!!.aspectRatioY)
            mOptions!!.scaleType = Enum.ScaleType.values()[ta.getInt(R.styleable.CropFrameLayout_cropScaleType, mOptions!!.scaleType.ordinal)]
            mOptions!!.autoZoomEnabled = ta.getBoolean(R.styleable.CropFrameLayout_cropAutoZoomEnabled, mOptions!!.autoZoomEnabled)
            mOptions!!.multiTouchEnabled = ta.getBoolean(R.styleable.CropFrameLayout_cropMultiTouchEnabled, mOptions!!.multiTouchEnabled)
            mOptions!!.maxZoom = ta.getInteger(R.styleable.CropFrameLayout_cropMaxZoom, mOptions!!.maxZoom)
            mOptions!!.cropShape = Enum.CropShape.values()[ta.getInt(R.styleable.CropFrameLayout_cropShape, mOptions!!.cropShape.ordinal)]
            mOptions!!.guidelines = Enum.Guidelines.values()[ta.getInt(R.styleable.CropFrameLayout_cropGuidelines, mOptions!!.guidelines.ordinal)]
            mOptions!!.snapRadius = ta.getDimension(R.styleable.CropFrameLayout_cropSnapRadius, mOptions!!.snapRadius)
            mOptions!!.touchRadius = ta.getDimension(R.styleable.CropFrameLayout_cropTouchRadius, mOptions!!.touchRadius)
            mOptions!!.initialCropWindowPaddingRatio = ta.getFloat(R.styleable.CropFrameLayout_cropInitialCropWindowPaddingRatio, mOptions!!.initialCropWindowPaddingRatio)
            mOptions!!.borderLineThickness = ta.getDimension(R.styleable.CropFrameLayout_cropBorderLineThickness, mOptions!!.borderLineThickness)
            mOptions!!.borderLineColor = ta.getInteger(R.styleable.CropFrameLayout_cropBorderLineColor, mOptions!!.borderLineColor)
            mOptions!!.borderCornerThickness = ta.getDimension(R.styleable.CropFrameLayout_cropBorderCornerThickness, mOptions!!.borderCornerThickness)
            mOptions!!.borderCornerOffset = ta.getDimension(R.styleable.CropFrameLayout_cropBorderCornerOffset, mOptions!!.borderCornerOffset)
            mOptions!!.borderCornerLength = ta.getDimension(R.styleable.CropFrameLayout_cropBorderCornerLength, mOptions!!.borderCornerLength)
            mOptions!!.borderCornerColor = ta.getInteger(R.styleable.CropFrameLayout_cropBorderCornerColor, mOptions!!.borderCornerColor)
            mOptions!!.guidelinesThickness = ta.getDimension(R.styleable.CropFrameLayout_cropGuidelinesThickness, mOptions!!.guidelinesThickness)
            mOptions!!.guidelinesColor = ta.getInteger(R.styleable.CropFrameLayout_cropGuidelinesColor, mOptions!!.guidelinesColor)
            mOptions!!.backgroundColor = ta.getInteger(R.styleable.CropFrameLayout_cropBackgroundColor, mOptions!!.backgroundColor)
            mOptions!!.showCropOverlay = ta.getBoolean(R.styleable.CropFrameLayout_cropShowCropOverlay, mShowCropOverlay)
            mOptions!!.showProgressBar = ta.getBoolean(R.styleable.CropFrameLayout_cropShowProgressBar, mShowProgressBar)
            mOptions!!.borderCornerThickness = ta.getDimension(R.styleable.CropFrameLayout_cropBorderCornerThickness, mOptions!!.borderCornerThickness)
            mOptions!!.minCropWindowWidth = ta.getDimension(R.styleable.CropFrameLayout_cropMinCropWindowWidth, mOptions!!.minCropWindowWidth.toFloat()).toInt()
            mOptions!!.minCropWindowHeight = ta.getDimension(R.styleable.CropFrameLayout_cropMinCropWindowHeight, mOptions!!.minCropWindowHeight.toFloat()).toInt()
            mOptions!!.minCropResultWidth = ta.getFloat(R.styleable.CropFrameLayout_cropMinCropResultWidthPX, mOptions!!.minCropResultWidth.toFloat()).toInt()
            mOptions!!.minCropResultHeight = ta.getFloat(R.styleable.CropFrameLayout_cropMinCropResultHeightPX, mOptions!!.minCropResultHeight.toFloat()).toInt()
            mOptions!!.maxCropResultWidth = ta.getFloat(R.styleable.CropFrameLayout_cropMaxCropResultWidthPX, mOptions!!.maxCropResultWidth.toFloat()).toInt()
            mOptions!!.maxCropResultHeight = ta.getFloat(R.styleable.CropFrameLayout_cropMaxCropResultHeightPX, mOptions!!.maxCropResultHeight.toFloat()).toInt()

            // 如果高宽比设置，则将fixed设置为true
            if (ta.hasValue(R.styleable.CropFrameLayout_cropAspectRatioX) &&
                    ta.hasValue(R.styleable.CropFrameLayout_cropAspectRatioX) &&
                    !ta.hasValue(R.styleable.CropFrameLayout_cropFixAspectRatio)) {
                mOptions!!.fixAspectRatio = true
            }
        } finally {
            ta.recycle()
        }
        mOptions!!.validate()
        LayoutInflater.from(context).inflate(R.layout.crop_image_view, this@CropImageView, true)
        image_view.scaleType = ImageView.ScaleType.MATRIX
        mScaleType = mOptions!!.scaleType
        mAutoZoomEnabled = mOptions!!.autoZoomEnabled
        mShowCropOverlay = mOptions!!.showCropOverlay
        mMaxZoom = mOptions!!.maxZoom
        mShowProgressBar = mOptions!!.showProgressBar
        mCropLayoutPadding = context.resources.getDimension(R.dimen.aci_crop_layout_padding).toInt()
        crop_overlay_view.setInitialAttributeValues(mOptions)

        val metrics = context.getResources().getDisplayMetrics()
        maxWidth = (metrics.widthPixels)
        maxHeight = (metrics.heightPixels) - context.resources.getDimension(R.dimen.tab_layout_height).toInt()
        SimpleLog.i("BitmapUtils", "maxWidth:$maxWidth")
        SimpleLog.i("BitmapUtils", "maxHeight:$maxHeight")
        mCropWindowOperator = CropWindowOperator()
    }

    fun setImageBitmap(bitmap: Bitmap) {
        image_view.setImageBitmap(bitmap)
        reset()
        image_view.clearAnimation()
        crop_overlay_view.setCropWindowType(CREATE_ENCLOSE_CROP_WINDOW)
        mCropWindowOperator.applyImageMatrix(bitmap, RectF(0f, 0f, 0f, 0f), mBaseImageMatrix, mBaseImageInverseMatrix,
                crop_overlay_view, 0, mScaleType, mAutoZoomEnabled, mZoom,
                mZoomOffsetX, mZoomOffsetY, image_view, getAvailWidth().toFloat(),
                getAvailHeight().toFloat(), mCropLayoutPadding.toFloat(), true, mLoadedSampleSize, false)
    }

    // 打开或关闭裁剪功能
    fun setCropEnabled(enabled: Boolean) {
        if (enabled) {
            crop_overlay_view.visibility = View.VISIBLE
        } else {
            crop_overlay_view.visibility = View.INVISIBLE
        }
    }

    // 对拆题之后的单题进行剪切
    fun cropSplitImage(splitBitmap: Bitmap, cropOverlayView: CropOverlayView, splitUriList: ArrayList<Uri>,
                       splitCropWindowRects: MutableList<RectF>): ArrayList<CropResult> {
        val splitPoints = ArrayList<FloatArray>()
        // 拆题剪切框区域列表
        for (rect in splitCropWindowRects) {
            if (rect.isEmpty) continue
            val cropPoint = getCropPoints(rect, mBaseImageMatrix)
            splitPoints.add(cropPoint)
        }
        return CropImageTask.cropSplitAndEncloseImage(
                context,
                splitBitmap,
                splitUriList,
                null,
                splitPoints,
                null,
                0,
                cropOverlayView.isFixAspectRatio(),
                cropOverlayView.getAspectRatioX(),
                cropOverlayView.getAspectRatioY(),
                if (mOptions !== Enum.RequestSizeOptions.NONE) mOptions!!.maxCropResultWidth else 0,
                if (mOptions !== Enum.RequestSizeOptions.NONE) mOptions!!.maxCropResultHeight else 0,
                mOptions!!.outputRequestSizeOptions,
                mOptions!!.outputCompressFormat,
                mOptions!!.outputCompressQuality)
    }

    // 清除之前的图片及初始化一些数据
    fun reset() {
        mBaseImageMatrix.reset()
        mLoadedSampleSize = 1
    }

    // FragmentWidth就是屏幕宽度
    fun getFragmentWidth(): Int? {
        return maxWidth
    }

    // FragmentHeight就是屏幕高度 - TabLayout高度
    fun getFragmentHeight(): Int? {
        return maxHeight
    }

    /**
     * 当前View可以使用的最大宽度
     *
     * @return
     */
    fun getAvailWidth(): Int {
        return maxWidth!! - mCropLayoutPadding * 2
    }

    /**
     * 当前View可以使用的最大高度
     *
     * @return
     */
    fun getAvailHeight(): Int {
        return maxHeight!! - mCropLayoutPadding * 2
    }

    fun getCropLayoutPadding(): Int {
        return mCropLayoutPadding
    }

    fun clearCuttingMatrix() {
        mBaseImageMatrix.reset()
        mBaseImageInverseMatrix.reset()
    }

    /**
     * 生成截图的Uri, fromFile() 生成的是file:// 开头的
     */
    fun generateCropViewUri(fileNameType: String): Uri {
        var outputUri = mOptions!!.outputUri
        if (outputUri.equals(Uri.EMPTY)) {
            val file = CropFileUtils.createMediaFile(context, fileNameType)
            outputUri = Uri.fromFile(file)
        }
        SimpleLog.i("CropManager", "outputUri: $outputUri")
        return outputUri
    }

    /**
     * 获取裁剪窗口相对于源位图的位置的4个点
     * @return 裁剪区域边界的4个点(x0, y0, x1, y1, x2, y2, x3, y3)
     */
    fun getCropPoints(cropWindowRect: RectF, imageMatrix: Matrix): FloatArray {
        // 获取相对于显示图像的裁剪窗口位置。
        val points = floatArrayOf(cropWindowRect.left, cropWindowRect.top, cropWindowRect.right, cropWindowRect.top, cropWindowRect.right, cropWindowRect.bottom, cropWindowRect.left, cropWindowRect.bottom)
        imageMatrix.invert(mBaseImageInverseMatrix)
        mBaseImageInverseMatrix.mapPoints(points)
        for (i in points.indices) {
            points[i] *= mLoadedSampleSize.toFloat()
        }
        return points
    }
}