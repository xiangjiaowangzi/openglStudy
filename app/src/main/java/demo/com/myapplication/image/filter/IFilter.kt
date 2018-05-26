package demo.com.myapplication.image.filter

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import demo.com.myapplication.utils.Gl2Utils
import demo.com.myapplication.utils.LogUtils
import demo.com.myapplication.utils.ShaderUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin
 */
abstract class IFilter(context: Context, vertex: String, fragment: String) :
        GLSurfaceView.Renderer {

    var mContext: Context
    var vertex: String
    var fragment: String

    protected var mProgram: Int = 0
    private var glHPosition: Int = 0
    private var glHTexture: Int = 0
    private var glHCoordinate: Int = 0
    private var glHMatrix: Int = 0
    private var hIsHalf: Int = 0
    private var glHUxy: Int = 0
    private var textureId: Int = 0

    lateinit var bPos: FloatBuffer
    lateinit var bCoord: FloatBuffer

    var isHalf = false
    private var uXY: Float = 0.toFloat()

    var mBitmap: Bitmap? = null
        private set

    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)

    private val sPos = floatArrayOf(-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f)
    private val sCoord = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f)

    init {
        this.mContext = context
        this.vertex = vertex
        this.fragment = fragment
        onInit()
    }

    fun onInit() {
        bPos = Gl2Utils.transFloatBuffer(sPos)
        bCoord = Gl2Utils.transFloatBuffer(sCoord)
    }

    fun setBitmap(bitmap: Bitmap) {
        this.mBitmap = bitmap
    }

    fun setImageBuffer(buffer: IntArray, width: Int, height: Int) {
        mBitmap = Bitmap.createBitmap(buffer, width, height, Bitmap.Config.RGB_565)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        // 启用纹理
        GLES20.glEnable(GLES20.GL_TEXTURE20)
        mProgram = ShaderUtils.createProgram(mContext.resources, vertex, fragment)
        glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition")
        glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate")
        glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
        glHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        hIsHalf = GLES20.glGetUniformLocation(mProgram, "vIsHalf")
        glHUxy = GLES20.glGetUniformLocation(mProgram, "uXY")
        onDrawCreatedSet(mProgram)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val w = mBitmap?.width ?: 0
        val h = mBitmap?.height ?: 0
        val sWH = w / h.toFloat()
        val sViewWH = width / height.toFloat()
        uXY = sViewWH
        LogUtils.log(" sViewWH:$sViewWH , sWH:$sWH")
        LogUtils.log(" width:$width , height:$height")
        // 这个判断估计会有问题,以后需要实际上去调试
        if (width > height) {
            if (sWH > sViewWH) {
                Matrix.orthoM(mProjectMatrix, 0, -sViewWH * sWH, sViewWH * sWH, -1f, 1f,
                        3f, 5f)
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sViewWH / sWH, sViewWH / sWH, -1f, 1f,
                        3f, 5f)
            }
        } else {
            if (sWH > sViewWH) {
                Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -1 / sViewWH * sWH,
                        1 / sViewWH * sWH, 3f, 5f)
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWH / sViewWH, sWH / sViewWH, -1f, 1f,
                        3f, 5f)
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 5.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    abstract fun onDrawSet()
    abstract fun onDrawCreatedSet(program: Int)

    override fun onDrawFrame(gl: GL10?) {
        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        onDrawSet()
        GLES20.glUniform1i(hIsHalf, if (isHalf) 1 else 0)
        GLES20.glUniform1f(glHUxy, uXY)
        GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0)
        GLES20.glEnableVertexAttribArray(glHPosition)
        GLES20.glEnableVertexAttribArray(glHCoordinate)
        GLES20.glUniform1i(glHTexture, 0)
        textureId = createTexture()
        GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos)
        GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoord)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    /**
     * 创建2D纹理
     * */

    // GL_NEAREST和GL_LINEAR
    //前者表示“使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色”，
    // 后者表示“使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色”。
    // 前者只经过简单比较，需要运算较少，可能速度较快，
    // 从视觉效果上看，前者效果较差，在一些情况下锯齿现象明显，后者效果会较好
    fun createTexture(): Int {
        val texture = IntArray(1)
        mBitmap?.let {
            if (!mBitmap!!.isRecycled) {
                // 生成纹理
                GLES20.glGenTextures(1, texture, 0)
                // 绑定纹理
                GLES20.glBindTexture(GLES20.GL_TEXTURE20, texture[0])
                //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                        GLES20.GL_NEAREST.toFloat())
                //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                        GLES20.GL_LINEAR.toFloat())
                //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                        GLES20.GL_CLAMP_TO_EDGE.toFloat())
                //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                        GLES20.GL_CLAMP_TO_EDGE.toFloat())
                //根据以上指定的参数，生成一个2D纹理
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
                return texture[0]
            }
        }
        return 0
    }
}