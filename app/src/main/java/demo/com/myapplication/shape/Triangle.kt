package demo.com.myapplication.shape

import android.opengl.GLES20
import android.view.View
import demo.com.myapplication.utils.Gl2Utils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin
 */
class Triangle(view: View) : Shape(view) {

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "}"

    private val fragmentShaderCode = (
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}")

    lateinit var vertexBuffer: FloatBuffer
    var mProgram = 0
    // 3维一个点
    val COORDS_PER_VERTEX = 3
    // 3个点
    val triangleCoords = floatArrayOf(
            0.5f, 0.5f, 1.0f, // top
            -0.5f, -0.5f, 0.5f, // bottom left
            0.5f, -0.5f, 0.8f  // bottom right
    )

    // 连接三角程序的点
    var mPositionHandle = 0
    var mColorHandle = 0
    var mMatrixHandler = 0

    //顶点个数
    private val vertexCount = triangleCoords.size / COORDS_PER_VERTEX
    //顶点之间的偏移量,顶点由（3个点组成）
    private val vertexStride = COORDS_PER_VERTEX * 4 // 每个点四个字节
    //设置颜色，依次为红绿蓝和透明通道
    var color = floatArrayOf(0.5f, 0.5f, 1.0f, 1.0f)

    init {
        // 顶点floatbuffer
        vertexBuffer = Gl2Utils.transFloatBuffer(triangleCoords)

        // 加载 shade
        var vertexShade = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        var fragmentShade = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        // 创建GLES程序
        mProgram = GLES20.glCreateProgram()
        // 添加渲染器
        GLES20.glAttachShader(mProgram, vertexShade)
        GLES20.glAttachShader(mProgram, fragmentShade)
        // 连接程序
        GLES20.glLinkProgram(mProgram)
    }

    override fun onDrawFrame(gl: GL10?) {
        // 将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram)
        // 获取顶点着色器句柄,与 vertexShaderCode 定义的保持一致
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        //启用顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        // 准备数据
        //
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer)
        // 获取片元着色器的vCOlor的句柄
        mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor")
        // 设置三角颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)
        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,vertexCount)
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    }

}