package com.hong.customviewkt

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import kotlin.math.abs
import kotlin.math.ceil

class LineView2 : View, GestureDetector.OnGestureListener {
    private var gesture: GestureDetector? = null
    private var scroller: Scroller? = null

    constructor(ctx: Context) : this(ctx, null, 0)
    constructor(ctx: Context, attrs: AttributeSet?) : this(ctx, attrs, 0)
    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    ) {
        setBackgroundColor(Color.parseColor("#30000000"))
        gesture = GestureDetector(ctx, this)
        scroller = Scroller(ctx)
    }

    //水平滚动距离
    private var scrollXDistance = 0f

    //每个数据点之间的距离 通过一屏展示几个点计算得来
    private var perDataDistance = 0f
    private val xyLineWidth = 5

    //x轴方向绘制点与x轴的值的距离
    private val xyAvailablePos = xyLineWidth.shr(1)

    //    private val xyAvailablePos = 0
    private val xyLineColor = Color.YELLOW
    private val xyValueTextColor = Color.parseColor("#333333")
    private val linePointColor = Color.BLUE
    private val lineColor = Color.BLUE
    private val linePointSize = 24
    private val lineWidth = 6
    private val xyValueTextSize = 30

    //绘制数据区域可用宽度
    private var availableWidth = 0f

    //一屏最多可以绘制多少个点
    private var maxValuePoint = 7
//    private var leftSubIndex = 0
//    private var rightSubIndex = 1

    //实际需要绘制的点
//    private var enableDrawData = mutableListOf<LindData>()

    //y轴顶部最大有效值位置
    private val yValueMaxLimitSpace = 30
    private val pointWidth = 9

    //x轴线以下位置高度
    private val xValueRectTextHeight = 80
    private val yValueRectTextWidth = 80

    //绘制折线图的基准x位置
    private var currentAnchorX = 0f

    //y轴有多少个数字
    private val YValueSize = 8
    private var yValues = mutableListOf<Int>()


    //y轴最大值
    private var maxYValue = 100
    var data = mutableListOf<LineView.LindData>()
        set(value) {
            field = value
            //找出y轴最大值
            var currentValue = 0f
            for (item in data) {
                if (item.value > currentValue) {
                    currentValue = item.value
                }
            }
            maxYValue = if (ceil(currentValue).toInt() > 0) {
                ceil(currentValue).toInt()
            } else {
                maxYValue
            }
            //填充Y轴的值
            val range = ceil(maxYValue / (YValueSize - 1).toFloat()).toInt()
            yValues.clear()
            for (index in range..maxYValue step range) {
                yValues.add(index)
            }
            if (!yValues.contains(maxYValue)) {
                yValues.add(maxYValue)
            }
            invalidate()
        }
    private var xLinePaint = Paint()
    private var xTextPaint = Paint()
    private var yLinePaint = Paint()
    private var yTextPaint = Paint()
    private var testPaint = Paint()
    private var linePaint = Paint()
    private var linePointPaint = Paint()
    private var path = Path()
    private var clipRectF = RectF()

    //往左边加
    fun insertToLeft(data: MutableList<LineView.LindData>) {

    }

    //往右边加
    fun insertToRight(data: MutableList<LineView.LindData>) {

    }

    /**
     * 画数据线
     */
    private fun drawData(canvas: Canvas) {
        linePaint.apply {
            isAntiAlias = true
            color = lineColor
            style = Paint.Style.STROKE
            strokeWidth = lineWidth.toFloat()
        }
        linePointPaint.apply {
            isAntiAlias = true
            color = linePointColor
            style = Paint.Style.FILL
            strokeWidth = linePointSize.toFloat()
        }
        Log.e("sssssssssss", canvas.height.toString())
        canvas.save()
        clipRectF.set(
            yValueRectTextWidth.toFloat() - pointWidth.shr(1),
            yValueMaxLimitSpace.toFloat(),
            measuredWidth.toFloat(),
            measuredHeight - xyAvailablePos - xValueRectTextHeight.toFloat()
        )
        canvas.clipRect(clipRectF)
//        val maxLimit = if (data.size < maxValuePoint) {
//            data.size
//        } else {
//            maxValuePoint
//        }
//        canvas.drawColor(Color.RED)
        if (perDataDistance == 0f) {
            //初始化状态
            perDataDistance = clipRectF.width() / maxValuePoint
            //填充可绘制数据
//            if (data.size <= maxValuePoint * 3) {
//                enableDrawData.addAll(data)
//            } else {
//                leftSubIndex = 0
//                rightSubIndex = maxValuePoint * 3
//                enableDrawData.addAll(data.subList(leftSubIndex, rightSubIndex))
//            }
            var xPos = clipRectF.left + pointWidth
            for (item in data) {
                item.xPos = xPos
                item.yPos =
                    clipRectF.height() - item.value * yRate + clipRectF.top + yValueMaxLimitSpace
//                item.yPos = clipRectF.top
                xPos += perDataDistance
            }
        }
        //处理拖动
        //滑动了多少个点
//        val scrollPosNumber = ceil(abs(scrollXDistance) / perDataDistance)
//        Log.e("scrollPosNumber","${scrollPosNumber}")

        if (scrollXDistance != 0f) {
            //右滑动
            if (scrollXDistance > 0) {
//                val willChangeIndex = leftSubIndex - scrollPosNumber
            } else {
                //左滑动
//                val willChangeIndex = rightSubIndex + scrollPosNumber
//                if (data.size >= willChangeIndex) {
//                    fillRightPos(scrollPosNumber.toInt(), willChangeIndex.toInt())
//                } else {
//                    //一次滚动数量超过极限
//                    val default = rightSubIndex + maxValuePoint * 3
//                    if (data.size >= default) {
//                        fillRightPos(maxValuePoint * 3, default)
//                    } else {
//                        val available = data.size - rightSubIndex
//                        if (available > 0) {
//                            fillRightPos(available, data.size)
//                        }
//                    }
//                }
                for (item in data) {
                    item.xPos = item.xPos - abs(scrollXDistance)
                }
            }
        }

        Log.e("sssssssssss", canvas.height.toString())
        //开始画点
        drawAllPoint(canvas)
    }

    /**
     * 往右边填充数据
     *
     * @param fillNumber 填充数量
     * @param willChangeIndex 截取索引
     */
