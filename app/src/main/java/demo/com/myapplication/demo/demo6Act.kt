package demo.com.myapplication.demo

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.opengl.GLSurfaceView.Renderer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.PersistableBundle
import android.support.annotation.RequiresApi
import android.support.annotation.RequiresPermission
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import demo.com.myapplication.R
import demo.com.myapplication.camera.CameraRender
import demo.com.myapplication.camera.FrameCallback
import demo.com.myapplication.camera.TextureController
import demo.com.myapplication.fliter.ZipPkmAnimationFilter
import demo.com.myapplication.utils.LogUtils
import demo.com.myapplication.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_camera2.mShutter
import kotlinx.android.synthetic.main.activity_camera2.mSurface
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Arrays
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin
 */
open class demo6Act : AppCompatActivity(), FrameCallback {

    companion object {
        val TAG = "demo6Act "
    }

    var cameraId = 1 // 0 后置摄像头
    private lateinit var mController: TextureController
    private var mRenderer: CameraRender? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)
        iniView()
        mShutter.setOnClickListener { v ->
            mController.takePhoto()
        }
    }

    fun iniView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mRenderer = Camera2Renderer()
        } else {
            mRenderer = Camera1Renderer()
        }
        mController = TextureController(this@demo6Act)
        onFilterSet(mController)
        mController.setFrameCallback(720, 1280, this@demo6Act)
        mSurface.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                LogUtils.log(" ACT  surfaceCreated ")
                mController.surfaceCreated(holder)
                mController.setRenderer(mRenderer!!)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int,
                    height: Int) {
                LogUtils.log(" ACT  surfaceChanged ")
                mController.surfaceChanged(width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mController.surfaceDestroyed()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (mController != null) {
            mController.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mController != null) {
            mController.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mController != null) {
            mController.destroy()
        }
    }

    protected open fun onFilterSet(controller: TextureController) {
        val mAniFilter = ZipPkmAnimationFilter(resources)
        mAniFilter.setAnimation("assets/etczip/cc.zip")
        controller.addFilter(mAniFilter)
    }

    override fun onFrame(bytes: ByteArray, time: Long) {
        Thread(Runnable {
            val bitmap = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888)
            val b = ByteBuffer.wrap(bytes)
            bitmap.copyPixelsFromBuffer(b)
            saveBitmap(bitmap)
            bitmap.recycle()
        }).start()
    }

    protected fun getSD(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }

    //图片保存
    fun saveBitmap(b: Bitmap) {
        val path = getSD() + "/OpenGLDemo/photo/"
        val folder = File(path)
        if (!folder.exists() && !folder.mkdirs()) {
            runOnUiThread {
                Toast.makeText(this@demo6Act, "无法保存照片", Toast.LENGTH_SHORT).show()
            }
            return
        }
        val dataTake = System.currentTimeMillis()
        val jpegName = "$path$dataTake.jpg"
        try {
            val fout = FileOutputStream(jpegName)
            val bos = BufferedOutputStream(fout)
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        runOnUiThread {
            Toast.makeText(this@demo6Act, "保存成功->$jpegName", Toast.LENGTH_SHORT).show()
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    inner class Camera2Renderer : CameraRender {

        var device: CameraDevice? = null
        var manager: CameraManager
        var thread: HandlerThread
        var hander: Handler
        var mPreviewSize: Size? = null

        init {
            manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            thread = HandlerThread("camera2")
            thread.start()
            hander = Handler(thread.looper)
        }

        override fun onDestroy() {
            device?.let {
                device!!.close()
                device = null
            }
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

            LogUtils.log(" Camera2Renderer onSurfaceCreated ")

            try {
                device?.let {
                    device!!.close()
                    device = null
                }
                val c = manager.getCameraCharacteristics(cameraId.toString())
                val map = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                // 自定义规则 选大小
                val sizes = map.getOutputSizes(SurfaceHolder::class.java)
                mPreviewSize = sizes[0]
                mController.setDataSize(mPreviewSize!!.height, mPreviewSize!!.width)
                var permission = ActivityCompat.checkSelfPermission(this@demo6Act,
                        android.Manifest.permission.CAMERA)
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    manager.openCamera(cameraId.toString(), object : CameraDevice.StateCallback() {
                        override fun onOpened(camera: CameraDevice?) {
                            device = camera
                            LogUtils.log(TAG, " open the camera")
                            try {
                                // 获取surface
                                val surface = Surface(mController
                                        .getTexture())
                                val builder = device!!.createCaptureRequest(TEMPLATE_PREVIEW)
                                // 添加到相机的 PREVIEW 中
                                builder.addTarget(surface)
                                mController.getTexture().setDefaultBufferSize(
                                        mPreviewSize!!.getWidth(), mPreviewSize!!.getHeight())
                                device!!.createCaptureSession(Arrays.asList(surface),
                                        object : CameraCaptureSession.StateCallback() {
                                            override fun onConfigured(
                                                    session: CameraCaptureSession) {
                                                try {
                                                    session.setRepeatingRequest(builder.build(),
                                                            object :
                                                                    CameraCaptureSession.CaptureCallback() {
                                                                override fun onCaptureProgressed(
                                                                        session: CameraCaptureSession,
                                                                        request: CaptureRequest,
                                                                        partialResult: CaptureResult) {
                                                                    super.onCaptureProgressed(
                                                                            session, request,
                                                                            partialResult)
                                                                }

                                                                override fun onCaptureCompleted(
                                                                        session: CameraCaptureSession,
                                                                        request: CaptureRequest,
                                                                        result: TotalCaptureResult) {
                                                                    super.onCaptureCompleted(
                                                                            session, request,
                                                                            result)
//                                                                    LogUtils.log(TAG, "camera  onCaptureCompleted")
                                                                    mController.requestRender()
                                                                }
                                                            }, hander)
                                                } catch (e: CameraAccessException) {
                                                    e.printStackTrace()
                                                }

                                            }

                                            override fun onConfigureFailed(
                                                    session: CameraCaptureSession) {

                                            }
                                        }, hander)
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }

                        }

                        override fun onDisconnected(camera: CameraDevice?) {
                            device = null
                        }

                        override fun onError(camera: CameraDevice?, error: Int) {
                        }

                    }, hander)
                }

            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e1: CameraAccessException) {
                e1.printStackTrace()
            }
        }

        override fun onDrawFrame(gl: GL10?) {
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        }
    }

    inner class Camera1Renderer : CameraRender {

        private var mCamera: Camera? = null

        override fun onDestroy() {
            mCamera?.let {
                mCamera!!.stopPreview()
                mCamera!!.release()
                mCamera = null
            }
        }

        override fun onDrawFrame(gl: GL10?) {
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            if (mCamera != null) {
                mCamera!!.stopPreview()
                mCamera!!.release()
                mCamera = null
            }
            mCamera = Camera.open(cameraId)
            mController.setImageDirection(cameraId)
            val size = mCamera!!.getParameters().previewSize
            mController.setDataSize(size.height, size.width)
            try {
                mCamera!!.setPreviewTexture(mController.getTexture())
                mController.getTexture().setOnFrameAvailableListener(
                        SurfaceTexture.OnFrameAvailableListener { mController.requestRender() })
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mCamera!!.startPreview()
        }

    }

}