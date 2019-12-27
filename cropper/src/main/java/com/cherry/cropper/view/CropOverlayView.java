// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.cherry.cropper.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.cherry.cropper.R;
import com.cherry.cropper.handler.BitmapUtils;
import com.cherry.cropper.handler.CropImageOptions;
import com.cherry.cropper.handler.CropWindowHandler;
import com.cherry.cropper.handler.CropWindowMoveHandler;
import com.cherry.cropper.utils.Enum;
import com.cherry.cropper.utils.SimpleLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A custom View representing the crop window and the shaded background outside the crop window.
 * 一个自定义视图，表示裁剪窗口和裁剪窗口外的阴影背景。
 * 裁剪框视图
 */
public class CropOverlayView extends View {

    private static final String TAG = "OCR:" + CropOverlayView.class.getName();

    /**
     * Gesture detector used for multi touch box scaling
     */
    private ScaleGestureDetector mScaleDetector;

    /**
     * Boolean to see if multi touch is enabled for the crop rectangle
     * 布尔值，以查看是否为裁切矩形启用了多点触摸
     */
    private boolean mMultiTouchEnabled;

    /**
     * Handler from crop window stuff, moving and knowing position.
     */
    private final CropWindowHandler mCropWindowHandler = new CropWindowHandler();

    /**
     * 剪切框变化的监听器
     */
    private CropWindowChangeListener mCropWindowChangeListener;

    /**
     * Rectangle used for drawing
     */
    private final RectF mDrawRect = new RectF();

    /**
     * 用于画矩形边线的Paint
     */
    private Paint mBorderPaint;

    /**
     * 用来画边角的Paint
     */
    private Paint mBorderCornerPaint;

    /**
     * 在剪切框的矩形区域内画分割线的Paint
     */
    private Paint mGuidelinePaint;

    /**
     * 将矩形之外的区域变暗的Paint
     */
    private Paint mBackgroundPaint;

    private Paint mUnactivatedPaint;

    /**
     * 用于椭圆裁切窗口形状或非直线旋转绘图
     */
    private Path mPath = new Path();

    /**
     * 用于记录被激活的裁剪区域
     * ADD BY OKAY: lucien liu
     */
    private Path mActivatedPath = new Path();

    /**
     * 用于记录 未被激活裁剪区域 - 激活裁剪区域（两者取差集）
     * ADD BY OKAY: lucien liu
     */
    private Path mUnactivatedPath = new Path();

    /**
     * The bounding box around the Bitmap that we are cropping.
     */
    private final float[] mBoundsPoints = new float[8];

    /**
     * The bounding box around the Bitmap that we are cropping.
     */
    private final RectF mCalcBounds = new RectF();

    /**
     * 视图的右侧边界，当视图起始点为（0,0）时，mViewRight 等于视图的宽度
     */
    private int mViewRight;

    /**
     * 视图的下侧边界，当视图起始点为（0,0）时，mViewHeight 等于视图的高度
     */
    private int mViewBottom;

    // 视图的左侧边界
    private int viewLeft;
    // 视图的上侧边界
    private int viewTop;

    /**
     * The offset to draw the border corener from the border
     */
    private float mBorderCornerOffset;

    /**
     * the length of the border corner to draw
     */
    private float mBorderCornerLength;

    /**
     * 初始剪切框距离边界的padding
     */
    private float mInitialCropWindowPaddingRatio;

    /**
     * The radius of the touch zone (in pixels) around a given Handle.
     */
    private float mTouchRadius;

    /**
     * An edge of the crop window will snap to the corresponding edge of a specified bounding box
     * when the crop window edge is less than or equal to this distance (in pixels) away from the bounding box edge.
     */
    private float mSnapRadius;

    /**
     * The Handle that is currently pressed; null if no Handle is pressed.
     */
    private CropWindowMoveHandler mMoveHandler;

    /**
     * Flag indicating if the crop area should always be a certain aspect ratio (indicated by mTargetAspectRatio).
     * 长宽比是否固定
     */
    private boolean mFixAspectRatio;

    /**
     * 长宽比的x值
     */
    private int mAspectRatioX;

    /**
     * 长宽比的y值
     */
    private int mAspectRatioY;

    /**
     * 裁剪框应该保持的纵横比
     * 此变量仅在mMaintainAspectRatio为true时使用
     */
    private float mTargetAspectRatio = ((float) mAspectRatioX) / mAspectRatioY;

    /**
     * 剪切框内分割线的显示模式
     */
    private Enum.Guidelines mGuidelines;

    /**
     * 裁剪区域的形状-矩形/圆形
     */
    private Enum.CropShape mCropShape;

    /**
     * 是否首次初始化裁剪视图
     */
    private boolean initializedCropWindow;

    /**
     * Used to set back LayerType after changing to software.
     */
    private Integer mOriginalLayerType;

    // 创建什么类型的剪切框
    private int cropWindowType;

    /**
     * 配置信息
     */
    private CropImageOptions options;

    /**
     * 裁剪框删除按钮
     */
    private Bitmap delBtnBM;

    private boolean allowSlideRect = true;

    // 创建拆题的剪切框
    public final static int CREATE_SPLIT_CROP_WINDOW = 0x01;
    // 创建圈图的剪切框
    public final static int CREATE_ENCLOSE_CROP_WINDOW = 0x02;
    // 标记用于裁剪的裁剪框
    public final static int CREATE_CROP_CROP_WINDOW = 0x03;

    // 设置是否允许滑动剪切框
    public void setCropWindowSlide(Boolean allow) {
        this.allowSlideRect = allow;
    }

