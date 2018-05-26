package demo.com.myapplication.camera

import android.content.Context
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Visibility
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import demo.com.myapplication.fliter.AFilter
import demo.com.myapplication.fliter.GroupFilter
import demo.com.myapplication.fliter.NoFilter
import demo.com.myapplication.utils.EasyGLUtils
import demo.com.myapplication.utils.LogUtils
import demo.com.myapplication.utils.MatrixUtils
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin
 *
 * 借助GLSurfaceView创建的GL环境，做渲染工作。不将内容渲染到GLSurfaceView
 * 的Surface上，而是将内容绘制到外部提供的Surface、SurfaceHolder或者SurfaceTexture上。
 *
 */
class TextureController(val mContext: Context) : GLSurfaceView.Renderer {

    private var surface: Any? = null

    private val mGLView: GLView

    //Filter
    private val mEffectFilter: TextureFilter                        //特效处理的Filter
    private val mGroupFilter: GroupFilter                           //中间特效
    private val mShowFilter: AFilter                                //用来渲染输出的Filter

    private var mRenderer: CameraRender? = null                                          //用户附加的Renderer或用来监听Renderer
    private val mDataSize: Point                                  //数据的大小
    private val mWindowSize: Point                                 //输出视图的大小

    private val isParamSet = AtomicBoolean(false)
    private var mShowType = MatrixUtils.TYPE_CENTERCROP          //输出到屏幕上的方式
    private var mDirectionFlag = -1                              //AiyaFilter方向flag

    private val SM = FloatArray(16)                           //用于绘制到屏幕上的变换矩阵
    private val callbackOM = FloatArray(16)                   //用于绘制回调缩放的矩阵

    //创建离屏buffer，用于最后导出数据
    private val mExportFrame = IntArray(1)
    private val mExportTexture = IntArray(1)

    private var isRecord = false                             //录像flag
    private var isShoot = false                              //一次拍摄flag

    private var outPutBuffer: Array<ByteBuffer?> = arrayOfNulls<ByteBuffer>(3)      //用于存储回调数据的buffer

    private var mFrameCallback: FrameCallback? = null                       //回调
    private var frameCallbackWidth: Int = 0                       //回调数据的宽高
    private var frameCallbackHeight: Int = 0                      //回调数据的宽高
    private var indexOutput = 0                                  //回调数据使用的buffer索引

    init {
        mGLView = GLView(mContext)
        //避免GLView的attachToWindow和detachFromWindow崩溃
        val parent = object:ViewGroup(mContext){
            override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            }
        }
        parent.addView(mGLView)
        parent.visibility = View.GONE

        mEffectFilter = TextureFilter(mContext.resources)
        mShowFilter = NoFilter(mContext.resources)
        mGroupFilter = GroupFilter(mContext.resources)

