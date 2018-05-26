package demo.com.myapplication.fliter

import android.content.res.Resources
import android.opengl.GLES20
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import demo.com.myapplication.utils.LogUtils
import demo.com.myapplication.utils.MatrixUtils
import demo.com.myapplication.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.Arrays

/**
 * Created by LiuBin
 */
abstract class AFilter : Object{
    protected lateinit var mRes: Resources
    protected var mFlag = 0

    // 程序句柄
    protected var mProgram = 0
    // 顶点坐标句柄
    protected var mHPosition: Int = 0
    // 纹理句柄
    protected var mHCoord: Int = 0
    // 总变换矩阵句柄
    protected var mHMatrix: Int = 0
    // 默认纹理贴图句柄
    protected var mHTexture: Int = 0

    // 顶点坐标Buffer
    protected lateinit var mVerBuffer: FloatBuffer
    //纹理坐标Buffer
    protected lateinit var mTexBuffer: FloatBuffer
    // 索引坐标Buffe
    protected var mindexBuffer: ShortBuffer? = null

    private var matrix = Arrays.copyOf(OM, 16)
    private var textureType = 0      //默认使用Texture2D0
    private var textureId = 0

    // 顶点坐标
    private val pos = floatArrayOf(-1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f)

    //纹理坐标
    // movie
    private val coord = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f)
//    // back
//    val coord = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f)
    // font
