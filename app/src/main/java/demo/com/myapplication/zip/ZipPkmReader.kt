package demo.com.myapplication.zip

import android.content.Context
import android.content.res.AssetManager
import android.opengl.ETC1
import android.opengl.ETC1Util
import demo.com.myapplication.utils.LogUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Created by LiuBin.
 */
class ZipPkmReader(private val mManager: AssetManager) {

    private var path: String? = null
    private var mZipStream: ZipInputStream? = null
    private var mZipEntry: ZipEntry? = null
    private var headerBuffer: ByteBuffer? = null

    val nextStream: InputStream?
        get() = if (hasElements()) {
            mZipStream
        } else null

    val nextTexture: ETC1Util.ETC1Texture?
        get() {
            if (hasElements()) {
                try {
                    return createTexture(mZipStream!!)
                } catch (e1: IOException) {
                    LogUtils.log("err->" + e1.message)
                    e1.printStackTrace()
                }

            }
            return null
        }

    constructor(context: Context) : this(context.assets) {}

    fun setZipPath(path: String) {
        LogUtils.log( "$path set")
        this.path = path
    }

    fun open(): Boolean {
        LogUtils.log(path!! + " open")
        if (path == null) return false
        try {
            if (path!!.startsWith("assets/")) {
                val s = mManager.open(path!!.substring(7))
                mZipStream = ZipInputStream(s)
            } else {
                val f = File(path!!)
                LogUtils.log( path + " is File exists->" + f.exists())
                mZipStream = ZipInputStream(FileInputStream(path!!))
            }
            return true
        } catch (e: IOException) {
            LogUtils.log( "eee-->" + e.message)
            e.printStackTrace()
            return false
        }

    }

    fun close() {
        if (mZipStream != null) {
            try {
                mZipStream!!.closeEntry()
                mZipStream!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (headerBuffer != null) {
                headerBuffer!!.clear()
                headerBuffer = null
            }
        }
    }

    private fun hasElements(): Boolean {
        try {
            if (mZipStream != null) {
                mZipEntry = mZipStream!!.nextEntry
                if (mZipEntry != null) {
                    return true
                }
                LogUtils.log( "mZip entry null")
            }
        } catch (e: IOException) {
            LogUtils.log( "err  dd->" + e.message)
            e.printStackTrace()
        }

        return false
    }

    @Throws(IOException::class)
    private fun createTexture(input: InputStream): ETC1Util.ETC1Texture {
        var width = 0
        var height = 0
        val ioBuffer = ByteArray(4096)
        run {
            if (input.read(ioBuffer, 0, ETC1.ETC_PKM_HEADER_SIZE) != ETC1.ETC_PKM_HEADER_SIZE) {
                throw IOException("Unable to read PKM file header.")
            }
            if (headerBuffer == null) {
                headerBuffer = ByteBuffer.allocateDirect(ETC1.ETC_PKM_HEADER_SIZE)
                        .order(ByteOrder.nativeOrder())
            }
            headerBuffer!!.put(ioBuffer, 0, ETC1.ETC_PKM_HEADER_SIZE).position(0)
            if (!ETC1.isValid(headerBuffer)) {
                throw IOException("Not a PKM file.")
            }
            width = ETC1.getWidth(headerBuffer)
            height = ETC1.getHeight(headerBuffer)
        }
        val encodedSize = ETC1.getEncodedDataSize(width, height)
        val dataBuffer = ByteBuffer.allocateDirect(encodedSize).order(ByteOrder.nativeOrder())
        var len = input.read(ioBuffer)
        while ((len) != -1) {
            dataBuffer.put(ioBuffer, 0, len)
            len = input.read(ioBuffer)
        }
        dataBuffer.position(0)
        return ETC1Util.ETC1Texture(width, height, dataBuffer)
    }

}