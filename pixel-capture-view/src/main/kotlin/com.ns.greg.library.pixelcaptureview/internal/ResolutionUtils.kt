package com.ns.greg.library.pixelcaptureview.internal

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.os.Build
import android.view.View.X
import android.view.WindowManager
import com.ns.greg.library.pixelcaptureview.ResolutionRatio
import com.ns.greg.library.pixelcaptureview.ResolutionRatio.FOUR_X_THREE
import com.ns.greg.library.pixelcaptureview.ResolutionRatio.ONE_X_ONE
import com.ns.greg.library.pixelcaptureview.ResolutionRatio.SIXTEEN_X_NINE

/**
 * @author gregho
 * @since 2018/9/19
 */
internal class ResolutionUtils {

  companion object {

    fun getRatioHeight(
      width: Int,
      ratio: ResolutionRatio
    ): Int {
      return when (ratio) {
        ONE_X_ONE -> width
        FOUR_X_THREE -> ((width / 4).toFloat() * 3).toInt()
        SIXTEEN_X_NINE -> ((width / 16).toFloat() * 9).toInt()
      }
    }
  }
}