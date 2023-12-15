package com.example.cursework

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mGLSurfaceView = GLSurfaceView(this)
        mGLSurfaceView.setEGLContextClientVersion(2)

        val renderer = ShadowsRenderer(this, this)
        mGLSurfaceView.setRenderer(renderer)

        setContentView(mGLSurfaceView)
    }
}
