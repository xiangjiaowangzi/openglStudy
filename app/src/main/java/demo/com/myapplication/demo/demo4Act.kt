package demo.com.myapplication.demo

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import demo.com.myapplication.R
import demo.com.myapplication.camera.CameraDrawer
import demo.com.myapplication.vary.VaryRender
import kotlinx.android.synthetic.main.activity_opengl.mGLView

/**
 * Created by LiuBin
 */
class demo4Act : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opengl)
        mGLView.setEGLContextClientVersion(2)
        mGLView.setRenderer(VaryRender(resources))
        mGLView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }





}