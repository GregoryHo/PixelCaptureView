package com.ns.greg.library.pixelcaptureview

import android.graphics.Rect
import android.graphics.RectF

/**
 * @author gregho
 * @since 2018/9/20
 */
interface CaptureListener {

  fun onCapture(
    imageWidth: Int,
    imageHeight: Int,
    capture: Rect
  )
}