        //设置默认的DateSize，DataSize由AiyaProvider根据数据源的图像宽高进行设置
        mDataSize = Point(720, 1280)
        mWindowSize = Point(720, 1280)
    }

    fun surfaceCreated(nativeWindow: Any) {
        this.surface = nativeWindow
        mGLView.surfaceCreated(null)
    }

    fun surfaceChanged(width: Int, height: Int) {
        this.mWindowSize.x = width
        this.mWindowSize.y = height
        mGLView.surfaceChanged(null, 0, width, height)
    }

    fun surfaceDestroyed() {
        mGLView.surfaceDestroyed(null)
    }

    fun getOutput(): Any? {
        return surface
    }

    //在Surface创建前，应该被调用
    fun setDataSize(width: Int, height: Int) {
        mDataSize.x = width
        mDataSize.y = height
    }

    override fun onDrawFrame(gl: GL10?) {
//        LogUtils.log(" controller onDrawFrame ")

        if (isParamSet.get()) {
            mEffectFilter.draw()
            mGroupFilter.setTextureId(mEffectFilter.getOutputTexture())
            mGroupFilter.draw()

            //显示传入的texture上，一般是显示在屏幕上
            GLES20.glViewport(0, 0, mWindowSize.x, mWindowSize.y)
            mShowFilter.setMatrix(SM)
            mShowFilter.setTextureId(mGroupFilter.getOutputTexture())
            mShowFilter.draw()
            if (mRenderer != null) {
                mRenderer!!.onDrawFrame(gl)
            }
            callbackIfNeeded()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        LogUtils.log(" controller onSurfaceChanged ")

        MatrixUtils.getMatrix(SM, mShowType,
                mDataSize.x, mDataSize.y, width, height)
        mShowFilter.setSize(width, height)
        mShowFilter.setMatrix(SM)
        mGroupFilter.setSize(mDataSize.x, mDataSize.y)
        mEffectFilter.setSize(mDataSize.x, mDataSize.y)
        mShowFilter.setSize(mDataSize.x, mDataSize.y)
        if (mRenderer != null) {
            mRenderer!!.onSurfaceChanged(gl, width, height)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        LogUtils.log(" controller onSurfaceCreated ")

        mEffectFilter.create()
        mGroupFilter.create()
        mShowFilter.create()
        if (!isParamSet.get()) {
            if (mRenderer != null) {
                mRenderer!!.onSurfaceCreated(gl, config)
            }
            sdkParamSet()
        }
        calculateCallbackOM()
        mEffectFilter.setFlag(mDirectionFlag)

        deleteFrameBuffer()
        GLES20.glGenFramebuffers(1, mExportFrame, 0)
        EasyGLUtils.genTexturesWithParameter(1, mExportTexture, 0, GLES20.GL_RGBA, mDataSize.x,
                mDataSize.y)
    }

    private fun deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, mExportFrame, 0)
        GLES20.glDeleteTextures(1, mExportTexture, 0)
    }

    /**
     * 增加滤镜
     * @param filter 滤镜
     */
    fun addFilter(filter: AFilter) {
        mGroupFilter.addFilter(filter)
    }

    /**
     * 设置输入图像与输出视图大小不同时，图像的展示方式
     * @param type 展示方式，可选项为：
     * [MatrixUtils.TYPE_CENTERCROP]、[MatrixUtils.TYPE_CENTERINSIDE]、
     * [MatrixUtils.TYPE_FITEND]、[MatrixUtils.TYPE_FITSTART]、
     * [MatrixUtils.TYPE_FITXY]，与[ImageView.ScaleType]对应
     */
    fun setShowType(type: Int) {
        this.mShowType = type
        if (mWindowSize.x > 0 && mWindowSize.y > 0) {
            MatrixUtils.getMatrix(SM, mShowType,
                    mDataSize.x, mDataSize.y, mWindowSize.x, mWindowSize.y)
            mShowFilter.setMatrix(SM)
            mShowFilter.setSize(mWindowSize.x, mWindowSize.y)
        }
    }

    fun startRecord() {
        isRecord = true
    }

    fun stopRecord() {
        isRecord = false
    }

    fun takePhoto() {
        isShoot = true
    }

    fun getTexture(): SurfaceTexture {
        return mEffectFilter.getTexture()
    }

    fun setImageDirection(flag: Int) {
        this.mDirectionFlag = flag
    }

    fun setRenderer(renderer: CameraRender) {
        mRenderer = renderer
    }

    fun setFrameCallback(width: Int, height: Int, frameCallback: FrameCallback) {
        this.frameCallbackWidth = width
        this.frameCallbackHeight = height
        if (frameCallbackWidth > 0 && frameCallbackHeight > 0) {
//            if (outPutBuffer == null) {
//                outPutBuffer = arrayOfNulls(3)
//            }
            calculateCallbackOM()
            this.mFrameCallback = frameCallback
        } else {
            this.mFrameCallback = null
        }
    }

    private fun calculateCallbackOM() {
        if (frameCallbackHeight > 0 && frameCallbackWidth > 0 && mDataSize.x > 0 && mDataSize.y > 0) {
            //计算输出的变换矩阵
            MatrixUtils.getMatrix(callbackOM, MatrixUtils.TYPE_CENTERCROP, mDataSize.x, mDataSize.y,
                    frameCallbackWidth,
                    frameCallbackHeight)
            MatrixUtils.flip(callbackOM, false, true)
        }
    }

    fun getWindowSize(): Point {
        return mWindowSize
    }

    private fun sdkParamSet() {
        if (!isParamSet.get() && mDataSize.x > 0 && mDataSize.y > 0) {
            isParamSet.set(true)
        }
    }

    //需要回调，则缩放图片到指定大小，读取数据并回调
    private fun callbackIfNeeded() {
        if (mFrameCallback != null && (isRecord || isShoot)) {
            indexOutput = if (indexOutput++ >= 2) 0 else indexOutput
            if (outPutBuffer[indexOutput] == null) {
                outPutBuffer[indexOutput] = ByteBuffer.allocate(frameCallbackWidth *
                        frameCallbackHeight * 4)
            }
            GLES20.glViewport(0, 0, frameCallbackWidth, frameCallbackHeight)
            EasyGLUtils.bindFrameTexture(mExportFrame[0], mExportTexture[0])
            mShowFilter.setMatrix(callbackOM)
            mShowFilter.draw()
            frameCallback()
            isShoot = false
            EasyGLUtils.unBindFrameBuffer()
            mShowFilter.setMatrix(SM)
        }
    }

    //读取数据并回调
    private fun frameCallback() {
        GLES20.glReadPixels(0, 0, frameCallbackWidth, frameCallbackHeight,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, outPutBuffer[indexOutput])
        mFrameCallback?.onFrame(outPutBuffer[indexOutput]?.array(), 0)
    }

    fun create(width: Int, height: Int) {
        mGLView.attachedToWindow()
        surface?.let {
            surfaceCreated(surface!!)
            surfaceChanged(width, height)
        }
    }

    fun destroy() {
        if (mRenderer != null) {
            mRenderer!!.onDestroy()
        }
        mGLView.surfaceDestroyed(null)
        mGLView.detachedFromWindow()
        mGLView.clear()
    }

    fun requestRender() {
        mGLView.requestRender()
    }

    fun onPause() {
        mGLView.onPause()
    }

    fun onResume() {
        mGLView.onResume()
    }

    /** 自定义GLSurfaceView，暴露出onAttachedToWindow
     * 方法及onDetachedFromWindow方法，取消holder的默认监听
     * onAttachedToWindow及onDetachedFromWindow必须保证view
     * 存在Parent  */
    private inner class GLView(context: Context) : GLSurfaceView(context) {

        init {
            init()
        }

        private fun init() {
            LogUtils.log(" GLView init ")
            holder.addCallback(null)
            setEGLWindowSurfaceFactory(object : GLSurfaceView.EGLWindowSurfaceFactory {
                // 会在 surfacecreate 和 surfaccechange 后调用
                override fun createWindowSurface(egl: EGL10, display: EGLDisplay, config: EGLConfig,
                        window: Any): EGLSurface {
                    LogUtils.log(" GLView createWindowSurface ")
                    return egl.eglCreateWindowSurface(display, config, surface, null)
                }

                override fun destroySurface(egl: EGL10, display: EGLDisplay, surface: EGLSurface) {
                    egl.eglDestroySurface(display, surface)
                }
            })
            setEGLContextClientVersion(2)
            setRenderer(this@TextureController)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            preserveEGLContextOnPause = true
        }

        fun attachedToWindow() {
            super.onAttachedToWindow()
        }

        fun detachedFromWindow() {
            super.onDetachedFromWindow()
        }

        fun clear() {
            //            try {
            //                finalize();
            //            } catch (Throwable throwable) {
            //                throwable.printStackTrace();
            //            }
        }

    }
}