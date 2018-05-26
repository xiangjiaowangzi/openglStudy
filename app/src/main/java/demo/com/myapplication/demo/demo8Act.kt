package demo.com.myapplication.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import demo.com.myapplication.R
import demo.com.myapplication.utils.Gl2Utils
import demo.com.myapplication.zip.StateChangeListener
import demo.com.myapplication.zip.StateChangeListener.STOP
import kotlinx.android.synthetic.main.activity_zip.mAni

/**
 * Created by LiuBin.
 */
class demo8Act : AppCompatActivity() {

    private val nowMenu = "assets/etczip/cc.zip"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zip)
        mAni.setScaleType(Gl2Utils.TYPE_CENTERINSIDE)
        mAni.setOnClickListener(View.OnClickListener {
            if (!mAni.isPlay) {
                mAni.setAnimation(nowMenu, 50)
                mAni.start()
            }
        })
        mAni.setStateChangeListener(object : StateChangeListener {
            override fun onStateChanged(lastState: Int, nowState: Int) {
                if (nowState == STOP) {
                    if (!mAni.isPlay) {
                        mAni.setAnimation(nowMenu, 50)
                        mAni.start()
                    }
                }
            }
        })
    }
}
