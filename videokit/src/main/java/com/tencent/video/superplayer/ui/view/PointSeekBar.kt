package com.tencent.video.superplayer.ui.view

import android.content.*
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.tencent.liteav.superplayer.*
import com.tencent.video.superplayer.ui.view.PointSeekBar.*

/**
 * 一个带有打点的，模仿seekbar的view
 * 1、添加打点信息[.addPoint]
 *
 * 2、自定义thumb[TCThumbView]
 *
 * 3、打点view[TCPointView]
 */
class PointSeekBar : FrameLayout {
    private var mWidth = 0// 自身宽度
    private var mHeight = 0// 自身高度
    private var mSeekBarLeft = 0 // SeekBar的起点位置
    private var mSeekBarRight = 0 // SeekBar的终点位置
    private var mBgTop = 0 // 进度条距离父布局上边界的距离
    private var mBgBottom = 0// 进度条距离父布局下边界的距离
    private var mRoundSize = 0 // 进度条圆角大小
    private var mViewEnd = 0 // 自身的右边界

    private var mNormalPaint: Paint? = null// seekbar背景画笔
    private var mProgressPaint: Paint? = null// seekbar进度条画笔
    private var mPointerPaint: Paint? = null// 打点view画笔
    private var mThumbDrawable: Drawable? = null// 拖动块图片
    private var mHalfDrawableWidth = 0// Thumb图片宽度的一半

    // Thumb距父布局中的位置
    private var mThumbLeft = 0f// thumb的marginLeft值
    private var mThumbRight = 0f// thumb的marginRight值
    private var mThumbTop = 0f// thumb的marginTop值
    private var mThumbBottom = 0f// thumb的marginBottom值

    private var mCurrentLeftOffset = 0f // thumb距离打点view的偏移量
    private var mLastX = 0f// 上一次点击事件的横坐标，用于计算偏移量
    private var mCurrentProgress = 0 // 当前seekbar的数值
    var mIsOnDrag = false// 是否处于拖动状态
        private set
    var max = 100 // seekbar最大数值
    private var mBarHeightPx = 0f // seekbar的高度大小 px
    private var mThumbView: TCThumbView? = null// 滑动ThumbView
    private var mPointList: List<PointParams>? = null// 打点信息的列表
    private var mPointClickListener: OnSeekBarPointClickListener? = null// 打点view点击回调
    private var mIsChangePointViews = false // 打点信息是否更新过

    private val rectF = RectF()
    private val pRecf = RectF()

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    /**
     * 设置seekbar进度值
     *
     */
    var progress: Int
        get() = mCurrentProgress
        set(progress) {
            var progressTemp = progress
            if (progressTemp < 0) {
                progressTemp = 0
            }
            if (progressTemp > max) {
                progressTemp = max
            }
            if (!mIsOnDrag) {
                mCurrentProgress = progressTemp
                invalidate()
                callbackProgressInternal(progressTemp, false)
            }
        }

