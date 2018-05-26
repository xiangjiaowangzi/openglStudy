package demo.com.myapplication.demo

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import demo.com.myapplication.R
import demo.com.myapplication.image.filter.ColorFilter
import demo.com.myapplication.image.filter.ColorFilter.Filter.NONE
import demo.com.myapplication.image.filter.ContrastColorFilter
import kotlinx.android.synthetic.main.activity_picture.glView

/**
 * Created by LiuBin
 */
class demo3Act: AppCompatActivity(){

    private var isHalf = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
    }


    override fun onResume() {
        super.onResume()
        glView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mDeal -> {
                isHalf = !isHalf
                if (isHalf) {
                    item.title = "处理一半"
                } else {
                    item.title = "全部处理"
                }
                glView.render?.refresh()
            }
            R.id.mDefault -> glView.setFilter(ContrastColorFilter(this, NONE))
            R.id.mGray -> glView.setFilter(ContrastColorFilter(this, ColorFilter.Filter.GRAY))
            R.id.mCool -> glView.setFilter(ContrastColorFilter(this, ColorFilter.Filter.COOL))
            R.id.mWarm -> glView.setFilter(ContrastColorFilter(this, ColorFilter.Filter.WARM))
            R.id.mBlur -> glView.setFilter(ContrastColorFilter(this, ColorFilter.Filter.BLUR))
            R.id.mMagn -> glView.setFilter(ContrastColorFilter(this, ColorFilter.Filter.MAGN))
        }
        glView.render?.mFilter?.isHalf = isHalf
        glView.requestRender()
        return super.onOptionsItemSelected(item)
    }
}