//    val coord = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f)

    private var mBools: SparseArray<BooleanArray>? = null
    private var mInts: SparseArray<IntArray>? = null
    private var mFloats: SparseArray<FloatArray>? = null

    constructor(mRes: Resources) {
        this.mRes = mRes
        initBuffer()
    }

    fun create() {
        onCreate()
    }

    fun setSize(width: Int, height: Int) {
        onSizeChanged(width, height)
    }

    open fun draw() {
        onClear()
        onUseProgram()
        onSetExpandData()
        onBindTexture()
        onDraw()
    }

    open fun setMatrix(matrix: FloatArray) {
        this.matrix = matrix
    }

    open fun getMatrix(): FloatArray {
        return matrix
    }

    fun setTextureType(type: Int) {
        this.textureType = type
    }

    fun getTextureType(): Int {
        return textureType
    }

    fun getTextureId(): Int {
        return textureId
    }

    open fun setTextureId(textureId: Int) {
        this.textureId = textureId
    }

    open fun setFlag(flag: Int) {
        this.mFlag = flag
    }

    fun getFlag(): Int {
        return mFlag
    }

    fun setFloat(type: Int, vararg params: Float) {
        if (mFloats == null) {
            mFloats = SparseArray()
        }
        mFloats!!.put(type, params)
    }

    open fun setInt(type: Int, vararg params: Int) {
        if (mInts == null) {
            mInts = SparseArray()
        }
        mInts!!.put(type, params)
    }

    fun setBool(type: Int, vararg params: Boolean) {
        if (mBools == null) {
            mBools = SparseArray()
        }
        mBools!!.put(type, params)
    }

    fun getBool(type: Int, index: Int): Boolean {
        if (mBools == null) return false
        val b = mBools!!.get(type)
        return !(b == null || b.size <= index) && b[index]
    }

    fun getInt(type: Int, index: Int): Int {
        if (mInts == null) return 0
        val b = mInts!!.get(type)
        return if (b == null || b.size <= index) {
            0
        } else b[index]
    }

    fun getFloat(type: Int, index: Int): Float {
        if (mFloats == null) return 0f
        val b = mFloats!!.get(type)
        return if (b == null || b.size <= index) {
            0f
        } else b[index]
    }

    open fun getOutputTexture(): Int {
        return -1
    }

    /**
     * 实现此方法，完成程序的创建，可直接调用createProgram来实现
     */
    protected abstract fun onCreate()

    abstract fun onSizeChanged(width: Int, height: Int)

    protected fun createProgramByAssetsFile(vertex: String, fragment: String) {
        createProgram(uRes(mRes,vertex),uRes(mRes,fragment))
    }

    /**
     * 创建GL程序
     * */
    protected fun createProgram(vertex: String?, fragment: String?) {
        if (TextUtils.isEmpty(vertex) || TextUtils.isEmpty(fragment)) return
        mProgram = uCreateGlProgram(vertex!!, fragment!!)
        mHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mHCoord = GLES20.glGetAttribLocation(mProgram, "vCoord")
        mHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        mHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
    }

    protected open fun initBuffer() {
        mVerBuffer = transBuffer(pos)
        mTexBuffer = transBuffer(coord)
    }

    protected fun onUseProgram() {
        GLES20.glUseProgram(mProgram)
    }

    private fun onDraw() {
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, 2, GLES20.GL_FLOAT, false, 0, mVerBuffer)
        GLES20.glEnableVertexAttribArray(mHCoord)
        GLES20.glVertexAttribPointer(mHCoord, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mHPosition)
        GLES20.glDisableVertexAttribArray(mHCoord)
    }

    /**
     * 清除画布
     * */
    open fun onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    /**
     * 设置其他扩展数据
     */
    open fun onSetExpandData() {
        GLES20.glUniformMatrix4fv(mHMatrix, 1, false, matrix, 0)
    }

    /**
     * 绑定默认纹理
     */
    open fun onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId())
        GLES20.glUniform1i(mHTexture, textureType)
    }

    /**
     * 为了提高性能,通常将这些数组存放到 java.io 中定义的 Buffer 类中:
     * */
    open fun transBuffer(vertices: FloatArray): FloatBuffer {
        val vbb = ByteBuffer.allocateDirect(vertices.size * 4)
        vbb.order(ByteOrder.nativeOrder())
        val vertexBuffer = vbb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)
        return vertexBuffer
    }

    companion object {

        private val TAG = "Filter"

        val KEY_OUT = 0x101
        val KEY_IN = 0x102
        val KEY_INDEX = 0x201

        var DEBUG = true
        /**
         * 单位矩阵
         */
        val OM = MatrixUtils.getOriginalMatrix()

        fun glError(code: Int, index: Any) {
            if (DEBUG && code != 0) {
                Log.e(TAG, "glError:$code---$index")
            }
        }

        //通过路径加载Assets中的文本内容
        fun uRes(mRes: Resources, path: String): String? {
            val result = StringBuilder()
            try {
                val inputStream = mRes.assets.open(path)
                var ch: Int
                val buffer = ByteArray(1024)
                ch = inputStream.read(buffer)
                while (-1 != (ch)) {
                    result.append(String(buffer, 0, ch))
                    ch = inputStream.read(buffer)
                }
            } catch (e: Exception) {
                return ""
            }

            return result.toString().replace("\\r\\n".toRegex(), "\n")
        }

        //创建GL程序
        fun uCreateGlProgram(vertexSource: String?, fragmentSource: String?): Int {
            val vertex = uLoadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
            if (vertex == 0) return 0
            val fragment = uLoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
            if (fragment == 0) return 0
            var program = GLES20.glCreateProgram()
            if (program != 0) {
                GLES20.glAttachShader(program, vertex)
                GLES20.glAttachShader(program, fragment)
                GLES20.glLinkProgram(program)
                val linkStatus = IntArray(1)
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
                if (linkStatus[0] != GLES20.GL_TRUE) {
                    glError(1, "Could not link program:" + GLES20.glGetProgramInfoLog(program))
                    GLES20.glDeleteProgram(program)
                    program = 0
                }
            }
            return program
        }

        //加载shader
        fun uLoadShader(shaderType: Int, source: String?): Int {
            var shader = GLES20.glCreateShader(shaderType)
            if (0 != shader) {
                GLES20.glShaderSource(shader, source)
                GLES20.glCompileShader(shader)
                val compiled = IntArray(1)
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
                if (compiled[0] == 0) {
                    glError(1, "Could not compile shader:$shaderType")
                    glError(1, "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader))
                    GLES20.glDeleteShader(shader)
                    shader = 0
                }
            }
            return shader
        }
    }
}
