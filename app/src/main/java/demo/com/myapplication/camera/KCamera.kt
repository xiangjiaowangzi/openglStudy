package demo.com.myapplication.camera

import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.Size
import android.hardware.Camera.open
import demo.com.myapplication.camera.ICamera.Config
import demo.com.myapplication.camera.ICamera.PreviewFrameCallback
import demo.com.myapplication.camera.ICamera.TakePhotoCallback
import java.util.Collections
import java.util.Comparator

/**
 * Created by LiuBin
 */
class KCamera : ICamera {

    var mConfig: Config = Config()
    private val sizeComparator = CameraSizeComparator()
    private var mCamera: Camera? = null

    private var picSize: Camera.Size? = null
    private var preSize: Camera.Size? = null

    private var mPicSize: Point? = null
    private var mPreSize: Point? = null

    init {
        initConfig()
    }

    fun initConfig() {
        mConfig.minPreviewWidth = 720
        mConfig.minPictureWidth = 720
        mConfig.rate = 1.778f

    }

    override fun setConfig(config: Config?) {
        config?.let {
            this.mConfig = config
        }
    }

    override fun open(cameraId: Int): Boolean {
        mCamera = Camera.open(cameraId)
        mCamera.let {
            val param = mCamera!!.parameters
            picSize = getPropPictureSize(param.supportedPictureSizes, mConfig.rate,
                    mConfig.minPictureWidth)
            preSize = getPropPreviewSize(param.supportedPreviewSizes, mConfig.rate, mConfig
                    .minPreviewWidth)
            if (picSize != null && preSize != null) {
                param.setPictureSize(picSize!!.width, picSize!!.height)
                param.setPreviewSize(preSize!!.width, preSize!!.height)
                mCamera!!.parameters = param
                val pre = param.previewSize
                val pic = param.pictureSize
                mPicSize = Point(pic.height, pic.width)
                mPreSize = Point(pre.height, pre.width)
                return true
            } else {
                return false
            }
        }
    }

    override fun preview(): Boolean {
        mCamera?.let {
            mCamera!!.startPreview()
            return true
        }
        return false
    }

    override fun switchTo(cameraId: Int): Boolean {
        if (close()) {
            return open(cameraId)
        }
        return false
    }

    override fun takePhoto(callback: TakePhotoCallback?) {
    }

    override fun close(): Boolean {
        mCamera.let {
            try {
                mCamera!!.stopPreview()
                mCamera!!.release()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }

    override fun setPreviewTexture(texture: SurfaceTexture?) {
        mCamera.let {
            mCamera!!.setPreviewTexture(texture)
        }
    }

    override fun getPreviewSize(): Point? = mPreSize

    override fun getPictureSize(): Point? = mPicSize

    override fun setOnPreviewFrameCallback(callback: PreviewFrameCallback?) {
        mCamera.let {
            mCamera!!.setPreviewCallback { data, camera ->
                callback?.onPreviewFrame(data, if (mPreSize == null) 0 else mPreSize!!.x,
                        if (mPreSize == null) 0 else mPreSize!!.y)
            }
        }
    }

    private fun getPropPictureSize(list: List<Camera.Size>, th: Float, minWidth: Int): Camera.Size {
        Collections.sort(list, sizeComparator)

        var i = 0
        for (s in list) {
            if (s.height >= minWidth && equalRate(s, th)) {
                break
            }
            i++
        }
        if (i == list.size) {
            i = 0
        }
        return list[i]
    }

    /**
     * 获取适合的分辨率
     * */
    private fun getPropPreviewSize(list: List<Camera.Size>, th: Float,
            minWidth: Int): Camera.Size? {
        Collections.sort(list, sizeComparator)

        var i = 0
        for (s in list) {
            if (s.height >= minWidth && equalRate(s, th)) {
                break
            }
            i++
        }
        if (i == list.size) {
            i = 0
        }
        return list[i]
    }

    private fun equalRate(s: Camera.Size, rate: Float): Boolean {
        val r = s.width.toFloat() / s.height.toFloat()
        return Math.abs(r - rate) <= 0.03
    }

    private inner class CameraSizeComparator : Comparator<Camera.Size> {
        override fun compare(lhs: Camera.Size, rhs: Camera.Size): Int {
            // TODO Auto-generated method stub
            return if (lhs.height == rhs.height) {
                0
            } else if (lhs.height > rhs.height) {
                1
            } else {
                -1
            }
        }

    }

}