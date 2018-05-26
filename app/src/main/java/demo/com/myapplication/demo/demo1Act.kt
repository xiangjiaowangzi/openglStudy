package demo.com.myapplication.demo

import android.app.Activity
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.opengl.GLU
import android.opengl.Matrix
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import demo.com.myapplication.utils.LogUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.Matrix.perspectiveM

/**
 * Created by LiuBin
 */
class demo1Act : Activity() {

//    加载顶点和片元着色器
//    确定需要绘制图形的坐标和颜色数据
//    创建program对象，连接顶点和片元着色器，链接program对象。
//    设置视图窗口(viewport)。
//    将坐标数据颜色数据传入OpenGL ES程序中
//    使颜色缓冲区的内容显示到屏幕上。

    lateinit var glSurfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2) // 2.0
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        glSurfaceView.setRenderer(OpenGlRender())
        // render 渲染模式 不断被渲染 RENDERMODE_CONTINUOUSLY ： 当创建表面时 渲染 RENDERMODE_WHEN_DIRTY
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        setContentView(glSurfaceView)
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    class OpenGlRender : Renderer {

        private var mProgram = 0
        private var mPositionHandle: Int = 0
        private var mMatrixHandle: Int = 0

        private val mMVPMatrix = FloatArray(16)

        lateinit var mVertexBuffer: FloatBuffer

        init {
            mVertexBuffer = transFloatBuffer(VERTEX)
        }

        companion object {
            // 顶点着色器
            val VERTEX_SHADER = (
                    "attribute vec4 vPosition;\n"
                            + "uniform mat4 uMVPMatrix;\n"
                            + "void main() {\n"
                            + "  gl_Position = uMVPMatrix * vPosition;\n"
                            + "}")

//            val VERTEX_SHADER = (
//                    " attribute vec4 vPosition;\n" +
//                            " void main() {\n" +
//                            "     gl_Position = vPosition;\n" +
//                            " }")

            // 片元着色器
            val FRAGMENT_SHADER = (
                    "precision mediump float;\n"
                            + "void main() {\n"
                            + "  gl_FragColor = vec4(0.5, 0, 0, 1);\n"
                            + "}")

//            val FRAGMENT_SHADER = (
//                    "precision mediump float;\n" +
//                            " uniform vec4 vColor;\n" +
//                            " void main() {\n" +
//                            "     gl_FragColor = vColor;\n" +
//                            " }")

            val VERTEX = floatArrayOf(// in counterclockwise order:
                    0f, 1f, 0f, // top
                    -0.5f, -1f, 0f, // bottom left
                    1f, -1f, 0f)// bottom right
        }

        fun loadShader(type: Int, shaderCode: String): Int {
            var shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }

        var color = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f) //白色

        override fun onDrawFrame(gl: GL10?) {
//            LogUtils.log("onDrawFrame")

            //将程序加入到OpenGLES2.0环境
            GLES20.glUseProgram(mProgram)
            //获取顶点着色器的vPosition成员句柄
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

//            LogUtils.log(" mPositionHandle : $mPositionHandle ")
//            LogUtils.log(" mProgram : $mProgram ")

            //启用三角形顶点的句柄
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            //准备三角形的坐标数据
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    12, mVertexBuffer)

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            //将 mMVPMatrix 内容 设置三角形 mMatrixHandle
            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0)
            //绘制三角形
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
            //禁止顶点数组的句柄
            GLES20.glDisableVertexAttribArray(mPositionHandle)

//            //将程序加入到OpenGLES2.0环境
//            GLES20.glUseProgram(mProgram)
//            //获取顶点着色器的vPosition成员句柄
//            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
//
//            LogUtils.log(" mPositionHandle : $mPositionHandle ")
//            LogUtils.log(" mProgram : $mProgram ")
//
//            //启用三角形顶点的句柄
//            GLES20.glEnableVertexAttribArray(mPositionHandle)
//            //准备三角形的坐标数据
//            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
//                    12, mVertexBuffer)
////            //获取片元着色器的vColor成员的句柄
////            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
////            //设置绘制三角形的颜色
////            GLES20.glUniform4fv(mMatrixHandle, 1, color, 0);
//            //绘制三角形
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
//            //禁止顶点数组的句柄
//            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
//            // 设置大小
            GLES20.glViewport(0, 0, width, height)
//          这会用45度的视野创建一个透视投影。 这个视椎体从Z值为-1的位置开始，在Z值为-10的位置结束。
            Matrix.perspectiveM(mMVPMatrix, 0, 45f, width.toFloat() / height, 0.1f, 100f)
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -2.5f)
        }

        /**
         * 刚创建时刻调用
         * */
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            // OpenGL 需要加载 GLSL 程序，让 GPU 进行绘制

            // 背景色
            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
            // 程序点
            mProgram = GLES20.glCreateProgram()
            // 获取share器
            var vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
            // attach GLSL程序点
            GLES20.glAttachShader(mProgram, vertexShader)
            GLES20.glAttachShader(mProgram, fragmentShader)
            //link GLSL程序点
            GLES20.glLinkProgram(mProgram)
//            //将程序加入到OpenGLES2.0环境
//            GLES20.glUseProgram(mProgram)
//            //获取顶点着色器的vPosition成员句柄
//            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
//
//            LogUtils.log(" mPositionHandle : $mPositionHandle ")
//            LogUtils.log(" mProgram : $mProgram ")
//
//            //启用三角形顶点的句柄
//            GLES20.glEnableVertexAttribArray(mPositionHandle)
//            //准备三角形的坐标数据
//            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
//                    12, mVertexBuffer)
        }

        /**
         * 正方形4个点
         * */
        private val vertices = floatArrayOf(
                -1.0f, 1.0f, 0.0f, // 0, Top Left
                -1.0f, -1.0f, 0.0f, // 1, Bottom Left
                1.0f, -1.0f, 0.0f, // 2, Bottom Right
                1.0f, 1.0f, 0.0f // 3, Top Right
        )

        /**
         * 为了提高性能,通常将这些数组存放到 java.io 中定义的 Buffer 类中:
         * */
        fun transFloatBuffer(vertices: FloatArray): FloatBuffer {
            val vbb = ByteBuffer.allocateDirect(vertices.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(vertices)
            vbb.position(0)
            return vbb
        }

    }

}