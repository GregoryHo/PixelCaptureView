package com.ns.greg.pixelcaptureview

import android.content.Context
import android.graphics.Rect
import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.ns.greg.library.pixelcaptureview.CaptureListener
import com.ns.greg.library.pixelcaptureview.PixelCaptureView

/**
 * @author gregho
 * @since 2018/9/19
 */
class DemoActivity : AppCompatActivity() {

  private lateinit var pixelCaptureView: PixelCaptureView
  private var enabled = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_demo)
    pixelCaptureView = findViewById(R.id.test_civ)
    pixelCaptureView.setCaptureListener(object : CaptureListener {
      override fun onCapture(
        imageWidth: Int,
        imageHeight: Int,
        capture: Rect
      ) {
        println(
            "imageWidth = [${imageWidth}], imageHeight = [${imageHeight}], capture = [${capture}]"
        )
      }
    })
    GlideApp.with(applicationContext)
        .load(R.drawable.ic_touka)
        .into(pixelCaptureView)
    findViewById<View>(R.id.capture_btn).setOnClickListener {
      pixelCaptureView.capture()
    }
    findViewById<View>(R.id.enabled_btn).setOnClickListener {
      enabled = !enabled
      pixelCaptureView.setCaptured(enabled)
    }
    findViewById<View>(R.id.center_crop_btn).setOnClickListener {
      pixelCaptureView.borderCenterCrop()
    }
    findViewById<View>(R.id.fit_xy_btn).setOnClickListener {
      pixelCaptureView.borderFitXy()
    }
    // set custom border size
    pixelCaptureView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
      override fun onGlobalLayout() {
        pixelCaptureView.viewTreeObserver.removeOnGlobalLayoutListener(this)
        /* if custom border is large too large, will just fit source xy */
        pixelCaptureView.borderFitCustom(0, 0, 2000, 2000)
      }
    })
  }
}