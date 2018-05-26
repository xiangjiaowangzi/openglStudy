package demo.com.myapplication.image

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import demo.com.myapplication.fliter.AFilter
import demo.com.myapplication.image.filter.IFilter
import java.io.IOException

/**
 * Created by LiuBin
 */
class SGLView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        GLSurfaceView(context, attrs) {

    var render: SGLRender? = null
        private set

    init {
        init()
    }

    private fun init() {
        setEGLContextClientVersion(2)
        render = SGLRender(this)
        setRenderer(render)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        try {
            render!!.setImage(
                    BitmapFactory.decodeStream(resources.assets.open("texture/fengj.png")))
            requestRender()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun setFilter(filter: IFilter) {
        render!!.setFilter(filter)
    }

}