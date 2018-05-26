package demo.com.myapplication

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import demo.com.myapplication.demo.demo1Act
import demo.com.myapplication.demo.demo2Act
import demo.com.myapplication.demo.demo3Act
import demo.com.myapplication.demo.demo4Act
import demo.com.myapplication.demo.demo5Act
import demo.com.myapplication.demo.demo6Act
import demo.com.myapplication.demo.demo7Act
import demo.com.myapplication.demo.demo8Act
import demo.com.myapplication.utils.LogUtils
import demo.com.myapplication.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_main.btn1
import kotlinx.android.synthetic.main.activity_main.btn2
import kotlinx.android.synthetic.main.activity_main.btn3
import kotlinx.android.synthetic.main.activity_main.btn4
import kotlinx.android.synthetic.main.activity_main.btn5
import kotlinx.android.synthetic.main.activity_main.btn6
import kotlinx.android.synthetic.main.activity_main.btn7
import kotlinx.android.synthetic.main.activity_main.btn8
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissin()
        btn1.setOnClickListener {
            start(demo1Act::class.java)
        }
        btn2.setOnClickListener {
            start(demo2Act::class.java)
        }
        btn3.setOnClickListener {
            start(demo3Act::class.java)
        }
        btn4.setOnClickListener {
            start(demo4Act::class.java)
        }
        btn5.setOnClickListener {
            start(demo5Act::class.java)
        }
        btn6.setOnClickListener {
            start(demo6Act::class.java)
        }
        btn7.setOnClickListener {
            start(demo7Act::class.java)
        }
        btn8.setOnClickListener {
            start(demo8Act::class.java)
        }
    }

    fun start(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }

    fun requestPermissin() {
        PermissionUtils.checkAndRequestMorePermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest
                .permission.WRITE_EXTERNAL_STORAGE), 0)
    }
}
