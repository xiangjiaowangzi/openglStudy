package demo.com.myapplication.fliter

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import demo.com.myapplication.utils.EasyGLUtils
import java.io.IOException

/**
 * Created by LiuBin.
 */
class LookupFilter(res:Resources):AFilter(res){
    private var mHMaskImage: Int = 0
    private var mHIntensity: Int = 0

    private var intensity: Float = 0.toFloat()

    private val mastTextures = IntArray(1)
    private var mBitmap: Bitmap? = null

    override fun onCreate() {
        createProgramByAssetsFile("lookup/lookup.vert", "lookup/lookup.frag")
        mHMaskImage = GLES20.glGetUniformLocation(mProgram, "maskTexture")
        mHIntensity = GLES20.glGetUniformLocation(mProgram, "intensity")
        EasyGLUtils.genTexturesWithParameter(1, mastTextures, 0, GLES20.GL_RGBA, 512, 512)
    }

    fun setIntensity(value: Float) {
        this.intensity = value
    }

    fun setMaskImage(mask: String) {
        try {
            mBitmap = BitmapFactory.decodeStream(mRes.assets.open(mask))
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun setMaskImage(bitmap: Bitmap) {
        this.mBitmap = bitmap
    }

    override fun onSizeChanged(width: Int, height: Int) {

    }

    override fun onBindTexture() {
        super.onBindTexture()
    }

    override fun onSetExpandData() {
        super.onSetExpandData()
        GLES20.glUniform1f(mHIntensity, intensity)
        if (mastTextures[0] != 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + getTextureType() + 1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mastTextures[0])
            if (mBitmap != null && !mBitmap!!.isRecycled) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
                mBitmap!!.recycle()
            }
            GLES20.glUniform1i(mHMaskImage, getTextureType() + 1)
        }

    }
}