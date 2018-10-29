package com.ns.greg.library.pixelcaptureview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Paint.Style.STROKE
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams
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
  private var setBorderThread: Thread? = null
  private var setResourceThread: Thread? = null
  private var isMoving = false
  private var listener: CaptureListener? = null
  private var captured = true

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
    /* always scale as FIT_XY */
    scaleType = FIT_XY
  }

  override fun setScaleType(scaleType: ScaleType?) {
    scaleType?.run {
      if (this != FIT_XY) {
        return
      }
    }

    super.setScaleType(scaleType)
  }

  override fun setImageResource(resId: Int) {
    if (isLayoutRequested) {
      setImageResourceAsync(resId)
    } else {
      super.setImageResource(resId)
    }
  }

  override fun setImageDrawable(drawable: Drawable?) {
    if (isLayoutRequested) {
      setImageDrawableAsync(drawable)
    } else {
      super.setImageDrawable(drawable)
    }
  }

  override fun onLayout(
    changed: Boolean,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int
  ) {
    super.onLayout(changed, left, top, right, bottom)
    val width = right - left
    val height = bottom - top
    if (layoutParams.width == LayoutParams.WRAP_CONTENT) {
      if (drawable != null) {
        Log.i(
            javaClass.simpleName,
            "PixelCaptureView can't resolve layout_width with LayoutParams.WRAP_CONTENT. " +
                "The layout size won't adjust to the resolution ratio which you assigned, " +
                "use LayoutParams.MATCH_PARENT or fixed dimension to layout_width."
        )
      }
    } else {
      if (changed) {
        /* request layout by resolution ratio */
        initRatioLayout(width)
      }
    }

    if ((width > 0) and (height > 0)) {
      /* init overlay */
      captureWindow.layoutOverlay(
          left.toFloat(), top.toFloat(), right.toFloat(),
          bottom.toFloat()
      )
      println("PixelCaptureView.onLayout")
      /* init border if has no one */
      if (!captureWindow.hasBorder()) {
        borderCenterCrop()
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
    if (!isEnabled or !captureWindow.hasOverlay()) {
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

  fun borderFitCustom(
    x: Int,
    y: Int,
    width: Int,
    height: Int
  ) {
    synchronized(this) {
      with(captureWindow) {
        if (hasOverlay()) {
          applyBorder(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
          post {
            invalidate()
          }
        } else {
          setBorderThread?.interrupt()
          setBorderThread = Thread {
            while (!hasOverlay()) {
              try {
                Thread.sleep(10L)
              } catch (e: InterruptedException) {
              }
            }

            applyBorder(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
            post {
              invalidate()
            }
          }.also {
            it.start()
          }
        }
      }
    }
  }

  fun borderCenterCrop() {
    synchronized(this) {
      with(captureWindow) {
        if (hasOverlay()) {
          centerCrop()
          post {
            invalidate()
          }
        } else {
          setBorderThread?.interrupt()
          setBorderThread = Thread {
            while (!hasOverlay()) {
              try {
                Thread.sleep(10L)
              } catch (e: InterruptedException) {
              }
            }

            centerCrop()
            post {
              invalidate()
            }
          }.also {
            it.start()
          }
        }
      }
    }
  }

  fun borderFitXy() {
    synchronized(this) {
      with(captureWindow) {
        if (hasOverlay()) {
          fitXy()
          invalidate()
        } else {
          setBorderThread?.interrupt()
          setBorderThread = Thread {
            while (!hasOverlay()) {
              try {
                Thread.sleep(10L)
              } catch (e: InterruptedException) {
              }

              fitXy()
              post {
                invalidate()
              }
            }
          }.also {
            it.start()
          }
        }
      }
    }
  }

  fun setCaptureListener(listener: CaptureListener) {
    this.listener = listener
  }

  fun setCaptured(captured: Boolean) {
    this.captured = captured
    invalidate()
  }

  fun capture() {
    if (!captured) {
      return
    }

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
    if (!captured) {
      return
    }

    canvas.drawRect(captureWindow.getDrawingRectf(), borderPaint)
  }

  private fun drawCorners(canvas: Canvas) {
    if (!captured) {
      return
    }

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
    if (!captured or !isMoving or (cells == 0)) {
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
    val params = layoutParams
    val height =
      ResolutionUtils.getRatioHeight(width, captureOptions.resolutionRatio)
    params.height = height
    layoutParams = params
  }

  private fun setImageResourceAsync(resId: Int) {
    setResourceThread?.interrupt()
    setResourceThread = Thread {
      while (isLayoutRequested) {
        try {
          Thread.sleep(10)
        } catch (e: InterruptedException) {
        }
      }

      post {
        setImageResource(resId)
      }
    }.also {
      it.start()
    }
  }

  private fun setImageDrawableAsync(drawable: Drawable?) {
    setResourceThread?.interrupt()
    setResourceThread = Thread {
      while (isLayoutRequested) {
        try {
          Thread.sleep(10L)
        } catch (e: InterruptedException) {
        }
      }

      post {
        setImageDrawable(drawable)
      }
    }.also {
      it.start()
    }
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