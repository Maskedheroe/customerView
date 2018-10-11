package com.example.hou.tmepView

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.example.hou.testcustomerview.R

class TView@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = -1) : View(context, attrs, defStyleAttr) {

    private var mPaint: Paint? = null
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    /**
     * 设置温度的最大范围
     */
    /***
     * 设置最大的温度值
     * @param maxCount
     */
    var maxCount = 100f
    /**
     * 设置当前温度
     */
    /***
     * 设置当前的温度
     * @param currentCount
     */
    var currentCount = 20f
        set(currentCount) {
            var currentCount = currentCount
            if (currentCount > maxCount) {
                field = maxCount - 5
            } else if (currentCount < 0f) {
                currentCount = 0f + 5
            } else {
                field = currentCount
            }
            invalidate()
        }
    private var mContext: Context? = null

    private var selction: Float = 0.toFloat()
    private var textPaint: Paint? = null
    private var path: Path? = null
    private var paint: Paint? = null
    /**
     * 指针的宽高
     */
    private var mDefaultIndicatorWidth = dipToPx(10)
    private var mDefaultIndicatorHeight = dipToPx(8)
    /**
     * 圆角矩形的高度
     */
    private var mDefaultTempHeight = dipToPx(20)
    private var mDefaultTextSize = 30
    private val textSpace = dipToPx(5)
    private var rectProgressBg: RectF? = null
    private var shader: LinearGradient? = null

    init {

        initView(context)

    }

    private fun initView(context: Context) {
        this.mContext = context
        //圆角矩形paint
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        //文本paint
        textPaint = TextPaint()
        textPaint!!.isAntiAlias = true
        textPaint!!.textSize = mDefaultTextSize.toFloat()
        textPaint!!.textAlign = Paint.Align.CENTER
        textPaint!!.color = mContext!!.resources.getColor(R.color.colorPrimary)
        //三角形指针paint
        path = Path()
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //确定圆角矩形的范围,在TmepView的最底部,top位置为总高度-圆角矩形的高度
        rectProgressBg = RectF(0f, (mHeight - mDefaultTempHeight).toFloat(), mWidth.toFloat(), mHeight.toFloat())
        shader = LinearGradient(0f, (mHeight - mDefaultTempHeight).toFloat(), mWidth.toFloat(), mHeight.toFloat(), SECTION_COLORS, null, Shader.TileMode.MIRROR)
        mPaint!!.shader = shader
        //绘制圆角矩形 mDefaultTempHeight / 2确定圆角的圆心位置
        canvas.drawRoundRect(rectProgressBg!!, (mDefaultTempHeight / 2).toFloat(), (mDefaultTempHeight / 2).toFloat(), mPaint!!)
        //当前位置占比
        selction = this.currentCount / maxCount
        //绘制指针 指针的位置在当前温度的位置 也就是三角形的顶点落在当前温度的位置

        //定义三角形的左边点的坐标 x= tempView的宽度*当前位置占比-三角形的宽度/2  y=tempView的高度-圆角矩形的高度
        path!!.moveTo(mWidth * selction - mDefaultIndicatorWidth / 2, (mHeight - mDefaultTempHeight).toFloat())
        //定义三角形的右边点的坐标 = tempView的宽度*当前位置占比+三角形的宽度/2  y=tempView的高度-圆角矩形的高度
        path!!.lineTo(mWidth * selction + mDefaultIndicatorWidth / 2, (mHeight - mDefaultTempHeight).toFloat())
        //定义三角形的左边点的坐标 x= tempView的宽度*当前位置占比  y=tempView的高度-圆角矩形的高度-三角形的高度
        path!!.lineTo(mWidth * selction, (mHeight - mDefaultTempHeight - mDefaultIndicatorHeight).toFloat())
        path!!.close()
        paint!!.shader = shader
        canvas.drawPath(path!!, paint!!)
        //绘制文本
        val text = this.currentCount.toInt().toString() + "°c"
        //确定文本的位置 x=tempViwe的宽度*当前位置占比 y=tempView的高度-圆角矩形的高度-三角形的高度-文本的间隙
        canvas.drawText(text, mWidth * selction, (mHeight - mDefaultTempHeight - mDefaultIndicatorHeight - textSpace).toFloat(), textPaint!!)

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)
        if (widthSpecMode == View.MeasureSpec.EXACTLY || widthSpecMode == View.MeasureSpec.AT_MOST) {
            mWidth = widthSpecSize
        } else {
            mWidth = 0
        }
        //主要确定view的整体高度,渐变长条的高度+指针的高度+文本的高度+文本与指针的间隙
        if (heightSpecMode == View.MeasureSpec.AT_MOST || heightSpecMode == View.MeasureSpec.UNSPECIFIED) {
            mHeight = mDefaultTextSize + mDefaultTempHeight + mDefaultIndicatorHeight + textSpace
        } else {
            mHeight = heightSpecSize
        }
        setMeasuredDimension(mWidth, mHeight)
    }


    private fun dipToPx(dip: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dip * scale + 0.5f * if (dip >= 0) 1 else -1).toInt()
    }

    /**
     * 设置温度指针的大小
     *
     * @param width
     * @param height
     */
    fun setIndicatorSize(width: Int, height: Int) {

        this.mDefaultIndicatorWidth = width
        this.mDefaultIndicatorHeight = height
    }

    fun setTempHeight(height: Int) {
        this.mDefaultTempHeight = height
    }

    fun setTextSize(textSize: Int) {
        this.mDefaultTextSize = textSize
    }

    companion object {
        /**
         * 分段颜色
         */
        private val SECTION_COLORS = intArrayOf(Color.GREEN, Color.YELLOW, Color.RED)
    }
}