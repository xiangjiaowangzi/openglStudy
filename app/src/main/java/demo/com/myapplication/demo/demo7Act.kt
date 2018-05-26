package demo.com.myapplication.demo

import android.support.v7.widget.AppCompatSeekBar
import android.util.Log
import android.widget.SeekBar
import demo.com.myapplication.R
import demo.com.myapplication.camera.TextureController
import demo.com.myapplication.fliter.Beauty
import demo.com.myapplication.fliter.LookupFilter
import kotlinx.android.synthetic.main.activity_camera3.mSeek

/**
 * Created by LiuBin.
 */
class demo7Act : demo6Act(){

    private var mLookupFilter: LookupFilter? = null
    private var mBeautyFilter: Beauty? = null

    protected fun setContentView() {
        setContentView(R.layout.activity_camera3)
        mSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mLookupFilter!!.setIntensity(progress / 100f)
                mBeautyFilter!!.setFlag(progress / 20 + 1)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    override fun onFilterSet(controller: TextureController) {
        mLookupFilter = LookupFilter(resources)
        mLookupFilter!!.setMaskImage("lookup/purity.png")
        mLookupFilter!!.setIntensity(0.0f)
        controller.addFilter(mLookupFilter!!)
        mBeautyFilter = Beauty(resources)
        controller.addFilter(mBeautyFilter!!)
    }

}