package demo.com.myapplication.shape

import android.opengl.GLES20
import android.opengl.Matrix
import android.view.View
import demo.com.myapplication.utils.Gl2Utils
import demo.com.myapplication.utils.ShaderUtils
import java.nio.FloatBuffer
import java.util.ArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by LiuBin
 */
class Cone(view: View) : Shape(view) {

    var mProgram = 0
    lateinit var vertexBuffer: FloatBuffer
    lateinit var oval: Oval

    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)

    private val n = 360  //切割份数
    private val height = 2.0f  //圆锥高度
    private val radius = 1.0f  //圆锥底面半径

    private val colors = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

    var vSize = 0

    init {
        oval = Oval(mView)
        val pos = ArrayList<Float>()
        pos.add(0.0f)
        pos.add(0.0f)
        pos.add(height)
        val angDegSpan = 360f / n
        run {
            var i = 0f
            while (i < 360 + angDegSpan) {
                pos.add((radius * Math.sin(i * Math.PI / 180f)).toFloat())
                pos.add((radius * Math.cos(i * Math.PI / 180f)).toFloat())
                pos.add(0.0f)
                i += angDegSpan
            }
        }
        val d = FloatArray(pos.size)
        for (i in d.indices) {
            d[i] = pos[i]
        }
        vSize = d.size / 3
        vertexBuffer = Gl2Utils.transFloatBuffer(d)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glUseProgram(mProgram)
        val mMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        GLES20.glUniformMatrix4fv(mMatrix, 1, false, mMVPMatrix, 0)
        val mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vSize)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        oval.setMatrix(mMVPMatrix)
        oval.onDrawFrame(gl)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //计算宽高比
        val ratio = width.toFloat() / height
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 20f)
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 1.0f, -10.0f, -4.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        mProgram = ShaderUtils.createProgram(mView.resources, "vshader/Cone.sh", "fshader/Cone.sh")
        oval.onSurfaceCreated(gl, config)
    }

}