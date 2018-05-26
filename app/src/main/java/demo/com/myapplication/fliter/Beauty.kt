package demo.com.myapplication.fliter

import android.content.res.Resources
import android.opengl.GLES20

/**
 * Created by LiuBin.
 */
class Beauty(res:Resources):AFilter(res){
    private var gHaaCoef: Int = 0
    private var gHmixCoef: Int = 0
    private var gHiternum: Int = 0
    private var gHWidth: Int = 0
    private var gHHeight: Int = 0

    private var aaCoef: Float = 0.toFloat()
    private var mixCoef: Float = 0.toFloat()
    private var iternum: Int = 0

    private var mWidth = 720
    private var mHeight = 1280

    init {
        setFlag(0)
    }

    override fun onCreate() {
        createProgramByAssetsFile("shader/beauty/beauty.vert", "shader/beauty/beauty.frag")
        gHaaCoef = GLES20.glGetUniformLocation(mProgram, "aaCoef")
        gHmixCoef = GLES20.glGetUniformLocation(mProgram, "mixCoef")
        gHiternum = GLES20.glGetUniformLocation(mProgram, "iternum")
        gHWidth = GLES20.glGetUniformLocation(mProgram, "mWidth")
        gHHeight = GLES20.glGetUniformLocation(mProgram, "mHeight")
    }

    override fun setFlag(flag: Int) {
        super.setFlag(flag)
        when (flag) {
            1 -> a(1, 0.19f, 0.54f)
            2 -> a(2, 0.29f, 0.54f)
            3 -> a(3, 0.17f, 0.39f)
            4 -> a(3, 0.25f, 0.54f)
            5 -> a(4, 0.13f, 0.54f)
            6 -> a(4, 0.19f, 0.69f)
            else -> a(0, 0f, 0f)
        }
    }

    private fun a(a: Int, b: Float, c: Float) {
        this.iternum = a
        this.aaCoef = b
        this.mixCoef = c
    }

    override fun onSizeChanged(width: Int, height: Int) {
        this.mWidth = width
        this.mHeight = height
    }

    override fun onSetExpandData() {
        super.onSetExpandData()
        GLES20.glUniform1i(gHWidth, mWidth)
        GLES20.glUniform1i(gHHeight, mHeight)
        GLES20.glUniform1f(gHaaCoef, aaCoef)
        GLES20.glUniform1f(gHmixCoef, mixCoef)
        GLES20.glUniform1i(gHiternum, iternum)
    }

}