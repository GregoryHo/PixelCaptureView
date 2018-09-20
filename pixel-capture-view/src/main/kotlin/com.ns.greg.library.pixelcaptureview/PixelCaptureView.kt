package com.ns.greg.library.pixelcaptureview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Paint.Style.STROKE
import android.graphics.Rect
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.ImageView.ScaleType.FIT_XY
import com.ns.greg.library.pixelcaptureview.ResolutionRatio.FOUR_X_THREE
import com.ns.greg.library.pixelcaptureview.internal.CaptureOptions
import com.ns.greg.library.pixelcaptureview.internal.CaptureWindow
import com.ns.greg.library.pixelcaptureview.internal.Corner
import com.ns.greg.library.pixelcaptureview.internal.ResolutionUtils
import java.lang.ref.WeakReference

/**
 * @author gregho
 * @since 2018/9/19
 */
class PixelCaptureView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

  private val gestureDetector: GestureDetectorCompat by lazy {
    GestureDetectorCompat(context, SimpleGestureListener(this))
  }
  private val captureOptions: CaptureOptions
  private val captureWindow: CaptureWindow
  private val borderPaint = Paint()
  private val cornerPaint = Paint()
  private var isMoving = false
  private var listener: CaptureListener? = null

  init {
    captureOptions = if (attrs != null) {
      /* load attributes */
      val attributes =
        context.obtainStyledAttributes(attrs, R.styleable.PixelCaptureView, defStyleAttr, 0)
      with(attributes) {
        /* view resolution ratio */
        val resolutionRatio = ResolutionRatio.fromValue(
            getInt(R.styleable.PixelCaptureView_resolution_ratio, FOUR_X_THREE.value)
        )!!
        /* border */
        val borderLineColor =
          getColor(R.styleable.PixelCaptureView_borderLineColor, Color.WHITE)
        val borderLineThickness =
          getDimension(R.styleable.PixelCaptureView_borderLineThickness, 2f)
        /* corner */
        val cornerPadding = getDimension(R.styleable.PixelCaptureView_cornerPadding, 20f)
        val cornerLineColor =
          getColor(R.styleable.PixelCaptureView_cornerLineColor, Color.WHITE)
        val cornerLineThickness =
          getDimension(R.styleable.PixelCaptureView_borderLineThickness, 2f)
        val cornerWidth = getDimension(R.styleable.PixelCaptureView_cornerWidth, 20f)
        val cornerHeight = getDimension(R.styleable.PixelCaptureView_cornerHeight, 20f)
        val gridCells = getInt(R.styleable.PixelCaptureView_grid_cells, 3)
        recycle()
        CaptureOptions(
            resolutionRatio, borderLineColor, borderLineThickness, cornerPadding, cornerLineColor,
            cornerLineThickness, cornerWidth, cornerHeight, gridCells
        )
      }
    } else {
      CaptureOptions()
    }
    /* capture window */
    captureWindow = CaptureWindow(
        captureOptions.cornerPadding, captureOptions.cornerWidth, captureOptions.cornerHeight
    )
    /* border paint */
    borderPaint.color = captureOptions.borderLineColor
    borderPaint.strokeWidth = captureOptions.borderLineThickness
    borderPaint.style = Style.STROKE
    borderPaint.isAntiAlias = true
    /* corner paint */
    cornerPaint.color = captureOptions.cornerLineColor
    cornerPaint.strokeWidth = captureOptions.cornerLineThickness
    cornerPaint.style = STROKE
    cornerPaint.isAntiAlias = true
    /* scale type */
    scaleType = FIT_XY
  }

  override fun setScaleType(scaleType: ScaleType?) {
    scaleType?.run {
      if (this != FIT_XY) {
        /* always set to FIT_XY */
        return
      }
    }

    super.setScaleType(scaleType)
  }

  override fun onLayout(
    changed: Boolean,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int
  ) {
    super.onLayout(changed, left, top, right, bottom)
    if (changed) {
      /* FIXME: how to determine the ratio layout */
      val width = right - left
      val height = bottom - top
      initRatioLayout(width)
      if ((width > 0) and (height > 0)) {
        captureWindow.initOverlayView(
            left.toFloat(), top.toFloat(), right.toFloat(),
            bottom.toFloat()
        )
        if (!captureWindow.hasBorder()) {
          val cx = (width / 2).toFloat()
          val cy = (height / 2).toFloat()
          captureWindow.initBorder(cx / 2, cy / 2, cx, cy)
        }
      }
    }
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    canvas?.run {
      drawBorder(this)
      drawCorners(this)
      drawGridLines(this)
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent?): Boolean {
    if (!isEnabled or !captureWindow.hasOverlayView()) {
      return false
    }

    event?.run {
      val action = actionMasked
      when (action) {
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
          isMoving = false
          invalidate()
        }
      }
    }

    return gestureDetector.onTouchEvent(event) or super.onTouchEvent(event)
  }

  /*--------------------------------
   * Public functions
   *-------------------------------*/

  fun setBorder(
    x: Int,
    y: Int,
    width: Int,
    height: Int
  ) {
    captureWindow.initBorder(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
  }

  fun setCaptureListener(listener: CaptureListener) {
    this.listener = listener
  }

  fun capture() {
    listener?.run {
      val current = captureWindow.getDrawingRectf()
      val rect = Rect(
          current.left.toInt(), current.top.toInt(), current.right.toInt(), current.bottom.toInt()
      )
      onCapture(width, height, rect)
    }
  }

  /*--------------------------------
   * Private functions
   *-------------------------------*/

  private fun drawBorder(canvas: Canvas) {
    canvas.drawRect(captureWindow.getDrawingRectf(), borderPaint)
  }

  private fun drawCorners(canvas: Canvas) {
    val recft = captureWindow.getDrawingRectf()
    val padding = captureWindow.cornerPadding
    val width = captureWindow.cornerWidth
    val height = captureWindow.cornerHeight
    /* left top */
    val ltX = recft.left + padding
    val ltY = recft.top + padding
    canvas.drawLine(ltX, ltY, ltX + width, ltY, cornerPaint)
    canvas.drawLine(ltX, ltY, ltX, ltY + height, cornerPaint)
    /* right top */
    val rtX = recft.right - padding
    val rtY = recft.top + padding
    canvas.drawLine(rtX, rtY, rtX - width, rtY, cornerPaint)
    canvas.drawLine(rtX, rtY, rtX, rtY + height, cornerPaint)
    /* left bottom */
    val lbX = recft.left + padding
    val lbY = recft.bottom - padding
    canvas.drawLine(lbX, lbY, lbX + width, lbY, cornerPaint)
    canvas.drawLine(lbX, lbY, lbX, lbY - height, cornerPaint)
    /* right bottom */
    val rbX = recft.right - padding
    val rbY = recft.bottom - padding
    canvas.drawLine(rbX, rbY, rbX - width, rbY, cornerPaint)
    canvas.drawLine(rbX, rbY, rbX, rbY - height, cornerPaint)
  }

  private fun drawGridLines(canvas: Canvas) {
    val cells = captureOptions.gridCells
    if (!isMoving || cells == 0) {
      /* return while not moving */
      return
    }

    val current = captureWindow.getDrawingRectf()
    val dw = (current.right - current.left) / cells
    val dh = (current.bottom - current.top) / cells
    for (i in 1 until cells) {
      val x = current.left + (dw * i)
      val y = current.top + (dh * i)
      canvas.drawLine(x, current.top, x, current.bottom, borderPaint)
      canvas.drawLine(current.left, y, current.right, y, borderPaint)
    }
  }

  private fun initRatioLayout(width: Int) {
    val height =
      ResolutionUtils.getRatioHeight(width, captureOptions.resolutionRatio)
    layoutParams.height = height
    requestLayout()
  }

  private class SimpleGestureListener(reference: PixelCaptureView) : SimpleOnGestureListener() {

    private val captureView: PixelCaptureView by lazy {
      WeakReference<PixelCaptureView>(reference).get()!!
    }
    private var downX = 0f
    private var downY = 0f
    private var onCorner: Corner? = null
    private var onWindow = false

    override fun onDown(e: MotionEvent?): Boolean {
      with(captureView) {
        onCorner = null
        onWindow = false
        captureView.isMoving = false
        e?.run {
          downX = e.x
          downY = e.y
          onCorner = captureWindow.isTouchOnCorner(downX, downY)
          return if (onCorner != null) {
            true
          } else {
            onWindow = captureWindow.isTouchOnWindow(downX, downY)
            onWindow
          }
        }
      }

      return false
    }

    override fun onScroll(
      e1: MotionEvent?,
      e2: MotionEvent?,
      distanceX: Float,
      distanceY: Float
    ): Boolean {
      /* multiply -1 to adjusted direction */
      val dx = distanceX * -1
      val dy = distanceY * -1
      with(captureView) {
        onCorner?.let {
          captureWindow.scale(it, dx, dy)
        } ?: run {
          captureWindow.translate(dx, dy)
        }

        isMoving = true
        invalidate()
        return true
      }
    }
  }
}