package demo.com.myapplication.demo

import android.Manifest
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import demo.com.myapplication.R
import demo.com.myapplication.utils.LogUtils
import demo.com.myapplication.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_camera.mCameraView
import java.util.Objects

/**
 * Created by LiuBin
 */
class demo5Act : AppCompatActivity() {

    val requetCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_camera)

        PermissionUtils.defaultCheckPermiss(this, arrayOf(Manifest.permission.CAMERA, Manifest
                .permission.WRITE_EXTERNAL_STORAGE), requetCode,
                onHasPermissionFun = {
                    setContentView(R.layout.activity_camera)
                },
                onUserHasAlreadyTurnedDownFun = { permission ->
                    for (s in permission) {
                        LogUtils.log(s)
                    }
                }, onUserHasAlreadyTurnedDownAndDontAskFun = { permission ->
            PermissionUtils.toAppSetting(this@demo5Act)
            for (s in permission) {
                LogUtils.log(s)
            }
            // 如果是第一次
//            if (isFirst)
            PermissionUtils.requestMorePermissions(this@demo5Act, arrayOf(Manifest.permission.CAMERA, Manifest
                    .permission.WRITE_EXTERNAL_STORAGE), requetCode)
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (this.requetCode == requestCode) {
            PermissionUtils.onRequestMorePermissionsResult(this, permissions as Array<String>,
                    object : PermissionUtils.PermissionCheckCallBack {
                        override fun onHasPermission() {
                            setContentView(R.layout.activity_camera)
                        }

                        override fun onUserHasAlreadyTurnedDown(vararg permission: String) {
//                            PermissionUtils.toAppSetting(this@demo5Act)
                        }

                        override fun onUserHasAlreadyTurnedDownAndDontAsk(
                                vararg permission: String) {
                        }
                    })
        }
    }

    override fun onPause() {
        super.onPause()
        if (mCameraView != null) {
            mCameraView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mCameraView != null) {
            mCameraView.onResume()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("切换摄像头").setTitle("切换摄像头").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val name = item.title.toString()
        if (name == "切换摄像头") {
            mCameraView.switchCamera()
        }
        return super.onOptionsItemSelected(item)
    }

}