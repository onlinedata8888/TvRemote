package com.remote.tvremote

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * A simple touchpad surface:
 *  - single tap        -> onTap()      (mapped to DPAD center / OK)
 *  - long press         -> onLongPress() (mapped to BACK)
 *  - swipe up/down/left/right -> onSwipe(direction)
 */
class TouchpadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onSwipe: ((direction: String) -> Unit)? = null
    var onTap: (() -> Unit)? = null
    var onLongPress: (() -> Unit)? = null

    private val bgPaint = Paint().apply {
        color = resources.getColor(R.color.surface, null)
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        color = 0xFF333333.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onTap?.invoke()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            onLongPress?.invoke()
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            val dx = e2.x - e1.x
            val dy = e2.y - e1.y
            if (abs(dx) < 40 && abs(dy) < 40) return false

            if (abs(dx) > abs(dy)) {
                onSwipe?.invoke(if (dx > 0) "RIGHT" else "LEFT")
            } else {
                onSwipe?.invoke(if (dy > 0) "DOWN" else "UP")
            }
            return true
        }
    })

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val r = 24f
        canvas.drawRoundRect(2f, 2f, width - 2f, height - 2f, r, r, bgPaint)
        canvas.drawRoundRect(2f, 2f, width - 2f, height - 2f, r, r, borderPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
}
