package demo.com.myapplication.image

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.view.View
import demo.com.myapplication.fliter.AFilter
import demo.com.myapplication.image.filter.ColorFilter
import demo.com.myapplication.image.filter.ContrastColorFilter
import demo.com.myapplication.image.filter.IFilter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin
 */
class SGLRender(view: View) : GLSurfaceView.Renderer {

    var mFilter: IFilter = ContrastColorFilter(view.context, ColorFilter.Filter.NONE)
    var bitmap: Bitmap? = null
    var width: Int = 0
    var height: Int = 0
    var refreshFlag = false
    var config: EGLConfig? = null

    fun setFilter(filter: IFilter) {
        refreshFlag = true
        mFilter = filter
        if (bitmap != null) {
            mFilter.setBitmap(bitmap!!)
        }
    }

    fun setImage(bitmap: Bitmap) {
        this.bitmap = bitmap
        mFilter.setBitmap(bitmap)
    }

    fun setImageBuffer(buffer: IntArray, width: Int, height: Int) {
        bitmap = Bitmap.createBitmap(buffer, width, height, Bitmap.Config.RGB_565)
        mFilter.setBitmap(bitmap!!)
    }

    fun refresh() {
        refreshFlag = true
    }


    override fun onDrawFrame(gl: GL10?) {
        if (refreshFlag && width != 0 && height != 0) {
            mFilter.onSurfaceCreated(gl, config)
            mFilter.onSurfaceChanged(gl, width, height)
            refreshFlag = false
        }
        mFilter.onDrawFrame(gl)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height
        mFilter.onSurfaceChanged(gl, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        this.config = config
        mFilter.onSurfaceCreated(gl, config)
    }

}