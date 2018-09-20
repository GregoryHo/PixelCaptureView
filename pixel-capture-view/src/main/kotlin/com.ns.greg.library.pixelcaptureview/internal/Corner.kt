package com.ns.greg.library.pixelcaptureview.internal

/**
 * @author gregho
 * @since 2018/9/20
 */
internal enum class Corner(private val value: Int) {

  LEFT_TOP(0),
  RIGHT_TOP(1),
  LEFT_BOTTOM(2),
  RIGHT_BOTTOM(3);

  override fun toString(): String {
    return when (value) {
      0 -> "Left Top"
      1 -> "Right Top"
      2 -> "Left Bottom"
      3 -> "Right Bottom"
      else -> " UNKNOWN"
    }
  }

  companion object {

    fun fromValue(value: Int): Corner? {
      for (corner in Corner.values()) {
        if (corner.value == value) {
          return corner
        }
      }

      return null
    }
  }
}