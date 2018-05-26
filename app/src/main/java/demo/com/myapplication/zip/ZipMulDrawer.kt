package demo.com.myapplication.zip

import android.content.res.Resources
import android.opengl.ETC1
import android.opengl.ETC1Util
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import demo.com.myapplication.fliter.AFilter
import demo.com.myapplication.utils.Gl2Utils
import demo.com.myapplication.utils.LogUtils
import java.nio.ByteBuffer

/**
 * Created by LiuBin.
 */
class ZipMulDrawer(mRes: Resources) : AFilter(mRes) {

    private var mView: GLSurfaceView? = null
    private var timeStep = 50
    var isPlay = false

    private var emptyBuffer: ByteBuffer? = null
    private var width: Int = 0
    private var height: Int = 0
    private val SM = Gl2Utils.getOriginalMatrix()
    private var type = Gl2Utils.TYPE_CENTERINSIDE

    private var texture: IntArray? = null

    private val mPkmReader: ZipPkmReader?
    private var mGlHAlpha: Int = 0

    private var mStateChangeListener: StateChangeListener? = null

    private var time: Long = 0

    init {
        mPkmReader = ZipPkmReader(mRes.assets)
    }

    override fun onCreate() {
        createProgramByAssetsFile("shader/pkm_mul.vert", "shader/pkm_mul.frag")
        texture = IntArray(2)
        createEtcTexture(texture!!)
        setTextureId(texture!![0])

        mGlHAlpha = GLES20.glGetUniformLocation(mProgram, "vTextureAlpha")
    }

    override fun onClear() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    override fun onSizeChanged(width: Int, height: Int) {
        emptyBuffer = ByteBuffer.allocateDirect(ETC1.getEncodedDataSize(width, height))
        this.width = width
        this.height = height
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun onBindTexture() {
        val t = mPkmReader!!.nextTexture
        val tAlpha = mPkmReader.nextTexture
        LogUtils.log( "is ETC null->" + (t == null))
        if (t != null && tAlpha != null) {
            Gl2Utils.getMatrix(SM, type, t.width, t.height, width, height)
            setMatrix(SM)
            onSetExpandData()
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + getTextureType())
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture!![0])
            ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D, 0, 0, GLES20.GL_RGB, GLES20
                    .GL_UNSIGNED_SHORT_5_6_5, t)
            GLES20.glUniform1i(mHTexture, getTextureType())

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1 + getTextureType())
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture!![1])
            ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D, 0, 0, GLES20.GL_RGB, GLES20
                    .GL_UNSIGNED_SHORT_5_6_5, tAlpha)
            GLES20.glUniform1i(mGlHAlpha, 1 + getTextureType())
        } else {
            setMatrix(OM)
            onSetExpandData()
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + getTextureType())
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture!![0])
            ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D, 0, 0, GLES20.GL_RGB, GLES20
                    .GL_UNSIGNED_SHORT_5_6_5, ETC1Util.ETC1Texture(width, height, emptyBuffer))
            GLES20.glUniform1i(mHTexture, getTextureType())

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1 + getTextureType())
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture!![1])
            ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D, 0, 0, GLES20.GL_RGB, GLES20
                    .GL_UNSIGNED_SHORT_5_6_5, ETC1Util.ETC1Texture(width, height, emptyBuffer))
            GLES20.glUniform1i(mGlHAlpha, 1 + getTextureType())
            isPlay = false
        }
    }

    override fun setInt(type: Int, vararg params: Int) {
        if (type == TYPE) {
            this.type = params[0]
        }
        super.setInt(type, *params)
    }

    override fun draw() {
        if (time != 0L) {
            LogUtils.log("time-->" + (System.currentTimeMillis() - time))
        }
        time = System.currentTimeMillis()
        val startTime = System.currentTimeMillis()
        super.draw()
        val s = System.currentTimeMillis() - startTime
        if (isPlay) {
            if (s < timeStep) {
                try {
                    Thread.sleep(timeStep - s)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
            mView!!.requestRender()
        } else {
            changeState(StateChangeListener.PLAYING, StateChangeListener.STOP)
        }
    }

    fun setAnimation(view: GLSurfaceView, path: String, timeStep: Int) {
        this.mView = view
        this.timeStep = timeStep
        mPkmReader!!.setZipPath(path)
    }

    fun start() {
        if (!isPlay) {
            stop()
            isPlay = true
            changeState(StateChangeListener.STOP, StateChangeListener.START)
            mPkmReader!!.open()
            mView!!.requestRender()
        }
    }

    fun stop() {
        mPkmReader?.close()
        isPlay = false
    }

    fun setStateChangeListener(listener: StateChangeListener) {
        this.mStateChangeListener = listener
    }

    private fun changeState(lastState: Int, nowState: Int) {
        if (this.mStateChangeListener != null) {
            this.mStateChangeListener!!.onStateChanged(lastState, nowState)
        }
    }

    private fun createEtcTexture(texture: IntArray) {
        //生成纹理
        GLES20.glGenTextures(2, texture, 0)
        for (i in texture.indices) {
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[i])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST.toFloat())
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR.toFloat())
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE.toFloat())
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE.toFloat())
            //根据以上指定的参数，生成一个2D纹理
        }
    }

    companion object {
        val TYPE = 0x01
    }

}