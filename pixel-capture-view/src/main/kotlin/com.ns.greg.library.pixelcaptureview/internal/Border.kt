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
internal class Border(
  private val rectF: RectF
) {

  fun getRectf(): RectF {
    return rectF
  }

  fun layout(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
  ) {
    with(rectF) {
      this.left = left
      this.top = top
      this.right = right
      this.bottom = bottom
    }
  }

  fun hasLayout(): Boolean {
    with(rectF) {
      return (left != 0f) or (top != 0f) or (right != 0f) or (bottom != 0f)
    }
  }

  fun getCorners(
    corner: Corner,
    radius: Float
  ): RectF {
    with(rectF) {
      return when (corner) {
        LEFT_TOP -> RectF(left, top, left + radius, top + radius)
        RIGHT_TOP -> RectF(right - radius, top, right, top + radius)
        LEFT_BOTTOM -> RectF(left, bottom - radius, left + radius, bottom)
        RIGHT_BOTTOM -> RectF(right - radius, bottom - radius, right, bottom)
      }
    }
  }

  fun applyX(
    left: Float,
    right: Float
  ) {
    rectF.left = left
    rectF.right = right
  }

  fun applyY(
    top: Float,
    bottom: Float
  ) {
    rectF.top = top
    rectF.bottom = bottom
  }
}