package demo.com.myapplication.camera

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import java.util.jar.Attributes
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin
 */
class CameraVIew(context: Context, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs),
        GLSurfaceView.Renderer {

    var mCamera2: KCamera
    var mCameraDrawer: CameraDrawer
    var cameraId = 0
    var runnable: Runnable? = null

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        mCamera2 = KCamera()
        mCameraDrawer = CameraDrawer(resources)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mCameraDrawer.onSurfaceCreated(gl, config)
        runnable?.let {
            runnable!!.run()
            runnable = null
        }
        mCamera2.open(cameraId)
        mCameraDrawer.setCameraId(cameraId)
        val point = mCamera2.previewSize
        mCameraDrawer.setDataSize(point?.x ?: 0, point?.y ?: 0)
        mCamera2.setPreviewTexture(mCameraDrawer.getSurfaceTexture())
        mCameraDrawer.getSurfaceTexture()?.let {
            mCameraDrawer.getSurfaceTexture()!!.setOnFrameAvailableListener {
                requestRender()
            }
        }
        mCamera2.preview()
    }

    fun switchCamera() {
        runnable = Runnable {
            mCamera2.close()
            cameraId = if (cameraId == 1) 0 else 1
        }
//        mCamera2.switchTo(cameraId)
        onPause()
        onResume()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mCameraDrawer.setViewSize(width, height)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        mCameraDrawer.onDrawFrame(gl)
    }

    override fun onPause() {
        super.onPause()
        mCamera2.close()
    }

    override fun onResume() {
        super.onResume()
//        mCamera2.open(cameraId)
    }
}