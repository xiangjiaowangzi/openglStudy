package demo.com.myapplication.camera

import android.opengl.GLSurfaceView

/**
 * Created by LiuBin
 */
interface CameraRender : GLSurfaceView.Renderer{
    fun onDestroy()
}