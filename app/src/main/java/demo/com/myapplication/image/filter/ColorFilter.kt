package demo.com.myapplication.image.filter

import android.content.Context
import android.opengl.GLES20

/**
 * Created by LiuBin
 */
class ColorFilter(context: Context, val mFilter: Filter) : IFilter(context, "filter/default_vertex.sh",
        "filter/color_fragment.sh") {


    private var hChangeType: Int = 0
    private var hChangeColor: Int = 0

    enum class Filter private constructor(val type: Int, private val data: FloatArray) {

        NONE(0, floatArrayOf(0.0f, 0.0f, 0.0f)),
        GRAY(1, floatArrayOf(0.299f, 0.587f, 0.114f)),
        COOL(2, floatArrayOf(0.0f, 0.0f, 0.1f)),
        WARM(2, floatArrayOf(0.1f, 0.1f, 0.0f)),
        BLUR(3, floatArrayOf(0.006f, 0.004f, 0.002f)),
        MAGN(4, floatArrayOf(0.4f, 0.4f, 0.4f));

        fun data(): FloatArray {
            return data
        }

    }

    override fun onDrawSet() {
        GLES20.glUniform1i(hChangeType, mFilter.type)
        GLES20.glUniform3fv(hChangeColor, 1, mFilter.data(), 0)
    }

    override fun onDrawCreatedSet(program: Int) {
        hChangeType = GLES20.glGetUniformLocation(program, "vChangeType")
        hChangeColor = GLES20.glGetUniformLocation(program, "vChangeColor")
    }
}