    public int setCropWindowType(int cropWindowType) {
        return this.cropWindowType = cropWindowType;
    }

    public CropOverlayView(Context context) {
        this(context, null);
    }

    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getViewRight() {
        return mViewRight;
    }

    public int getViewBottom() {
        return mViewBottom;
    }

    public int getViewLeft() {
        return viewLeft;
    }

    public int getViewTop() {
        return viewTop;
    }

    /**
     * 设置裁剪窗口变化的监听器
     */
    public void setCropWindowChangeListener(CropWindowChangeListener listener) {
        mCropWindowChangeListener = listener;
    }

    /**
     * 获取激活状态下的裁剪窗口矩形
     */
    public RectF getCropWindowRect() {
        return mCropWindowHandler.getRect();
    }

    /**
     * 删除全部剪切框
     */
    public void clearCropWindowRects() {
        mCropWindowHandler.clearRects();
    }

    /**
     * 获取全部 CropOverlayView 下的裁剪窗口
     *
     * @return
     */
    public List<RectF> getCropWindowRects() {
        return mCropWindowHandler.getRects();
    }

    /**
     * 设置裁剪窗口的左/上/右/下坐标
     */
    public void setCropWindowRect(RectF rect) {
        mCropWindowHandler.setRect(rect);
    }

    public void setInitRect(RectF rect) {
        mCropWindowHandler.setInitRect(rect);
    }

    /**
     * 如果当前裁剪窗口矩形位于要裁剪图像或视图边界之外，则修复该矩形
     */
//    public void fixCurrentCropWindowRect() {
//        RectF rect = getCropWindowRect();
//        fixCropWindowRectByRules(rect);
//        mCropWindowHandler.setRect(rect);
//    }

    /**
     * 通知裁剪框要被裁剪图片的相对位置
     *
     * @param boundsPoints 图像的边界点
     */
    public void setBounds(float[] boundsPoints, int viewRight, int viewBottom) {
        SimpleLog.i("CropOverlayView", "setBounds()");
        if (boundsPoints == null) {
            Arrays.fill(mBoundsPoints, 0);
        } else if (!Arrays.equals(mBoundsPoints, boundsPoints)) {
            System.arraycopy(boundsPoints, 0, mBoundsPoints, 0, boundsPoints.length);
        }
        viewLeft = (int) BitmapUtils.getRectLeft(mBoundsPoints);
        viewTop = (int)BitmapUtils.getRectTop(mBoundsPoints);
        mViewRight = viewRight;
        mViewBottom = viewBottom;
//        SimpleLog.i("CropOverlayView", "viewLeft: " + viewLeft);
//        SimpleLog.i("CropOverlayView", "viewTop: " + viewTop);
//        SimpleLog.i("CropOverlayView", "viewRight: " + viewRight);
//        SimpleLog.i("CropOverlayView", "mViewBottom: " + mViewBottom);
        initCropWindow();
    }

    /**
     * The shape of the cropping area - rectangle/circular.
     */
    public Enum.CropShape getCropShape() {
        return mCropShape;
    }

