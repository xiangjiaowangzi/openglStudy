package demo.com.myapplication.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import demo.com.myapplication.R
import demo.com.myapplication.shape.Shape
import kotlinx.android.synthetic.main.activity_fglview.mChange
import kotlinx.android.synthetic.main.activity_fglview.mGLView

/**
 * Created by LiuBin
 *
 *  绘制形体
 */
class demo2Act : Activity(){

    val REQ_CHOOSE=0x0101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fglview)
        mChange.setOnClickListener { view ->
            val intent = Intent(this, ChooseShapeActivity::class.java)
            startActivityForResult(intent, REQ_CHOOSE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (data?.getSerializableExtra("name") != null) {
                mGLView.setShape(data?.getSerializableExtra("name") as Class<out Shape>)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mGLView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mGLView.onPause()
    }

}