package demo.com.myapplication.shape

import android.opengl.GLES20
import android.opengl.Matrix
import android.view.View
import demo.com.myapplication.utils.Gl2Utils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin
 */
class TriangleColorFull(view: View) : Shape(view) {

    lateinit var vertexBuffer: FloatBuffer
    lateinit var colorBuffer: FloatBuffer

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;" +
            "varying  vec4 vColor;" +
            "attribute vec4 aColor;" +
            "void main() {" +
            "  gl_Position = vMatrix*vPosition;" +
            "  vColor=aColor;" +
            "}"

    private val fragmentShaderCode = (
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}")

    var mProgram = 0
    var mPositionHandle = 0
    var mColorHandle = 0
    var mMatrixHandler: Int = 0

    val COORDS_PER_VERTEX = 3
    val triangleCoords = floatArrayOf(0.5f, 0.5f, 0.0f, // top
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f  // bottom right
    )

    //设置颜色
    var color = floatArrayOf(0.0f, 0.6f, 0.0f, 0.7f, 1.0f, 0.0f, 0.2f, 1.0f, 0.0f, 0.0f,
            0.8f, 0.3f)

    // 顶点数
    val vertexCount = triangleCoords.size / COORDS_PER_VERTEX
    // 每个顶点的stride 3*4 12字节
    val vertexStride = COORDS_PER_VERTEX * 4

    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)

    init {

    }

    override fun onDrawFrame(gl: GL10?) {
        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(mProgram)
        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0)
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer)
        mColorHandle = GLES20.glGetAttribLocation(mProgram,"aColor")
        //设置绘制三角形的颜色
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glVertexAttribPointer(mColorHandle, 4,
                GLES20.GL_FLOAT, false,
                16, colorBuffer)
        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //计算宽高比
        val ratio = width.toFloat() / height
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        vertexBuffer = Gl2Utils.transFloatBuffer(triangleCoords)
        colorBuffer = Gl2Utils.transFloatBuffer(color)
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentSHader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentSHader)
        GLES20.glLinkProgram(mProgram)
    }

}