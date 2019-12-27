package com.cherry.cropwindow

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bmp = BitmapFactory.decodeResource(resources, R.drawable.wallpaper)
        crop_imageview.setImageBitmap(bmp)
        crop_imageview.setCropEnabled(true)
    }
}
