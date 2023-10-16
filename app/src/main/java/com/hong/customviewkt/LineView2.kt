package com.hong.customviewkt

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import kotlin.math.abs
import kotlin.math.ceil

fun main() {
    for (index in 5 downTo 0) {
        println(index)
    }
}
class LineView2 : View, GestureDetector.OnGestureListener {
    private var gesture: GestureDetector? = null
    private var scroller: Scroller? = null

    //装需要被移除得索引
    private var needRemoveIndex = mutableListOf<LineView.LindData>()

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

    //有边界预留宽度
    private var endSpace = 30

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
    private var pointScale = 10
    private var leftSubIndex = 0//包含这个索引
    private var rightSubIndex = 1//不包含这个索引
    private var minXPos = 0f//可视区域内最小得x值
    private var maxXPos = 0f//可视区域内最大得x值

    //实际需要绘制的点
    private var enableDrawData = mutableListOf<LineView.LindData>()
    private var originalStartX = 0f
    private var originalEndX = 0f

    //滚动右边界红线
    private var diedEndX = 0f
    private var diedStartX = 0f
    private var scrollPosLimit = maxValuePoint * 2

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
    private var totalScrollX = 0f


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
    private var pointValuePaint = Paint()
    private var linePointPaint = Paint()
    private var path = Path()
    private var clipRectF = RectF()

    //往左边加
    private fun insertToLeft() {
        if (data.isEmpty()) {
            return
        }
        if (enableDrawData[0].xPos > diedStartX + scrollPosLimit && data[0].id != enableDrawData[0].id) {
            Log.e("ssssssssss", "开始往左边加数据")
            val number =
                ceil(abs(diedStartX - enableDrawData[0].xPos) / perDataDistance).toInt()
            if (leftSubIndex - number >= 0) {
                //包含
                addEnableDataToEnableData(leftSubIndex - number)
            } else {
                addEnableDataToEnableData(0)
            }
        }
    }

    //往右边加 避免新建对象
    private fun insertToRight() {
        if (data.isEmpty()) {
            return
        }
        if (enableDrawData[enableDrawData.lastIndex].xPos < diedEndX - scrollPosLimit && data.last().id != enableDrawData[enableDrawData.lastIndex].id) {
            Log.e("ssssssssss", "开始往右边加数据")
            val number =
                ceil((diedEndX - enableDrawData[enableDrawData.lastIndex].xPos) / perDataDistance).toInt()
            if (data.size >= number + rightSubIndex) {
                addDataToEnableData(number + rightSubIndex)
            } else {
                addDataToEnableData(data.size)
            }
        }
    }


    /**
     * 反向加数据
     *
     * @param startIndex
     */
    private fun addEnableDataToEnableData(startIndex: Int) {
        for (index in leftSubIndex - 1 downTo startIndex) {
            val newItem = data[index]
            newItem.xPos =
                enableDrawData[0].xPos - perDataDistance
            enableDrawData.add(0, newItem)
            if (newItem.id == 0) {
                Log.e("qqqqqq", newItem.xPos.toString())
            }

        }
        //超过部分移除
        removeRight()
//        for (removeIndex in 0 until (enableDrawData.size - maxValuePoint * 5)) {
//            enableDrawData.removeAt(enableDrawData.lastIndex)
//        }
        originalEndX = enableDrawData[enableDrawData.lastIndex].xPos
        originalStartX = enableDrawData[0].xPos
        leftSubIndex = startIndex
        rightSubIndex = startIndex + enableDrawData.size

    }

    /**
     * 移除enableDrawData中得无效数据
     *
     */
    private fun removeInvalidPoint() {
        //开始移除
        for (item in needRemoveIndex) {
            enableDrawData.remove(item)
        }
    }

    /**
     * 移除左边多余得点
     *
     */
    private fun removeLeft() {
        needRemoveIndex.clear()
        //先检查左边
        for (i in 0 until enableDrawData.lastIndex) {
            val item = enableDrawData[i]
            if (item.xPos < diedStartX) {
                needRemoveIndex.add(item)
            } else {
                break
            }
        }
        removeInvalidPoint()
    }

