package com.ns.greg.pixelcaptureview

import android.content.Context
import android.graphics.Rect
import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
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
    // set custom border size
    //pixelCaptureView.setBorder(360, 200, 720, 400)
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
  }
}