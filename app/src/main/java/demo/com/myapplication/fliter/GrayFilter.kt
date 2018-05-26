package demo.com.myapplication.fliter

import android.content.res.Resources

/**
 * Created by LiuBin
 */
class GrayFilter(res: Resources) : AFilter(res) {
    override fun onCreate() {
        createProgramByAssetsFile("shader/base_vertex.sh",
                "shader/color/gray_fragment.frag")
    }

    override fun onSizeChanged(width: Int, height: Int) {
    }

}