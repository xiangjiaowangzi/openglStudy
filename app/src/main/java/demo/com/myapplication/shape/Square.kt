package demo.com.myapplication.shape

import android.opengl.GLES20
import android.opengl.Matrix
import android.view.View
import demo.com.myapplication.utils.Gl2Utils
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin
 */
class Square(view: View) : Shape(view) {

    lateinit var vertexBuffer: FloatBuffer
    lateinit var indexBuffer: ShortBuffer

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

    var mProgrom = 0
    var mPositionHandle = 0
    var mColorHandle = 0
    var mMatrixHandler = 0

    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)

    val COORDS_PER_VERTEX = 3
    var triangleCoords = floatArrayOf(-0.5f, 0.5f, 0.0f, // top left
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f, // bottom right
            0.5f, 0.5f, 0.0f  // top right
    )

    internal var index = shortArrayOf(1,2,3,3,0,1)

    //顶点个数
    private val vertexCount = triangleCoords.size / COORDS_PER_VERTEX
    //顶点之间的偏移量
    private val vertexStride = COORDS_PER_VERTEX * 4 // 每个顶点四个字节

    //设置颜色，依次为红绿蓝和透明通道
    internal var color = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

    override fun onDrawFrame(gl: GL10?) {
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgrom)
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandler = GLES20.glGetUniformLocation(mProgrom, "vMatrix")
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0)
        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgrom, "vPosition")
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer)
        //获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetUniformLocation(mProgrom, "vColor")
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)
        //绘制三角形
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
        //索引法绘制正方形
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.size, GLES20.GL_UNSIGNED_SHORT,
                indexBuffer)
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
        indexBuffer = Gl2Utils.transShortBuffer(index)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode)

        //创建一个空的OpenGLES程序
        mProgrom = GLES20.glCreateProgram()
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgrom, vertexShader)
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgrom, fragmentShader)
        //连接到着色器程序
        GLES20.glLinkProgram(mProgrom)
    }
}
