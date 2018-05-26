package demo.com.myapplication.shape

import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.view.View
import demo.com.myapplication.utils.Gl2Utils
import demo.com.myapplication.utils.LogUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin
 */
class TriangleWithCamera(view: View) : Shape(view) {

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;" +
            "void main() {" +
            "  gl_Position = vMatrix*vPosition;" +
            "}"

    private val fragmentShaderCode = (
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}")

    lateinit var vertexBuffer: FloatBuffer
    var mProgrom = 0
    var mPositionHandle = 0
    var mColorHandle = 0
    var mMatrixHandler: Int = 0

    internal val COORDS_PER_VERTEX = 3
    internal var triangleCoords = floatArrayOf(0.5f, 0.5f, 0.0f, // top
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f  // bottom right
    )
    // 顶点数
    val vertexCount = triangleCoords.size / COORDS_PER_VERTEX
    // 每个顶点的stride 3*4 12字节
    val vertexStride = COORDS_PER_VERTEX * 4

    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)
    //设置颜色，依次为红绿蓝和透明通道
    internal var color = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // FloatBuffer
        vertexBuffer = Gl2Utils.transFloatBuffer(triangleCoords)
        // 创建顶点着色器
        var vertextShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        // 片元着色器
        var fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        // 创建GLES程序
        mProgrom = GLES20.glCreateProgram()
        // Attach shader
        GLES20.glAttachShader(mProgrom, vertextShader)
        GLES20.glAttachShader(mProgrom, fragmentShader)
        // link
        GLES20.glLinkProgram(mProgrom)
    }

    override fun onDrawFrame(gl: GL10?) {
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgrom)
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandler = GLES20.glGetUniformLocation(mProgrom, "vMatrix")
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0)
        // 获取vPosition句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgrom, "vPosition")
        // 启用vPositionHandle句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        // 准备坐标
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer)
        // 获取vColor句柄
        mColorHandle = GLES20.glGetUniformLocation(mProgrom, "vColor")
        // 绘制颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)
        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
        // 禁止顶点句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // 计算宽高比
        var ratio = width.toFloat() / height
        var radio2 = height.toFloat() / width

        LogUtils.log(" ratio : $ratio")
//        ratio = 1f
        // 设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        // 设置相机
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7f, 0f, 0f, 0f, 0f, 1f, .0f)
        // 计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

}