    /**
     * 移除右边多余得点
     *
     */
    private fun removeRight() {
        needRemoveIndex.clear()
        //再检查右边
        for (i in enableDrawData.lastIndex downTo 0) {
            val item = enableDrawData[i]
            if (item.xPos > diedEndX) {
                needRemoveIndex.add(item)
            } else {
                break
            }
        }
        removeInvalidPoint()
    }

    private fun addDataToEnableData(endIndex: Int) {
        for (index in rightSubIndex until endIndex) {
            val newItem = data[index]
            newItem.xPos =
                enableDrawData[enableDrawData.lastIndex].xPos + perDataDistance
            enableDrawData.add(newItem)
        }
        //超过部分移除
        removeLeft()
//        for (removeIndex in 0 until (enableDrawData.size - maxValuePoint * 5)) {
//            enableDrawData.removeAt(0)
//        }
        originalEndX = enableDrawData[enableDrawData.lastIndex].xPos
        originalStartX = enableDrawData[0].xPos
        rightSubIndex = endIndex
        leftSubIndex = rightSubIndex - enableDrawData.size
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
        pointValuePaint.apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.FILL
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        linePointPaint.apply {
            isAntiAlias = true
            color = linePointColor
            style = Paint.Style.FILL
            strokeWidth = linePointSize.toFloat()
        }
        Log.e("开始绘制", "${scrollXDistance.toString()}--${totalScrollX}---${scrollX}")
        Log.e("sssssssssss", canvas.height.toString())
        canvas.save()
//        第一次裁剪
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
            if (data.size <= maxValuePoint * pointScale) {
                enableDrawData.addAll(data)
            } else {
                leftSubIndex = 0
                rightSubIndex = maxValuePoint * pointScale
                enableDrawData.addAll(data.subList(leftSubIndex, rightSubIndex))
            }
            var xPos = clipRectF.left + pointWidth
            for (item in data) {
                item.xPos = xPos
                item.yPos =
                    clipRectF.height() - item.value * yRate + clipRectF.top + yValueMaxLimitSpace
//                item.yPos = clipRectF.top
                xPos += perDataDistance
            }
            minXPos = if (data.isNotEmpty()) {
                data[0].xPos - (perDataDistance * maxValuePoint * 5)
            } else {
                0f
            }
//            maxXPos = if (data.size > maxValuePoint) {
//                data[maxValuePoint - 1].xPos + (perDataDistance * maxValuePoint * pointScale)
//            } else {
//                0f
//            }
            maxXPos = data[0].xPos + perDataDistance * maxValuePoint * 5
            diedEndX = data[0].xPos + maxValuePoint * perDataDistance * 7
            diedStartX = data[0].xPos - maxValuePoint * perDataDistance * 7
            originalStartX = if (enableDrawData.isNotEmpty()) {
                enableDrawData[0].xPos
            } else {
                0f
            }
            originalEndX = if (enableDrawData.isNotEmpty()) {
                enableDrawData[enableDrawData.lastIndex].xPos
            } else {
                0f
            }
        }

