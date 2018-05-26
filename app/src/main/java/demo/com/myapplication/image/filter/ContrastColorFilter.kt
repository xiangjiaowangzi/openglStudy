package demo.com.myapplication.image.filter

import android.content.Context
import android.opengl.GLES20

/**
 * Created by LiuBin
 */
class ContrastColorFilter(context: Context, private val filter: ColorFilter.Filter) :
        IFilter(context, "filter/half_color_vertex.sh", "filter/half_color_fragment.sh") {

    private var hChangeType: Int = 0
    private var hChangeColor: Int = 0

    override
    fun onDrawSet() {
        GLES20.glUniform1i(hChangeType, filter.type)
        GLES20.glUniform3fv(hChangeColor, 1, filter.data(), 0)
    }

    override
    fun onDrawCreatedSet(mProgram: Int) {
        hChangeType = GLES20.glGetUniformLocation(mProgram, "vChangeType")
        hChangeColor = GLES20.glGetUniformLocation(mProgram, "vChangeColor")
    }

}