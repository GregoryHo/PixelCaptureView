package com.ns.greg.library.pixelcaptureview

/**
 * @author gregho
 * @since 2018/9/19
 */
enum class ResolutionRatio(internal val value: Int) {

  ONE_X_ONE(0),
  FOUR_X_THREE(1),
  SIXTEEN_X_NINE(2);

  override fun toString(): String {
    return when (value) {
      0 -> "Resolution 1:1"
      1 -> "Resolution 4:3"
      2 -> "Resolution 16:9"
      else -> "Resolution unknown"
    }
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