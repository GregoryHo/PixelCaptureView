package com.ns.greg.library.pixelcaptureview.internal

import android.graphics.Color
import com.ns.greg.library.pixelcaptureview.ResolutionRatio
import com.ns.greg.library.pixelcaptureview.ResolutionRatio.FOUR_X_THREE

/**
 * @author gregho
 * @since 2018/9/20
 */
data class CaptureOptions(
  val resolutionRatio: ResolutionRatio = FOUR_X_THREE,
  val borderLineColor: Int = Color.WHITE,
  val borderLineThickness: Float = 2f,
  val cornerPadding: Float = 20f,
  val cornerLineColor: Int = Color.WHITE,
  val cornerLineThickness: Float = 2f,
  val cornerWidth: Float = 20f,
  val cornerHeight: Float = 20f,
  val gridCells: Int = 3
)