        if (scrollXDistance != 0f) {
            //右滑动
            if (scrollXDistance > 0) {
                if (enableDrawData.isNotEmpty()) {
                    if (enableDrawData[0].id == data[0].id) {
                        //是第一条 处理左边界
                        if (enableDrawData[0].xPos + abs(scrollXDistance) > getFirstPointX()) {
                            scrollXDistance = getFirstPointX() - enableDrawData[0].xPos
                        }
                    }
                }
                for (item in enableDrawData) {
                    item.xPos = item.xPos + abs(scrollXDistance)
                }
                if (enableDrawData.isNotEmpty()) {
                    insertToLeft()
//                    if (enableDrawData[enableDrawData.lastIndex].xPos - originalEndX >= 0) {
//                        //往右滑动 滑动得距离达到了限度，调整可绘制集合
//
//                    } else {
//                        Log.e("ssssssssss", "else分支")
//                        //往左滑动
//                        if (abs(originalEndX - enableDrawData[enableDrawData.lastIndex].xPos) >= perDataDistance * maxValuePoint) {
//                            insertToRight()
//                        }
//                    }

                }
            } else {
                //右边界处理
                val enableLast = enableDrawData.last()
                if (enableDrawData.last().id == data.last().id) {
                    if (enableLast.xPos - abs(scrollXDistance) < measuredWidth - endSpace) {
                        scrollXDistance = enableLast.xPos - measuredWidth + endSpace
                    }
                }
                //左滑动
                for (item in enableDrawData) {
                    item.xPos = item.xPos - abs(scrollXDistance)
                }
                if (enableDrawData.isNotEmpty()) {
                    //往左滑动 滑动得距离达到了限度，调整可绘制集合
                    insertToRight()
                    Log.e("ssssssssss", "一直往左滑动")

                }

            }
        }

        Log.e("sssssssssss", canvas.height.toString())
        //开始画点
        drawAllPoint(canvas)
        //画点上的数据
//        canvas.restore()
        drawPointValue(canvas)


    }

    private fun getFirstPointX(): Float = clipRectF.left + pointWidth

    private fun drawPointValue(canvas: Canvas) {
        val font = pointValuePaint.fontMetrics
        val dy = (font.bottom - font.top) / 2 - font.bottom
        for (item in enableDrawData) {
            canvas.drawText(item.time.toString(), item.xPos, item.yPos - dy - 20, pointValuePaint)
        }
    }


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
        for (item in enableDrawData) {
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
        val res = gesture!!.onTouchEvent(event)
        Log.e("onTouchEvent", "onTouchEvent==${res}")
        return res
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

    override fun onDown(p0: MotionEvent): Boolean {
        Log.e("OnGestureListener", "onDown")
        return true
    }

    override fun onShowPress(p0: MotionEvent) {
        Log.e("OnGestureListener", "onShowPress")

    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        Log.e("OnGestureListener", "onSingleTapUp")
        return false
    }

    override fun onScroll(
        p0: MotionEvent,
        p1: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.e("OnGestureListener", "onScroll")
        if (scroller?.isFinished == false) {
            scroller?.abortAnimation()
        }
        totalScrollX += distanceX
        scrollXDistance = (-distanceX)
//        scroller!!.startScroll(scrollX, 0, distanceX.toInt(), 0)
        invalidate()
        Log.e("onScroll", "${scrollXDistance}")
        Log.e("distanceX", "${-distanceX}")
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
        Log.e("OnGestureListener", "onLongPress")
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        Log.e("OnGestureListener", "onFling")
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
//        scroller!!.startScroll()
        Log.e("ssssssssssss", "onFling==${p1.x}==${p0.x}--velocityX=${p2}---velocityy=${p3}")
        return true
    }

    private var distanceX = Integer.MAX_VALUE
    override fun computeScroll() {
        super.computeScroll()
//        scroller!!.startScroll()
        Log.e("ssssssssssss", "${scroller!!.computeScrollOffset()}")
        if (scroller!!.computeScrollOffset()) {
//            scrollXDistance = (-scroller!!.currX).toFloat()
            if (distanceX == Integer.MAX_VALUE) {
                scrollXDistance = (scroller!!.currX - scroller!!.startX).toFloat()
            } else {
                scrollXDistance = (scroller!!.currX - distanceX).toFloat()

            }
            Log.e("computeScroll", "${scrollXDistance}")
            Log.e("scroller!!.currX", "${scroller!!.currX}")
            distanceX = scroller!!.currX
            invalidate()
        } else {
            distanceX = Integer.MAX_VALUE
        }
//        if (scroller!!.computeScrollOffset()){
//            scro
//        }
    }

}