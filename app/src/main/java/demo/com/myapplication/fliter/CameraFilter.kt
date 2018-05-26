package demo.com.myapplication.fliter

import android.content.res.Resources
import android.hardware.Camera

/**
 * Created by LiuBin.
 */
class CameraFilter(mRes: Resources) : OnesFilter(mRes) {

    override fun initBuffer() {
        super.initBuffer()
        movie()
    }

    override fun setFlag(flag: Int) {
        super.setFlag(flag)
        if (getFlag() == Camera.CameraInfo.CAMERA_FACING_FRONT) {    //前置摄像头
            cameraFront()
        } else if (getFlag() == Camera.CameraInfo.CAMERA_FACING_BACK) {   //后置摄像头
            cameraBack()
        }
    }

    private fun cameraFront() {
        val coord = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f)
        mTexBuffer.clear()
        mTexBuffer.put(coord)
        mTexBuffer.position(0)
    }

    private fun cameraBack() {
        val coord = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f)
        mTexBuffer.clear()
        mTexBuffer.put(coord)
        mTexBuffer.position(0)
    }

    private fun movie() {
        val coord = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f)
        mTexBuffer.clear()
        mTexBuffer.put(coord)
        mTexBuffer.position(0)
    }
}