package com.rfid.rfidreader.ui

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView

/**
 * Lightweight pinch-to-zoom ImageView with pan support.
 */
class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ImageView(context, attrs) {

    private val matrixValues = FloatArray(9)
    private val imageMatrixInternal = Matrix()
    private val lastTouch = PointF()
    private var mode = Mode.NONE

    private var saveScale = 1f
    private val minScale = 1f
    private val maxScale = 5f

    private var viewWidth = 0f
    private var viewHeight = 0f
    private var drawableWidth = 0f
    private var drawableHeight = 0f

    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    init {
        scaleType = ScaleType.MATRIX
        imageMatrix = imageMatrixInternal
        isClickable = true
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w.toFloat()
        viewHeight = h.toFloat()
        fitImageToView()
    }

    override fun setImageDrawable(drawableRes: android.graphics.drawable.Drawable?) {
        super.setImageDrawable(drawableRes)
        fitImageToView()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        val current = PointF(event.x, event.y)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouch.set(current)
                mode = Mode.DRAG
            }

            MotionEvent.ACTION_MOVE -> {
                if (mode == Mode.DRAG && saveScale > minScale) {
                    val dx = current.x - lastTouch.x
                    val dy = current.y - lastTouch.y
                    imageMatrixInternal.postTranslate(dx, dy)
                    fixTranslation()
                    imageMatrix = imageMatrixInternal
                    lastTouch.set(current)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    performClick()
                }
                mode = Mode.NONE
            }
        }

        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun fitImageToView() {
        val drawableRes = drawable ?: return
        if (viewWidth <= 0f || viewHeight <= 0f) return

        drawableWidth = drawableRes.intrinsicWidth.toFloat().coerceAtLeast(1f)
        drawableHeight = drawableRes.intrinsicHeight.toFloat().coerceAtLeast(1f)

        imageMatrixInternal.reset()

        val scale = minOf(viewWidth / drawableWidth, viewHeight / drawableHeight)
        val redundantXSpace = (viewWidth - scale * drawableWidth) / 2f
        val redundantYSpace = (viewHeight - scale * drawableHeight) / 2f

        imageMatrixInternal.postScale(scale, scale)
        imageMatrixInternal.postTranslate(redundantXSpace, redundantYSpace)

        saveScale = 1f
        imageMatrix = imageMatrixInternal
    }

    private fun fixTranslation() {
        imageMatrixInternal.getValues(matrixValues)
        val transX = matrixValues[Matrix.MTRANS_X]
        val transY = matrixValues[Matrix.MTRANS_Y]

        val fixX = getFixTranslation(transX, viewWidth, drawableWidth * matrixValues[Matrix.MSCALE_X])
        val fixY = getFixTranslation(transY, viewHeight, drawableHeight * matrixValues[Matrix.MSCALE_Y])

        if (fixX != 0f || fixY != 0f) {
            imageMatrixInternal.postTranslate(fixX, fixY)
        }
    }

    private fun getFixTranslation(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }

        return when {
            trans < minTrans -> minTrans - trans
            trans > maxTrans -> maxTrans - trans
            else -> 0f
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = Mode.ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val originalScale = saveScale
            saveScale *= scaleFactor

            if (saveScale > maxScale) {
                saveScale = maxScale
            } else if (saveScale < minScale) {
                saveScale = minScale
            }

            val effectiveScale = saveScale / originalScale
            imageMatrixInternal.postScale(
                effectiveScale,
                effectiveScale,
                detector.focusX,
                detector.focusY
            )
            fixTranslation()
            imageMatrix = imageMatrixInternal
            return true
        }
    }

    private enum class Mode {
        NONE,
        DRAG,
        ZOOM
    }
}

