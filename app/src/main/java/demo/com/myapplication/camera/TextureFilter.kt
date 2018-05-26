package demo.com.myapplication.camera

import android.content.res.Resources
import android.graphics.SurfaceTexture
import android.opengl.GLES10
import android.opengl.GLES20
import android.util.Log
import demo.com.myapplication.fliter.AFilter
import demo.com.myapplication.fliter.CameraFilter
import demo.com.myapplication.utils.EasyGLUtils
import demo.com.myapplication.utils.LogUtils

/**
 * Created by LiuBin
 */
class TextureFilter(res:Resources) : AFilter(res){

    private var filter:CameraFilter
    var mSurfaceTexture: SurfaceTexture? = null
    private val mCoordOM = FloatArray(16)

    private var width = 0
    private var height = 0

    private val fFrame = IntArray(1)
    private val fTexture = IntArray(1)
    private val mCameraTexture = IntArray(1)

    init {
        filter = CameraFilter(res)
    }

    override fun setFlag(flag: Int) {
        filter.setFlag(flag)
    }

    override fun setMatrix(matrix: FloatArray) {
        filter.setMatrix(matrix)
    }

    override fun getOutputTexture(): Int = fTexture[0]

    override fun draw() {
        val b = GLES20.glIsEnabled(GLES20.GL_DEPTH_TEST)
        if (b){
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }
        if (mSurfaceTexture!=null){
            mSurfaceTexture!!.updateTexImage()
            mSurfaceTexture!!.getTransformMatrix(mCoordOM)
            filter.setCoordMatrix(mCoordOM)
        }
        EasyGLUtils.bindFrameTexture(fFrame[0],fTexture[0])
        GLES20.glViewport(0, 0, width, height)
        filter.setTextureId(mCameraTexture[0])
        filter.draw()
//        LogUtils.log( "textureFilter draw")
        EasyGLUtils.unBindFrameBuffer()
        if (b) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }

    }

    fun getTexture(): SurfaceTexture {
        return mSurfaceTexture!!
    }

    private fun createOesTexture() {
        // 生成纹理
        GLES20.glGenTextures(1, mCameraTexture, 0)
    }

    override fun onCreate() {
        filter.create()
        createOesTexture()
        // SurfaceTexture 是 Surface 和 GLES texture 的粗糙结合
        mSurfaceTexture = SurfaceTexture(mCameraTexture[0])

    }

    override fun onSizeChanged(width: Int, height: Int) {
        filter.setSize(width, height)
        if (this.width != width || this.height != height) {
            this.width = width
            this.height = height
            //创建FrameBuffer和Texture
            deleteFrameBuffer()
            GLES20.glGenFramebuffers(1, fFrame, 0)
            EasyGLUtils.genTexturesWithParameter(1, fTexture, 0, GLES20.GL_RGBA, width, height)
        }
    }

    private fun deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fFrame, 0)
        GLES20.glDeleteTextures(1, fTexture, 0)
    }

}