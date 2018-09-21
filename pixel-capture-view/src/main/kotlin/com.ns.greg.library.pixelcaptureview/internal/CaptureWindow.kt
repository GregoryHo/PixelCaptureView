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

  /* only changed when invoked initOverlay */
  private val minBorder = Border(RectF())
  /* only changed when invoked initOverlay */
  private val maxBorder = Border(RectF())
  private val currentBorder = Border(RectF())
  private val radius = (Math.max(cornerWidth, cornerHeight) * 3) + cornerPadding

  fun initBorder(
    left: Float,
    top: Float,
    width: Float,
    height: Float
  ) {
    currentBorder.layout(left, top, left + width, top + height)
  }

  fun hasBorder(): Boolean {
    return currentBorder.hasLayout()
  }

  fun initOverlay(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
  ) {
    val width = right - left
    val height = bottom - top
    minBorder.layout(0f, 0f, width * 0.2f, height * 0.2f)
    maxBorder.layout(0f, 0f, width, height)
  }

  fun hasOverlay(): Boolean {
    return minBorder.hasLayout() and maxBorder.hasLayout()
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