package com.ns.greg.library.pixelcaptureview

/**
 * @author gregho
 * @since 2018/9/19
 */
enum class ResolutionRatio(
  internal val value: Int,
  private val widthRatio: Float,
  private val heightRatio: Float
) {

  ONE_X_ONE(0, 1f, 1f),
  FOUR_X_THREE(1, 4f, 3f),
  SIXTEEN_X_NINE(2, 16f, 9f);

  override fun toString(): String {
    return when (value) {
      0 -> "Resolution 1:1"
      1 -> "Resolution 4:3"
      2 -> "Resolution 16:9"
      else -> "Resolution unknown"
    }
  }

  fun getHeight(
    width: Float
  ): Int {
    return (width / widthRatio * heightRatio).toInt()
  }

  companion object {

    fun fromValue(value: Int): ResolutionRatio? {
      for (ratio in ResolutionRatio.values()) {
        if (ratio.value == value) {
          return ratio
        }
      }

      return null
    }
  }
}