package com.ns.greg.pixelcaptureview

import android.graphics.Rect
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_demo)
    pixelCaptureView = findViewById(R.id.test_civ)
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
  }
}