//    private fun fillRightPos(fillNumber: Int, willChangeIndex: Int) {
//        if (fillNumber <= 0) {
//            return
//        }
//        if (willChangeIndex <= rightSubIndex) {
//            return
//        }
//        if (rightSubIndex > data.lastIndex) {
//            return
//        }
//        if (willChangeIndex > data.size) {
//            return
//        }
//        for (i in 0 until fillNumber) {
//            enableDrawData.removeAt(0)
//        }
//        for (item in enableDrawData) {
//            item.xPos = item.xPos - abs(scrollXDistance)
//        }
//        val newList = data.subList(rightSubIndex, willChangeIndex)
//        var xPos = enableDrawData[enableDrawData.lastIndex].xPos + perDataDistance
//        for (item in newList) {
//            item.xPos = xPos
//            item.yPos =
//                clipRectF.height() - item.value * yRate + clipRectF.top + xValueRectTextHeight
//            xPos += perDataDistance
//        }
//
//        enableDrawData.addAll(newList)
//        rightSubIndex = willChangeIndex
//    }

    /**
     * 画点和线
     *
     * @param canvas
     */
    private fun drawAllPoint(canvas: Canvas) {
        path.reset()
//        if (enableDrawData.isNotEmpty()) {
//            path.moveTo(enableDrawData[0].xPos, enableDrawData[0].yPos)
//        }
        for (item in data) {
            canvas.drawCircle(item.xPos, item.yPos, pointWidth.toFloat(), linePointPaint)
            path.lineTo(item.xPos, item.yPos)
        }
        canvas.drawPath(path, linePaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        maxValuePoint =
//            ceil(measuredWidth.toFloat() - yValueRectTextWidth.toFloat() / perDataDistance).toInt()
        xLinePaint.apply {
            isAntiAlias = true
            color = xyLineColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = xyLineWidth.toFloat()
        }
        testPaint.apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = xyLineWidth.toFloat()
        }
        xTextPaint.apply {
            isAntiAlias = true
            color = xyValueTextColor
            style = Paint.Style.FILL
            textSize = xyValueTextSize.toFloat()
            textAlign = Paint.Align.LEFT
        }
        yTextPaint = xTextPaint
        yLinePaint = xLinePaint
        if (enableYHeight == 0) {
            enableYHeight = measuredHeight - xValueRectTextHeight - yValueMaxLimitSpace
            yRate = enableYHeight / maxYValue.toFloat()
        }
        drawXyLines(canvas)
        drawData(canvas)

    }


    /**
     * 将值转换到坐标中
     *
     * @param value
     * @return
     */
    private fun getRealPos(value: Float): Float {
        val yValidDistance = measuredHeight - xValueRectTextHeight - yValueMaxLimitSpace
        val pos = yValidDistance / maxYValue.toFloat()
        return yValidDistance - value * pos
    }

    //可用的y轴高度
    private var enableYHeight = 0

    //y轴像素换算成实际数值的比例
    private var yRate = 0f

    /**
     * 花xy轴
     */
    private fun drawXyLines(canvas: Canvas) {
        //从y轴正方向往负方向画
        val font = yTextPaint.fontMetrics
        val dy = (font.bottom - font.top) / 2 - font.bottom
        canvas.drawText("0", 0f, enableYHeight + dy + yValueMaxLimitSpace, yTextPaint)
        for (yValue in yValues) {
            val realYPos = enableYHeight - yValue * yRate

            //注意，这里是以y轴值底部为重点开始画的，没有垂直剧中
            canvas.drawText(
                "${yValue}%",
                0f,
                realYPos + dy + yValueMaxLimitSpace + (font.bottom - font.top) / 2,
                yTextPaint
            )
        }
        canvas.drawLine(
            yValueRectTextWidth.toFloat(),
            0f,
            yValueRectTextWidth.toFloat(),
            measuredHeight - xValueRectTextHeight.toFloat(),
            yLinePaint
        )
        canvas.drawLine(
            yValueRectTextWidth.toFloat(),
            measuredHeight - xValueRectTextHeight.toFloat(),
            measuredWidth.toFloat(),
            measuredHeight - xValueRectTextHeight.toFloat(),
            xLinePaint
        )

    }

    //    private var offsetX = 0f
    private var downX = 0f
    private var lastX = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gesture!!.onTouchEvent(event)
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                return true
//            }
//            MotionEvent.ACTION_MOVE -> {
//                scrollXDistance = event.x - lastX
//                invalidate()
//            }
//        }
//        lastX = event.x
//        return true
        //注意处理左右拖动极限
    }


    /**
     * 转换后的数据
     */
//    data class LindData(
//        val time: Long,
//        val value: Float,
//        var xPos: Float = 0f,
//        var yPos: Float = 0f
//    )

    override fun onDown(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {
    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(
        p0: MotionEvent,
        p1: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        scrollXDistance = (-distanceX)
        scroller!!.startScroll(scrollX, 0, distanceX.toInt(), 0)
        invalidate()
        Log.e("onScroll", "${scrollXDistance}")
        Log.e("distanceX", "${-distanceX}")
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        scroller!!.fling(
            p1.x.toInt(),
            p1.y.toInt(),
            p2.toInt(),
            p3.toInt(),
            -Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            -Integer.MAX_VALUE,
            Integer.MAX_VALUE
        )
        Log.e("ssssssssssss", "onFling--${p1.x - p0.x}--velocityX=${p2}---velocityy=${p3}")
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
//        scroller!!.startScroll()
        Log.e("ssssssssssss", "${scroller!!.computeScrollOffset()}")
        if (scroller!!.computeScrollOffset()) {
//            scrollXDistance = (-scroller!!.currX).toFloat()
            invalidate()
        }
//        if (scroller!!.computeScrollOffset()){
//            scro
//        }
    }

}