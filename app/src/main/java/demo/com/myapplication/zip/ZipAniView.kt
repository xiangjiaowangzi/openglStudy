package demo.com.myapplication.zip

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin.
 */
class ZipAniView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        GLSurfaceView(context, attrs), GLSurfaceView.Renderer {

    private var mDrawer: ZipMulDrawer? = null

    val isPlay: Boolean
        get() = mDrawer!!.isPlay

    init {
        init()
    }

    private fun init() {
        setEGLContextClientVersion(2)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setRenderer(this)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        mDrawer = ZipMulDrawer(resources)
    }

    fun setScaleType(type: Int) {
        if (mDrawer != null) {
            mDrawer!!.setInt(ZipMulDrawer.TYPE, type)
        }
    }

    fun setAnimation(path: String, timeStep: Int) {
        mDrawer!!.setAnimation(this, path, timeStep)
    }

    fun start() {
        mDrawer!!.start()
    }

    fun stop() {
        mDrawer!!.stop()
    }

    fun setStateChangeListener(listener: StateChangeListener) {
        mDrawer!!.setStateChangeListener(listener)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        mDrawer!!.create()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        mDrawer!!.onSizeChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        mDrawer!!.draw()
    }

}