    private fun init(attrs: AttributeSet?) {
        setWillNotDraw(false)
        var progressColor =
            ContextCompat.getColor(context, R.color.superplayer_default_progress_color)
        var backgroundColor =
            ContextCompat.getColor(context, R.color.superplayer_default_progress_background_color)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.PointSeekBarStyleable)
            mThumbDrawable =
                a.getDrawable(R.styleable.PointSeekBarStyleable_psb_thumbBackground)
            mHalfDrawableWidth = mThumbDrawable!!.intrinsicWidth / 2
            progressColor =
                a.getColor(R.styleable.PointSeekBarStyleable_psb_progressColor, progressColor)
            backgroundColor = a.getColor(
                R.styleable.PointSeekBarStyleable_psb_backgroundColor,
                backgroundColor
            )
            mCurrentProgress = a.getInt(R.styleable.PointSeekBarStyleable_psb_progress, 0)
            max = a.getInt(R.styleable.PointSeekBarStyleable_psb_max, 100)
            mBarHeightPx =
                a.getDimension(R.styleable.PointSeekBarStyleable_psb_progressHeight, 40f)
            a.recycle()
        }
        mNormalPaint = Paint()
        mNormalPaint!!.color = backgroundColor
        mPointerPaint = Paint()
        mPointerPaint!!.color = Color.RED
        mProgressPaint = Paint()
        mProgressPaint!!.color = progressColor
        post { addThumbView() }
    }

    private fun changeThumbPos() {
        if (mThumbView == null) return
        val params = mThumbView?.layoutParams as LayoutParams
        params.leftMargin = mThumbLeft.toInt()
        params.topMargin = mThumbTop.toInt()
        mThumbView?.layoutParams = params
    }

    private fun addThumbView() {
        mThumbView = TCThumbView(context, mThumbDrawable)
        val thumbParams =
            LayoutParams(mThumbDrawable!!.intrinsicHeight, mThumbDrawable!!.intrinsicHeight)
        mThumbView!!.layoutParams = thumbParams
        addView(mThumbView)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        mSeekBarLeft = mHalfDrawableWidth
        mSeekBarRight = mWidth - mHalfDrawableWidth
        val barPaddingTop = (mHeight - mBarHeightPx) / 2
        mBgTop = barPaddingTop.toInt()
        mBgBottom = ((mHeight - barPaddingTop).toInt())
        mRoundSize = mHeight / 2
        mViewEnd = mWidth
    }

    private fun calProgressDis() {
        val dis = (mSeekBarRight - mSeekBarLeft) * (mCurrentProgress * 1.0f / max)
        mThumbLeft = dis
        mLastX = mThumbLeft
        mCurrentLeftOffset = 0f
        calculatePointerRect()
    }

    private fun addThumbAndPointViews() {
        post {
            if (mIsChangePointViews) {
                removeAllViews()
                if (mPointList != null) {
                    for (i in mPointList!!.indices) {
                        val params = mPointList!![i]
                        addPoint(params, i)
                    }
                }
                addThumbView()
                mIsChangePointViews = false
            }
            if (!mIsOnDrag) {
                calProgressDis()
                changeThumbPos()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //draw  bg

        rectF.left = mSeekBarLeft.toFloat()
        rectF.right = mSeekBarRight.toFloat()
        rectF.top = mBgTop.toFloat()
        rectF.bottom = mBgBottom.toFloat()
        canvas.drawRoundRect(rectF, mRoundSize.toFloat(), mRoundSize.toFloat(), mNormalPaint!!)

        //draw progress

        pRecf.left = mSeekBarLeft.toFloat()
        pRecf.top = mBgTop.toFloat()
        pRecf.right = mThumbRight - mHalfDrawableWidth
        pRecf.bottom = mBgBottom.toFloat()
        canvas.drawRoundRect(
            pRecf,
            mRoundSize.toFloat(), mRoundSize.toFloat(), mProgressPaint!!
        )
        addThumbAndPointViews()
    }

    /**
     * 添加打点view
     *
     * @param pointParams
     * @param index
     */
    fun addPoint(pointParams: PointParams, index: Int) {
        val percent = pointParams.progress * 1.0f / max
        val pointSize = mBgBottom - mBgTop
        val leftMargin = percent * (mSeekBarRight - mSeekBarLeft)
        val rectLeft = ((mThumbDrawable!!.intrinsicWidth - pointSize) / 2).toFloat()
        val rectTop = mBgTop.toFloat()
        val rectBottom = mBgBottom.toFloat()
        val rectRight = rectLeft + pointSize
        val view = TCPointView(context)
        val params = LayoutParams(mThumbDrawable!!.intrinsicWidth, mThumbDrawable!!.intrinsicWidth)
        params.leftMargin = leftMargin.toInt()
        view.setDrawRect(rectLeft, rectTop, rectBottom, rectRight)
        view.layoutParams = params
        view.setColor(pointParams.color)
        view.setOnClickListener {
            if (mPointClickListener != null) {
                mPointClickListener!!.onSeekBarPointClick(view, index)
            }
        }
        addView(view)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        var isHandle = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> isHandle = handleDownEvent(event)
            MotionEvent.ACTION_MOVE -> isHandle = handleMoveEvent(event)
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> isHandle = handleUpEvent(event)
        }
        return isHandle
    }

    private fun handleUpEvent(event: MotionEvent): Boolean {
        if (mIsOnDrag) {
            mIsOnDrag = false
            if (mListener != null) {
                mListener!!.onStopTrackingTouch(this)
            }
            return true
        }
        return false
    }

    private fun handleMoveEvent(event: MotionEvent): Boolean {
        val x = event.x
        if (mIsOnDrag) {
            mCurrentLeftOffset = x - mLastX
            //计算出标尺的Rect
            calculatePointerRect()
            if (mThumbRight - mHalfDrawableWidth <= mSeekBarLeft) {
                mThumbLeft = 0f
                mThumbRight = mThumbLeft + mThumbDrawable!!.intrinsicWidth
            }
            if (mThumbLeft + mHalfDrawableWidth >= mSeekBarRight) {
                mThumbRight = mWidth.toFloat()
                mThumbLeft = (mWidth - mThumbDrawable!!.intrinsicWidth).toFloat()
            }
            changeThumbPos()
            invalidate()
            callbackProgress()
            mLastX = x
            return true
        }
        return false
    }

    private fun callbackProgress() {
        if (mThumbLeft == 0f) {
            callbackProgressInternal(0, true)
        } else if (mThumbRight == mWidth.toFloat()) {
            callbackProgressInternal(max, true)
        } else {
            val pointerMiddle = mThumbLeft + mHalfDrawableWidth
            if (pointerMiddle >= mViewEnd) {
                callbackProgressInternal(max, true)
            } else {
                val percent = pointerMiddle / mViewEnd * 1.0f
                var progress = (percent * max).toInt()
                if (progress > max) {
                    progress = max
                }
                callbackProgressInternal(progress, true)
            }
        }
    }

    private fun callbackProgressInternal(progress: Int, isFromUser: Boolean) {
        mCurrentProgress = progress
        if (mListener != null) {
            mListener!!.onProgressChanged(this, progress, isFromUser)
        }
    }

    private fun handleDownEvent(event: MotionEvent): Boolean {
//        val x = event.x
//        val y = event.y
//        if (x >= mThumbLeft - 100 && x <= mThumbRight + 100) {
        if (mListener != null) mListener!!.onStartTrackingTouch(this)
//            mIsOnDrag = true
//            mLastX = x
//            return true
//        }
        mLastX = mThumbLeft
        mIsOnDrag = true
        handleMoveEvent(event)
        return true
    }

    private fun calculatePointerRect() {
        //draw pointer
        val pointerLeft = getPointerLeft(mCurrentLeftOffset)
        val pointerRight = pointerLeft + mThumbDrawable!!.intrinsicWidth
        mThumbLeft = pointerLeft
        mThumbRight = pointerRight
        mThumbTop = 0f
        mThumbBottom = mHeight.toFloat()
    }

    private fun getPointerLeft(offset: Float): Float {
        return mThumbLeft + offset
    }

    private var mListener: OnSeekBarChangeListener? = null

    fun setOnSeekBarChangeListener(listener: OnSeekBarChangeListener?) {
        mListener = listener
    }

    interface OnSeekBarChangeListener {
        fun onProgressChanged(seekBar: PointSeekBar, progress: Int, fromUser: Boolean)
        fun onStartTrackingTouch(seekBar: PointSeekBar?)
        fun onStopTrackingTouch(seekBar: PointSeekBar)
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    fun setOnPointClickListener(listener: OnSeekBarPointClickListener?) {
        mPointClickListener = listener
    }

    /**
     * 打点view点击回调
     */
    interface OnSeekBarPointClickListener {
        fun onSeekBarPointClick(view: View, pos: Int)
    }

    /**
     * 设置打点信息列表
     *
     * @param pointList
     */
    fun setPointList(pointList: List<PointParams>?) {
        mPointList = pointList
        mIsChangePointViews = true
        invalidate()
    }

    /**
     * 打点信息
     */
    class PointParams(progress: Int, color: Int) {
        var progress = 0 // 视频进度值(秒)
        var color = Color.RED // 打点view的颜色

        init {
            this.progress = progress
            this.color = color
        }
    }

    /**
     * 打点view
     */
    private class TCPointView : View {
        private var mColor = Color.WHITE // view颜色
        private var mPaint // 画笔
                : Paint? = null
        private var mRectF // 打点view的位置信息(矩形)
                : RectF? = null

        constructor(context: Context?) : super(context) {
            init()
        }

        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        ) {
            init()
        }

        private fun init() {
            mPaint = Paint()
            mPaint!!.isAntiAlias = true
            mPaint!!.color = mColor
            mRectF = RectF()
        }

        /**
         * 设置打点颜色
         *
         * @param color
         */
        fun setColor(color: Int) {
            mColor = color
            mPaint!!.color = mColor
        }

        /**
         * 设置打点view的位置信息
         *
         * @param left
         * @param top
         * @param right
         * @param bottom
         */
        fun setDrawRect(left: Float, top: Float, right: Float, bottom: Float) {
            mRectF!!.left = left
            mRectF!!.top = top
            mRectF!!.right = right
            mRectF!!.bottom = bottom
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRect(mRectF!!, mPaint!!)
        }
    }

    /**
     * 拖动块view
     */
    private class TCThumbView(
        context: Context?, // thumb图片
        private val mThumbDrawable: Drawable?
    ) : View(context) {
        private val mPaint: Paint = Paint()
        private val mRect = Rect()

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            mRect.left = 0
            mRect.top = 0
            mRect.right = w
            mRect.bottom = h
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            mThumbDrawable!!.bounds = mRect
            mThumbDrawable.draw(canvas)
        }

        init {
            mPaint.isAntiAlias = true
        }
    }

    interface PointDrag {
        fun isDrag(): Boolean
    }
}