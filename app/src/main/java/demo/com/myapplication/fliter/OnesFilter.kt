package demo.com.myapplication.fliter

import android.content.res.Resources
import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.util.Arrays

/**
 * Created by LiuBin
 */
open class OnesFilter(res: Resources) : AFilter(res) {

    private var vCoordMatrix: Int = 0
    private var mCoordMatrix = Arrays.copyOf(OM, 16)

    override fun onCreate() {
        createProgramByAssetsFile("shader/oes_base_vertex.sh", "shader/oes_base_fragment.sh")
        vCoordMatrix = GLES20.glGetUniformLocation(mProgram, "vCoordMatrix")
    }

    fun setCoordMatrix(matrix: FloatArray) {
        this.mCoordMatrix = matrix
    }

    override fun onSetExpandData() {
        super.onSetExpandData()
        GLES20.glUniformMatrix4fv(vCoordMatrix, 1, false, mCoordMatrix, 0)
    }

    override fun onBindTexture() {
//        super.onBindTexture()
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + getTextureType())
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getTextureId())
        GLES20.glUniform1i(mHTexture, getTextureType())
    }

    override fun onSizeChanged(width: Int, height: Int) {

    }

}