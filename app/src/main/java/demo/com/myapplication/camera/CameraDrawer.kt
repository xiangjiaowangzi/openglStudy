package demo.com.myapplication.camera

import android.content.res.Resources
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import demo.com.myapplication.fliter.AFilter
import demo.com.myapplication.fliter.OnesFilter
import demo.com.myapplication.utils.Gl2Utils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin
 */
class CameraDrawer(res: Resources) : GLSurfaceView.Renderer {

    private val matrix = FloatArray(16)
    private var surfaceTexture: SurfaceTexture? = null
    private var width: Int = 0
    private var height: Int = 0
    private var dataWidth: Int = 0
    private var dataHeight: Int = 0
    private val mOesFilter: AFilter
    private var cameraId = 1

    init {
        mOesFilter = OnesFilter(res)
    }

    fun setDataSize(width: Int, height: Int) {
        this.dataWidth = width
        this.dataHeight = height
        calculateMatrix()
    }

    fun setViewSize(width: Int, height: Int) {
        this.width = width
        this.height = height
        calculateMatrix()
    }

    private fun calculateMatrix() {
        Gl2Utils.getShowMatrix(matrix, this.dataWidth, this.dataHeight, this.width, this.height);
        if (cameraId == 1) {
            Gl2Utils.flip(matrix, false, true)
            Gl2Utils.rotate(matrix, -90f)
        } else {
            Gl2Utils.rotate(matrix, 270f)
        }
        mOesFilter.setMatrix(matrix)
    }

    private fun createTextureId(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        //设置缩小过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR.toFloat())
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
        return texture[0]
    }

    fun getSurfaceTexture() = surfaceTexture

    fun setCameraId(id: Int) {
        this.cameraId = id
        calculateMatrix()
    }

    override fun onDrawFrame(gl: GL10?) {
        surfaceTexture?.let {
            surfaceTexture!!.updateTexImage()
        }
        mOesFilter.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        setViewSize(width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        var texture = createTextureId()
        surfaceTexture = SurfaceTexture(texture)
        mOesFilter.create()
        mOesFilter.setTextureId(texture)
    }

}