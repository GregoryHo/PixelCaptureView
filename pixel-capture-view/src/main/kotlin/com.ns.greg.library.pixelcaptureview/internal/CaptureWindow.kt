package com.ns.greg.library.pixelcaptureview.internal

import android.graphics.RectF
import com.ns.greg.library.pixelcaptureview.internal.Corner.LEFT_BOTTOM
import com.ns.greg.library.pixelcaptureview.internal.Corner.LEFT_TOP
import com.ns.greg.library.pixelcaptureview.internal.Corner.RIGHT_BOTTOM
import com.ns.greg.library.pixelcaptureview.internal.Corner.RIGHT_TOP

/**
 * @author gregho
 * @since 2018/9/19
 */
internal class CaptureWindow(
  val cornerPadding: Float,
  val cornerWidth: Float,
  val cornerHeight: Float
) {

  /* only changed when invoked layoutOverlay */
  private val minBorder = Border(RectF())
  /* only changed when invoked layoutOverlay */
  private val maxBorder = Border(RectF())
  private val currentBorder = Border(RectF())
  private val radius = (Math.max(cornerWidth, cornerHeight) * 3) + cornerPadding

  fun applyBorder(
    left: Float,
    top: Float,
    width: Float,
    height: Float
  ) {
    with(maxBorder) {
      if (hasLayout()) {
        with(getRectf()) {
          val l = if (left < this.left) this.left else left
          val t = if (top < this.top) this.top else top
          val w = if (width < this.width()) width else this.width()
          val h = if (height < this.height()) height else this.height()
          currentBorder.layout(l, t, l + w, t + h)
        }
      }
    }
  }

  fun hasBorder(): Boolean {
    return currentBorder.hasLayout()
  }

  fun layoutOverlay(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
  ) {
    val width = right - left
    val height = bottom - top
    minBorder.layout(0f, 0f, width * 0.4f, height * 0.4f)
    maxBorder.layout(0f, 0f, width, height)
  }

  fun hasOverlay(): Boolean {
    return minBorder.hasLayout() and maxBorder.hasLayout()
  }

  fun centerCrop() {
    with(maxBorder.getRectf()) {
      val cx = width() / 2
      val cy = height() / 2
      applyBorder(cx / 2, cy / 2, cx, cy)
    }
  }

  fun fitXy() {
    with(maxBorder.getRectf()) {
      applyBorder(0f, 0f, right, bottom)
    }
  }

  fun getDrawingRectf(): RectF {
    return currentBorder.getRectf()
  }

  fun isTouchOnWindow(
    x: Float,
    y: Float
  ): Boolean {
    return currentBorder.getRectf()
        .contains(x, y)
  }

  fun isTouchOnCorner(
    x: Float,
    y: Float
  ): Corner? {
    return when {
      currentBorder.getCorners(LEFT_TOP, radius).contains(x, y) -> LEFT_TOP
      currentBorder.getCorners(RIGHT_TOP, radius).contains(x, y) -> RIGHT_TOP
      currentBorder.getCorners(LEFT_BOTTOM, radius).contains(x, y) -> LEFT_BOTTOM
      currentBorder.getCorners(RIGHT_BOTTOM, radius).contains(x, y) -> RIGHT_BOTTOM
      else -> null
    }
  }

  fun scale(
    corner: Corner,
    dx: Float,
    dy: Float
  ) {
    val min = minBorder.getRectf()
    val max = maxBorder.getRectf()
    val current = currentBorder.getRectf()
    when (corner) {
      LEFT_TOP -> {
        /* width */
        val left = current.left + dx
        if ((left >= max.left) and (left + min.width() <= current.right)) {
          currentBorder.applyX(left, current.right)
        }
        /* height */
        val top = current.top + dy
        if ((top >= max.top) and (top + min.height() <= current.bottom)) {
          currentBorder.applyY(top, current.bottom)
        }
      }
      RIGHT_TOP -> {
        /* width */
        val right = current.right + dx
        if ((right <= max.right) and (right - min.width() >= current.left)) {
          currentBorder.applyX(current.left, right)
        }
        /* height */
        val top = current.top + dy
        if ((top >= max.top) and (top + min.height() <= current.bottom)) {
          currentBorder.applyY(top, current.bottom)
        }
      }
      LEFT_BOTTOM -> {
        /* width */
        val left = current.left + dx
        if ((left >= max.left) and (left + min.width() <= current.right)) {
          currentBorder.applyX(left, current.right)
        }
        /* height */
        val bottom = current.bottom + dy
        if ((bottom <= max.bottom) and (bottom - min.height() >= current.top)) {
          currentBorder.applyY(current.top, bottom)
        }
      }
      RIGHT_BOTTOM -> {
        /* width */
        val right = current.right + dx
        if ((right <= max.right) and (right - min.width() >= current.left)) {
          currentBorder.applyX(current.left, right)
        }
        /* height */
        val bottom = current.bottom + dy
        if ((bottom <= max.bottom) and (bottom - min.height() >= current.top)) {
          currentBorder.applyY(current.top, bottom)
        }
      }
    }
  }

  fun translate(
    dx: Float,
    dy: Float
  ) {
    val max = maxBorder.getRectf()
    val current = currentBorder.getRectf()
    val left = current.left + dx
    val right = current.right + dx
    /* width */
    if ((left >= max.left) and (right + dx <= max.right)) {
      currentBorder.applyX(left, right)
    }
    /* height */
    val top = current.top + dy
    val bottom = current.bottom + dy
    if ((top >= max.top) and (bottom + dy <= max.bottom)) {
      currentBorder.applyY(top, bottom)
    }
  }
}