    /**
     * 设置裁剪区域的形状-矩形/圆形。
     */
    public void setCropShape(Enum.CropShape cropShape) {
        if (mCropShape != cropShape) {
            mCropShape = cropShape;
            if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT <= 17) {
                if (mCropShape == Enum.CropShape.OVAL) {
                    mOriginalLayerType = getLayerType();
                    if (mOriginalLayerType != View.LAYER_TYPE_SOFTWARE) {
                        // TURN off hardware acceleration
                        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    } else {
                        mOriginalLayerType = null;
                    }
                } else if (mOriginalLayerType != null) {
                    // return hardware acceleration back
                    setLayerType(mOriginalLayerType, null);
                    mOriginalLayerType = null;
                }
            }
            invalidate();
        }
    }

    /**
     * Get the current guidelines option set.
     */
    public Enum.Guidelines getGuidelines() {
        return mGuidelines;
    }

    /**
     * Sets the guidelines for the CropOverlayView to be either on, off, or to show when resizing the application.
     */
    public void setGuidelines(Enum.Guidelines guidelines) {
        if (mGuidelines != guidelines) {
            mGuidelines = guidelines;
            if (initializedCropWindow) {
                invalidate();
            }
        }
    }

    /**
     * 长宽比是否固定; true固定长宽比，而false允许对其进行更改。
     */
    public boolean isFixAspectRatio() {
        return mFixAspectRatio;
    }

    /**
     * 设置长宽比是否固定; true固定长宽比， 而false允许对其进行更改。
     */
    public void setFixedAspectRatio(boolean fixAspectRatio) {
        if (mFixAspectRatio != fixAspectRatio) {
            mFixAspectRatio = fixAspectRatio;
            if (initializedCropWindow) {
                initCropWindow();
                SimpleLog.i("CropOverlayView", "setFixedAspectRatio()");
                invalidate();
            }
        }
    }

    /**
     * 长宽比的X值;
     */
    public int getAspectRatioX() {
        return mAspectRatioX;
    }

    /**
     * 设置长宽比的X值; 默认是 1.
     */
    public void setAspectRatioX(int aspectRatioX) {
        if (aspectRatioX <= 0) {
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        } else if (mAspectRatioX != aspectRatioX) {
            mAspectRatioX = aspectRatioX;
            mTargetAspectRatio = ((float) mAspectRatioX) / mAspectRatioY;

            if (initializedCropWindow) {
                SimpleLog.i("CropOverlayView", "setAspectRatioX()");
                initCropWindow();
                invalidate();
            }
        }
    }

    /**
     * 长宽比的y值;
     */
    public int getAspectRatioY() {
        return mAspectRatioY;
    }

    /**
     * 设置长宽比的y值; 默认是 1.
     */
    public void setAspectRatioY(int aspectRatioY) {
        if (aspectRatioY <= 0) {
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        } else if (mAspectRatioY != aspectRatioY) {
            mAspectRatioY = aspectRatioY;
            mTargetAspectRatio = ((float) mAspectRatioX) / mAspectRatioY;

            if (initializedCropWindow) {
                SimpleLog.i("CropOverlayView", "setAspectRatioY()");
                initCropWindow();
                invalidate();
            }
        }
    }

    /**
     * An edge of the crop window will snap to the corresponding edge of a
     * specified bounding box when the crop window edge is less than or equal to
     * this distance (in pixels) away from the bounding box edge. (default: 3)
     */
    public void setSnapRadius(float snapRadius) {
        mSnapRadius = snapRadius;
    }

    /**
     * Set multi touch functionality to enabled/disabled.
     */
    public boolean setMultiTouchEnabled(boolean multiTouchEnabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && mMultiTouchEnabled != multiTouchEnabled) {
            mMultiTouchEnabled = multiTouchEnabled;
            if (mMultiTouchEnabled && mScaleDetector == null) {
                mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
            }
            return true;
        }
        return false;
    }

    /**
     * set the max width/height and scale factor of the shown image to original image to scale the limits
     * appropriately.
     */
    public void setCropWindowLimits(float maxWidth, float maxHeight, float scaleFactorWidth, float scaleFactorHeight) {
        mCropWindowHandler.setCropWindowLimits(maxWidth, maxHeight, scaleFactorWidth, scaleFactorHeight);
    }


    /**
     * 设置裁剪窗口初始矩形而不是默认矩形。
     */
    public void setInitialCropWindowRect() {
        if (initializedCropWindow) {
            SimpleLog.i("CropOverlayView", "setInitialCropWindowRect()");
            initCropWindow();
            invalidate();
            callOnCropWindowChanged(false, null);
        }
    }

    /**
     * 设置所有初始值，但不调用initCropWindow()来重置视图
     * 在初始化属性时使用一次。
     */
    public void setInitialAttributeValues(CropImageOptions options) {

        this.options = options;

        mCropWindowHandler.setInitialAttributeValues(options);

        setCropShape(options.cropShape);

        setSnapRadius(options.snapRadius);

        setGuidelines(options.guidelines);

        setFixedAspectRatio(options.fixAspectRatio);

        setAspectRatioX(options.aspectRatioX);

        setAspectRatioY(options.aspectRatioY);

        setMultiTouchEnabled(options.multiTouchEnabled);

        mTouchRadius = options.touchRadius;

        mInitialCropWindowPaddingRatio = options.initialCropWindowPaddingRatio;

        mBorderPaint = getNewPaintOrNull(options.borderLineThickness, options.borderLineColor);

        mBorderCornerOffset = options.borderCornerOffset;
        mBorderCornerLength = options.borderCornerLength;
        mBorderCornerPaint = getNewPaintOrNull(options.borderCornerThickness, options.borderCornerColor);

        mGuidelinePaint = getNewPaintOrNull(options.guidelinesThickness, options.guidelinesColor);

        mBackgroundPaint = getNewPaint(options.backgroundColor);

        mUnactivatedPaint = getNewPaint(options.unactivatedRegionColor);

        // 裁剪框关闭按钮
        delBtnBM = BitmapFactory.decodeResource(getResources(), R.drawable.icon_close);
    }

    /**
     * 设置初始裁剪窗口的大小和位置。 这取决于被裁剪图像的大小和位置。
     */
    public void initCropWindow() {
        float leftLimit = Math.max(BitmapUtils.getRectLeft(mBoundsPoints), 0);
        float topLimit = Math.max(BitmapUtils.getRectTop(mBoundsPoints), 0);
        float rightLimit = Math.min(BitmapUtils.getRectRight(mBoundsPoints), mViewRight);
        float bottomLimit = Math.min(BitmapUtils.getRectBottom(mBoundsPoints), mViewBottom);
        if (rightLimit <= leftLimit || bottomLimit <= topLimit) {
            return;
        }
        // 告诉属性函数裁剪窗口已经初始化
        initializedCropWindow = true;

        if (cropWindowType == CREATE_CROP_CROP_WINDOW) {           // 创建原始的剪切框
            RectF rect = mCropWindowHandler.getRect();
            scaleRect(rect, leftLimit, topLimit);
            fixCropWindowRectByRules(rect);
            mCropWindowHandler.addRect(rect);
        } else if (cropWindowType == CREATE_ENCLOSE_CROP_WINDOW) { // 创建圈图的剪切框
            RectF rect = mCropWindowHandler.getRect();
            mCropWindowHandler.addRect(rect);
        } else if (cropWindowType == CREATE_SPLIT_CROP_WINDOW) {   // 创建拆题的剪切框
            RectF rect = mCropWindowHandler.getRect();
            scaleRect(rect, leftLimit, topLimit);
            fixCropWindowRectByRules(rect);
            mCropWindowHandler.addRect(rect);
        }
        invalidate(); // 刷新界面
    }

    // 对从后台获取的矩形坐标进行等比例放大及位置处理
    private void scaleRect(RectF defRectF, float leftLimit, float topLimit) {
        defRectF.left = defRectF.left / mCropWindowHandler.mScaleFactorWidth + leftLimit;
        defRectF.top = defRectF.top / mCropWindowHandler.mScaleFactorHeight + topLimit;
        defRectF.right = defRectF.right / mCropWindowHandler.mScaleFactorWidth + leftLimit;
        defRectF.bottom = defRectF.bottom / mCropWindowHandler.mScaleFactorHeight + topLimit;
    }

    // 将通过Matrix调整之后的rect还原
    public List<RectF> restoreRectFList(List<RectF> splitCropWindowRects, int width) {
        List<RectF> primitiveRectF = new ArrayList<RectF>();
        float topLimit = Math.max(BitmapUtils.getRectTop(mBoundsPoints), 0);
        for (RectF rectF : splitCropWindowRects) {
            rectF.left = 0f;
            rectF.top = (rectF.top - topLimit) * mCropWindowHandler.mScaleFactorHeight;
            rectF.right = width;
            rectF.bottom = (rectF.bottom - topLimit) * mCropWindowHandler.mScaleFactorHeight;
            primitiveRectF.add(rectF);
            SimpleLog.i(TAG, "primitiveRectF: " + rectF);
        }
        return primitiveRectF;
    }

    /**
     * 修正给定矩形以适应位图矩形，并遵循最小、最大和纵横比规则。
     */
    private void fixCropWindowRectByRules(RectF rect) {
        if (rect.width() < mCropWindowHandler.getMinCropWidth()) {
            float adj = (mCropWindowHandler.getMinCropWidth() - rect.width()) / 2;
            rect.left -= adj;
            rect.right += adj;
        }
        if (rect.height() < mCropWindowHandler.getMinCropHeight()) {
            float adj = (mCropWindowHandler.getMinCropHeight() - rect.height()) / 2;
            rect.top -= adj;
            rect.bottom += adj;
        }
        if (rect.width() > mCropWindowHandler.getMaxCropWidth()) {
            float adj = (rect.width() - mCropWindowHandler.getMaxCropWidth()) / 2;
            rect.left += adj;
            rect.right -= adj;
        }
        if (rect.height() > mCropWindowHandler.getMaxCropHeight()) {
            float adj = (rect.height() - mCropWindowHandler.getMaxCropHeight()) / 2;
            rect.top += adj;
            rect.bottom -= adj;
        }

        calculateBounds(rect);
        if (mCalcBounds.width() > 0 && mCalcBounds.height() > 0) {
            float leftLimit = Math.max(mCalcBounds.left, 0);
            float topLimit = Math.max(mCalcBounds.top, 0);
            float rightLimit = Math.min(mCalcBounds.right, mViewRight);
            float bottomLimit = Math.min(mCalcBounds.bottom, mViewBottom);

            if (rect.left < leftLimit) {
                rect.left = leftLimit;
            }
            if (rect.top < topLimit) {
                rect.top = topLimit;
            }
            if (rect.right > rightLimit) {
                rect.right = rightLimit;
            }
            if (rect.bottom > bottomLimit) {
                rect.bottom = bottomLimit;
            }
        }
        if (mFixAspectRatio && Math.abs(rect.width() - rect.height() * mTargetAspectRatio) > 0.1) {
            if (rect.width() > rect.height() * mTargetAspectRatio) {
                float adj = Math.abs(rect.height() * mTargetAspectRatio - rect.width()) / 2;
                rect.left += adj;
                rect.right -= adj;
            } else {
                float adj = Math.abs(rect.width() / mTargetAspectRatio - rect.height()) / 2;
                rect.top += adj;
                rect.bottom -= adj;
            }
        }
    }

    /**
     * Draw crop overview by drawing background over image not in the cropping area, then borders and guidelines.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        SimpleLog.i("CropOverlayView", "onDraw");
        if (mCropWindowHandler.showGuidelines()) {
            // 决定是否要显示剪切框内的分割线
            if (mGuidelines == Enum.Guidelines.ON) {
                drawGuidelines(canvas);
            } else if (mGuidelines == Enum.Guidelines.ON_TOUCH && mMoveHandler != null) {
                // 只在调整剪切框大小时显示
                drawGuidelines(canvas);
            }
        }

        drawBackground(canvas);
        drawBorders(canvas);
        drawCorners(canvas);
    }

    /**
     * 为裁剪区域绘制半透明背景。
     */
    private void drawBackground(Canvas canvas) {
        RectF rect = mCropWindowHandler.getRect();
        float left = Math.max(BitmapUtils.getRectLeft(mBoundsPoints), 0);
        float top = Math.max(BitmapUtils.getRectTop(mBoundsPoints), 0);
        float right = Math.min(BitmapUtils.getRectRight(mBoundsPoints), mViewRight);
        float bottom = Math.min(BitmapUtils.getRectBottom(mBoundsPoints), mViewBottom);

        if (mCropShape == Enum.CropShape.RECTANGLE) {

           /* if (!isNonStraightAngleRotated() || Build.VERSION.SDK_INT <= 17) {
                canvas.drawRect(left, top, right, rect.top, mBackgroundPaint);
                canvas.drawRect(left, rect.bottom, right, bottom, mBackgroundPaint);
                canvas.drawRect(left, rect.top, rect.left, rect.bottom, mBackgroundPaint);
                canvas.drawRect(rect.right, rect.top, right, rect.bottom, mBackgroundPaint);
            } else {
                mPath.reset();
                mPath.moveTo(mBoundsPoints[0], mBoundsPoints[1]);
                mPath.lineTo(mBoundsPoints[2], mBoundsPoints[3]);
                mPath.lineTo(mBoundsPoints[4], mBoundsPoints[5]);
                mPath.lineTo(mBoundsPoints[6], mBoundsPoints[7]);
                mPath.close();

                canvas.save();
                canvas.clipPath(mPath, Region.Op.INTERSECT);
                canvas.clipRect(rect, Region.Op.XOR);
                canvas.drawRect(left, top, right, bottom, mBackgroundPaint);
                canvas.restore();
//            }*/

            List<RectF> rectFs = mCropWindowHandler.getRects();
            mPath.reset();
            mPath.moveTo(mBoundsPoints[0], mBoundsPoints[1]);
            mPath.lineTo(mBoundsPoints[2], mBoundsPoints[3]);
            mPath.lineTo(mBoundsPoints[4], mBoundsPoints[5]);
            mPath.lineTo(mBoundsPoints[6], mBoundsPoints[7]);
            mPath.close();

            mActivatedPath.reset();
            mUnactivatedPath.reset();

            //激活的裁剪区域
            mActivatedPath.addRect(mCropWindowHandler.getRect(), Path.Direction.CW);

            //未激活的裁剪区域
            for (int i = 0; i < rectFs.size() - 1; i++) {
                mUnactivatedPath.addRect(rectFs.get(i), Path.Direction.CW);
            }

            //未激活裁剪区域 - 激活裁剪区域
            if (rectFs.size() > 1) {
                canvas.save();
                canvas.clipPath(mUnactivatedPath, Region.Op.INTERSECT);
                canvas.clipPath(mActivatedPath, Region.Op.DIFFERENCE);
                canvas.drawRect(left, top, right, bottom, mUnactivatedPaint);
                canvas.restore();
            }

            //全集 - （未激活裁剪区域 与 激活裁剪区域的 并集）
            mUnactivatedPath.addPath(mActivatedPath);
            canvas.save();
            canvas.clipPath(mPath, Region.Op.INTERSECT);
            canvas.clipPath(mUnactivatedPath, Region.Op.DIFFERENCE);
            canvas.drawRect(left, top, right, bottom, mBackgroundPaint);
            canvas.restore();

        } else {
            mPath.reset();
            if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT <= 17 && mCropShape == Enum.CropShape.OVAL) {
                mDrawRect.set(rect.left + 2, rect.top + 2, rect.right - 2, rect.bottom - 2);
            } else {
                mDrawRect.set(rect.left, rect.top, rect.right, rect.bottom);
            }
            mPath.addOval(mDrawRect, Path.Direction.CW);
            canvas.save();
            canvas.clipPath(mPath, Region.Op.XOR);
            canvas.drawRect(left, top, right, bottom, mBackgroundPaint);
            canvas.restore();
        }
    }

    /**
     * 在裁剪区域内画两条垂直和两条水平的线，将其分成9等份。
     */
    private void drawGuidelines(Canvas canvas) {
        if (mGuidelinePaint != null) {
            float sw = mBorderPaint != null ? mBorderPaint.getStrokeWidth() : 0;
            RectF rect = mCropWindowHandler.getRect();
            rect.inset(sw, sw);

            float oneThirdCropWidth = rect.width() / 3;
            float oneThirdCropHeight = rect.height() / 3;

            if (mCropShape == Enum.CropShape.OVAL) {

                float w = rect.width() / 2 - sw;
                float h = rect.height() / 2 - sw;

                // Draw vertical guidelines.
                float x1 = rect.left + oneThirdCropWidth;
                float x2 = rect.right - oneThirdCropWidth;
                float yv = (float) (h * Math.sin(Math.acos((w - oneThirdCropWidth) / w)));
                canvas.drawLine(x1, rect.top + h - yv, x1, rect.bottom - h + yv, mGuidelinePaint);
                canvas.drawLine(x2, rect.top + h - yv, x2, rect.bottom - h + yv, mGuidelinePaint);

                // Draw horizontal guidelines.
                float y1 = rect.top + oneThirdCropHeight;
                float y2 = rect.bottom - oneThirdCropHeight;
                float xv = (float) (w * Math.cos(Math.asin((h - oneThirdCropHeight) / h)));
                canvas.drawLine(rect.left + w - xv, y1, rect.right - w + xv, y1, mGuidelinePaint);
                canvas.drawLine(rect.left + w - xv, y2, rect.right - w + xv, y2, mGuidelinePaint);
            } else {

                // Draw vertical guidelines.
                float x1 = rect.left + oneThirdCropWidth;
                float x2 = rect.right - oneThirdCropWidth;
                canvas.drawLine(x1, rect.top, x1, rect.bottom, mGuidelinePaint);
                canvas.drawLine(x2, rect.top, x2, rect.bottom, mGuidelinePaint);

                // Draw horizontal guidelines.
                float y1 = rect.top + oneThirdCropHeight;
                float y2 = rect.bottom - oneThirdCropHeight;
                canvas.drawLine(rect.left, y1, rect.right, y1, mGuidelinePaint);
                canvas.drawLine(rect.left, y2, rect.right, y2, mGuidelinePaint);
            }
        }
    }

    /**
     * 绘制剪切框边框
     */
    private void drawBorders(Canvas canvas) {
        if (mBorderPaint != null) {

            List<RectF> rectFs = mCropWindowHandler.getRects();
            float w = mBorderPaint.getStrokeWidth();

            int borderColor = options.borderLineColor;
            switch (cropWindowType) {
                case CREATE_SPLIT_CROP_WINDOW:
                    borderColor = Color.parseColor("#FF9900");
                    break;
                case CREATE_ENCLOSE_CROP_WINDOW:
                    borderColor = Color.parseColor("#7ACC52");
                    break;
            }
            // 不同类型的裁剪框具有不同颜色的corner
            mBorderPaint.setColor(borderColor);

            for (RectF rect : rectFs) {
//                SimpleLog.i("CropOverlayView", "left: " + rect.left);
//                SimpleLog.i("CropOverlayView", "top: " + rect.top);
//                SimpleLog.i("CropOverlayView", "right: " + rect.right);
//                SimpleLog.i("CropOverlayView", "bottom: " + rect.bottom);
                rect.inset(w / 2, w / 2);
                if (mCropShape == Enum.CropShape.RECTANGLE) {
                    // 绘制矩形裁剪窗口边框
                    canvas.drawRect(rect, mBorderPaint);
                } else {
                    // 绘制圆形裁剪窗口边框
                    canvas.drawOval(rect, mBorderPaint);
                }

            }
        }
    }


    /**
     * 绘制剪切框的角落。
     */
    private void drawCorners(Canvas canvas) {
        if (mBorderCornerPaint != null) {

            float lineWidth = mBorderPaint != null ? mBorderPaint.getStrokeWidth() : 0;
            float cornerWidth = mBorderCornerPaint.getStrokeWidth();
            float w = cornerWidth / 2 + mBorderCornerOffset;
            RectF rect = mCropWindowHandler.getRect();
            if (rect.width() == 0 || rect.height() == 0) return;

//          rect.inset(-w, -w);
            float cornerOffset = (cornerWidth - lineWidth) / 2;
            float cornerExtension = cornerWidth / 2 + cornerOffset;

            float middleLineRadius = 20;  //中线长度的一半
            float radius = 10;

            float middleLineCenterX = (rect.left + rect.right) / 2;
            float middleLineCenterY = (rect.top + rect.bottom) / 2;

            mBorderCornerPaint.setStyle(Paint.Style.FILL);

            int cornerColor = options.borderCornerColor;
            switch (cropWindowType) {
                case CREATE_SPLIT_CROP_WINDOW:
                    cornerColor = Color.parseColor("#FF9900");
                    break;
                case CREATE_ENCLOSE_CROP_WINDOW:
                    cornerColor = Color.parseColor("#7ACC52");
                    break;
            }
            // 不同类型的裁剪框具有不同颜色的corner
            mBorderCornerPaint.setColor(cornerColor);

            // 线条型边角
            if (cropWindowType == CREATE_CROP_CROP_WINDOW) {
                // Top left
                canvas.drawLine(rect.left - cornerOffset, rect.top - cornerExtension, rect.left - cornerOffset, rect.top + mBorderCornerLength, mBorderCornerPaint);
                canvas.drawLine(rect.left - cornerExtension, rect.top - cornerOffset, rect.left + mBorderCornerLength, rect.top - cornerOffset, mBorderCornerPaint);

                // Top right
                canvas.drawLine(rect.right + cornerOffset, rect.top - cornerExtension, rect.right + cornerOffset, rect.top + mBorderCornerLength, mBorderCornerPaint);
                canvas.drawLine(rect.right + cornerExtension, rect.top - cornerOffset, rect.right - mBorderCornerLength, rect.top - cornerOffset, mBorderCornerPaint);

                // Bottom left
                canvas.drawLine(rect.left - cornerOffset, rect.bottom + cornerExtension, rect.left - cornerOffset, rect.bottom - mBorderCornerLength, mBorderCornerPaint);
                canvas.drawLine(rect.left - cornerExtension, rect.bottom + cornerOffset, rect.left + mBorderCornerLength, rect.bottom + cornerOffset, mBorderCornerPaint);

                // Bottom right
                canvas.drawLine(rect.right + cornerOffset, rect.bottom + cornerExtension, rect.right + cornerOffset, rect.bottom - mBorderCornerLength, mBorderCornerPaint);
                canvas.drawLine(rect.right + cornerExtension, rect.bottom + cornerOffset, rect.right - mBorderCornerLength, rect.bottom + cornerOffset, mBorderCornerPaint);

                // Top middle
                canvas.drawLine((middleLineCenterX - middleLineRadius), rect.top - cornerExtension / 2, (middleLineCenterX + middleLineRadius), rect.top - cornerExtension / 2, mBorderCornerPaint);
                // Right middle
                canvas.drawLine(rect.right + cornerExtension / 2, (middleLineCenterY - middleLineRadius), rect.right + cornerExtension / 2, (middleLineCenterY + middleLineRadius), mBorderCornerPaint);
                // Left middle
                canvas.drawLine(rect.left - cornerExtension / 2, (middleLineCenterY - middleLineRadius), rect.left - cornerExtension / 2, (middleLineCenterY + middleLineRadius), mBorderCornerPaint);
                // Bottom middle
                canvas.drawLine((middleLineCenterX - middleLineRadius), rect.bottom + cornerExtension / 2, (middleLineCenterX + middleLineRadius), rect.bottom + cornerExtension / 2, mBorderCornerPaint);
            } else {
                // 圆形边角
                canvas.drawCircle(rect.left, rect.top, radius, mBorderCornerPaint);
                canvas.drawBitmap(delBtnBM, rect.right - delBtnBM.getWidth() / 2, rect.top - delBtnBM.getHeight() / 2, mBorderCornerPaint);
                canvas.drawCircle(rect.right, rect.bottom, radius, mBorderCornerPaint);
                canvas.drawCircle(rect.left, rect.bottom, radius, mBorderCornerPaint);

                canvas.drawCircle(middleLineCenterX, rect.top, radius, mBorderCornerPaint);
                canvas.drawCircle(middleLineCenterX, rect.bottom, radius, mBorderCornerPaint);

                canvas.drawCircle(rect.left, middleLineCenterY, radius, mBorderCornerPaint);
                canvas.drawCircle(rect.right, middleLineCenterY, radius, mBorderCornerPaint);

            }

        }

    }

    /**
     * 创建绘图的绘图Paint
     */
    private static Paint getNewPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        return paint;
    }

    /**
     * 为给定的厚度和颜色创建Paint对象,如果厚度< 0返回null
     */
    private static Paint getNewPaintOrNull(float thickness, int color) {
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

    // 手指按下时的X轴的位置
    private float startX;
    // 手指按下时的Y轴的位置
    private float startY;
    // 手指抬起时的X轴的位置
    private float curX;
    // 手指抬起时的Y轴的位置
    private float curY;
    // 判定需要创建剪切框的滑动距离
    private float moveThreshold = 100;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        getParent().requestDisallowInterceptTouchEvent(true);
//        SimpleLog.e(TAG, "event: " + event.getAction());
//        SimpleLog.e(TAG, "onTouch CropOverlayView: " + this.toString());
        // 如果未启用此视图，则不允许触摸交互。
        if (isEnabled() && allowSlideRect) {
            if (mMultiTouchEnabled) {
                mScaleDetector.onTouchEvent(event);
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    startY = event.getY();
                    onActionDown(event.getX(), event.getY());
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
//                    getParent().requestDisallowInterceptTouchEvent(false);
                    onActionUp();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    curX = event.getX();
                    curY = event.getY();
                    if (cropWindowType != CREATE_CROP_CROP_WINDOW &&  // 用于裁剪的CropOverlayView不能创建新裁剪框
                            mMoveHandler == null &&
                            (Math.abs(curX - startX) > moveThreshold || Math.abs(curY - startY) > moveThreshold)) {
                        // 计算裁剪边界
                        calculateBounds(mCropWindowHandler.getRect());
                        // 裁剪边界内才允许创建裁剪框
                        if (mCalcBounds.contains(startX, startY)) {
                            mCropWindowHandler.addRect(new RectF(startX, startY, curX, curY));
                            CropWindowMoveHandler.Type type = getTypeByDirection(startX, startY, curX, curY);
//                            SimpleLog.d(TAG, "type = "+type+ String.format(" sx= %f, sy=%f, cx=%f, cy=%f", startX, startY, curX, curY));
                            mMoveHandler = new CropWindowMoveHandler(type, mCropWindowHandler, curX, curY);
                        }
                    }
                    onActionMove(event.getX(), event.getY());
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    private CropWindowMoveHandler.Type getTypeByDirection(float startX, float startY, float curX, float curY) {
        CropWindowMoveHandler.Type type = null;
        if (curX > startX && curY > startY) {
            type = CropWindowMoveHandler.Type.BOTTOM_RIGHT;
        } else if (curX > startX && curY < startY) {
            type = CropWindowMoveHandler.Type.TOP_RIGHT;
        } else if (curX < startX && curY < startY) {
            type = CropWindowMoveHandler.Type.TOP_LEFT;
        } else {
            type = CropWindowMoveHandler.Type.BOTTOM_LEFT;
        }
        return type;
    }

    /**
     * On press down start crop window movment depending on the location of the press.<br>
     * if press is far from crop window then no move handler is returned (null).
     */
    private void onActionDown(float x, float y) {

        CropWindowMoveHandler.Type delPosi = CropWindowMoveHandler.Type.TOP_RIGHT;  //默认点击右上角进行删除
        if (cropWindowType == CREATE_CROP_CROP_WINDOW) {
            delPosi = CropWindowMoveHandler.Type.DELETE;  //设置 Type.DELETE 表示不设置可点击删除的位置
        }
        mMoveHandler = mCropWindowHandler.getMoveHandler(x, y, mTouchRadius, mCropShape, delPosi);
        if (mMoveHandler != null) {
            invalidate();
        }
    }

    /**
     * 拖动剪切框完成
     */
    private void onActionUp() {
        if (mMoveHandler != null) {
            mMoveHandler = null;
            callOnCropWindowChanged(false, mCropWindowHandler.getRect());
            invalidate();
        }
    }

    /**
     * Handle move of crop window using the move handler created in {@link #onActionDown(float, float)}.<br>
     * The move handler will do the proper move/resize of the crop window.
     */
    private void onActionMove(float x, float y) {
        if (mMoveHandler != null) {
            float snapRadius = mSnapRadius;
            RectF rect = mCropWindowHandler.getRect();

            if (calculateBounds(rect)) {
                snapRadius = 0;
            }

            mMoveHandler.move(rect, x, y, mCalcBounds, mViewRight, mViewBottom, snapRadius, mFixAspectRatio, mTargetAspectRatio);
            mCropWindowHandler.setRect(rect);
            callOnCropWindowChanged(true, null);
            invalidate();
        }
    }

    /**
     * 计算当前裁剪窗口的边框, 处理非直旋转角度。
     * 如果旋转角度是直的那么剪切框矩形就是位图矩形
     * otherwsie we find the max rectangle that is within the image bounds starting from the crop window rectangle.
     *
     * @param rect the crop window rectangle to start finsing bounded rectangle from
     * @return true - non straight rotation in place, false - otherwise.
     */
    private boolean calculateBounds(RectF rect) {

        float left = BitmapUtils.getRectLeft(mBoundsPoints);
        float top = BitmapUtils.getRectTop(mBoundsPoints);
        float right = BitmapUtils.getRectRight(mBoundsPoints);
        float bottom = BitmapUtils.getRectBottom(mBoundsPoints);

        if (!isNonStraightAngleRotated()) {
            mCalcBounds.set(left, top, right, bottom);
            return false;
        } else {
            float x0 = mBoundsPoints[0];
            float y0 = mBoundsPoints[1];
            float x2 = mBoundsPoints[4];
            float y2 = mBoundsPoints[5];
            float x3 = mBoundsPoints[6];
            float y3 = mBoundsPoints[7];

            if (mBoundsPoints[7] < mBoundsPoints[1]) {
                if (mBoundsPoints[1] < mBoundsPoints[3]) {
                    x0 = mBoundsPoints[6];
                    y0 = mBoundsPoints[7];
                    x2 = mBoundsPoints[2];
                    y2 = mBoundsPoints[3];
                    x3 = mBoundsPoints[4];
                    y3 = mBoundsPoints[5];
                } else {
                    x0 = mBoundsPoints[4];
                    y0 = mBoundsPoints[5];
                    x2 = mBoundsPoints[0];
                    y2 = mBoundsPoints[1];
                    x3 = mBoundsPoints[2];
                    y3 = mBoundsPoints[3];
                }
            } else if (mBoundsPoints[1] > mBoundsPoints[3]) {
                x0 = mBoundsPoints[2];
                y0 = mBoundsPoints[3];
                x2 = mBoundsPoints[6];
                y2 = mBoundsPoints[7];
                x3 = mBoundsPoints[0];
                y3 = mBoundsPoints[1];
            }

            float a0 = (y3 - y0) / (x3 - x0);
            float a1 = -1f / a0;
            float b0 = y0 - a0 * x0;
            float b1 = y0 - a1 * x0;
            float b2 = y2 - a0 * x2;
            float b3 = y2 - a1 * x2;

            float c0 = (rect.centerY() - rect.top) / (rect.centerX() - rect.left);
            float c1 = -c0;
            float d0 = rect.top - c0 * rect.left;
            float d1 = rect.top - c1 * rect.right;

            left = Math.max(left, (d0 - b0) / (a0 - c0) < rect.right ? (d0 - b0) / (a0 - c0) : left);
            left = Math.max(left, (d0 - b1) / (a1 - c0) < rect.right ? (d0 - b1) / (a1 - c0) : left);
            left = Math.max(left, (d1 - b3) / (a1 - c1) < rect.right ? (d1 - b3) / (a1 - c1) : left);
            right = Math.min(right, (d1 - b1) / (a1 - c1) > rect.left ? (d1 - b1) / (a1 - c1) : right);
            right = Math.min(right, (d1 - b2) / (a0 - c1) > rect.left ? (d1 - b2) / (a0 - c1) : right);
            right = Math.min(right, (d0 - b2) / (a0 - c0) > rect.left ? (d0 - b2) / (a0 - c0) : right);

            top = Math.max(top, Math.max(a0 * left + b0, a1 * right + b1));
            bottom = Math.min(bottom, Math.min(a1 * left + b3, a0 * right + b2));

            mCalcBounds.left = left;
            mCalcBounds.top = top;
            mCalcBounds.right = right;
            mCalcBounds.bottom = bottom;
            return true;
        }
    }

    /**
     * Is the cropping image has been rotated by NOT 0,90,180 or 270 degrees.
     */
    private boolean isNonStraightAngleRotated() {
        return mBoundsPoints[0] != mBoundsPoints[6] && mBoundsPoints[1] != mBoundsPoints[7];
    }

    /**
     * Invoke on crop change listener safe, don't let the app crash on exception.
     */
    private void callOnCropWindowChanged(boolean inProgress, RectF rectF) {
        try {
            if (mCropWindowChangeListener != null) {
                mCropWindowChangeListener.onCropWindowChanged(inProgress, rectF);
            }
        } catch (Exception e) {
            SimpleLog.e("AIC", "Exception in crop window changed: " + e);
        }
    }

    /**
     * 当裁剪窗口矩形发生变化时调用回调的接口定义。
     */
    public interface CropWindowChangeListener {
        /**
         * 在裁剪窗口矩形更改后调用
         *
         * @param inProgress 剪切框是否正在变化中
         */
        void onCropWindowChanged(boolean inProgress, RectF rectF);
    }

    /**
     * 处理基于两个手指输入的矩形缩放
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public boolean onScale(ScaleGestureDetector detector) {
            RectF rect = mCropWindowHandler.getRect();

            float x = detector.getFocusX();
            float y = detector.getFocusY();
            float dY = detector.getCurrentSpanY() / 2;
            float dX = detector.getCurrentSpanX() / 2;

            float newTop = y - dY;
            float newLeft = x - dX;
            float newRight = x + dX;
            float newBottom = y + dY;

            if (newLeft < newRight &&
                    newTop <= newBottom &&
                    newLeft >= 0 &&
                    newRight <= mCropWindowHandler.getMaxCropWidth() &&
                    newTop >= 0 &&
                    newBottom <= mCropWindowHandler.getMaxCropHeight()) {

                rect.set(newLeft, newTop, newRight, newBottom);
                mCropWindowHandler.setRect(rect);
                invalidate();
            }

            return true;
        }
    }
}