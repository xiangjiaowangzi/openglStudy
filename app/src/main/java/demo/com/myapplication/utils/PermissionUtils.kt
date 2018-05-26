package demo.com.myapplication.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri
import android.net.Uri.fromParts
import android.support.v4.content.ContextCompat

/**
 * Created by LiuBin
 */
object PermissionUtils {

    /**
     * 检测权限
     *
     * @return true：已授权； false：未授权；
     */
    fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context,
                permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检测多个权限
     *
     * @return 未授权的权限
     */
    fun checkMorePermissions(context: Context, permissions: Array<String>): List<String> {
        val permissionList = ArrayList<String>()
        for (i in permissions.indices) {
            if (!checkPermission(context, permissions[i]))
                permissionList.add(permissions[i])
        }
        return permissionList
    }

    /**
     * 请求权限
     */
    fun requestPermission(context: Context, permission: String, requestCode: Int) {
        if (context is Activity) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(permission), requestCode)
        }
    }

    /**
     * 请求多个权限
     */
    fun requestMorePermissions(context: Context, permissionList: List<String>, requestCode: Int) {
        if (permissionList.isNotEmpty()){
            val permissions = permissionList.toTypedArray()
            requestMorePermissions(context, permissions, requestCode)
        }
    }

    /**
     * 请求多个权限
     */
    fun requestMorePermissions(context: Context, permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(context as Activity, permissions, requestCode)
    }

    /**
     * 判断是否已拒绝过权限
     *
     * @return
     * @describe :如果应用之前请求过此权限但用户拒绝，此方法将返回 true;
     * -----------如果应用第一次请求权限或 用户在过去拒绝了权限请求，
     * -----------并在权限请求系统对话框中选择了 Don't ask again 选项，此方法将返回 false。
     */
    fun judgePermission(context: Context, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                permission)
    }

    /**
     * 检测权限并请求权限：如果没有权限，则请求权限
     */
    fun checkAndRequestPermission(context: Context, permission: String, requestCode: Int) {
        if (!checkPermission(context, permission)) {
            requestPermission(context, permission, requestCode)
        }
    }

    /**
     * 检测并请求多个权限
     */
    fun checkAndRequestMorePermissions(context: Context, permissions: Array<String>,
            requestCode: Int) {
        val permissionList = checkMorePermissions(context, permissions)
        requestMorePermissions(context, permissionList, requestCode)
    }

    /**
     * 检测权限
     *
     * @describe：具体实现由回调接口决定
     */
    fun checkPermission(context: Context, permission: String, callBack: PermissionCheckCallBack) {
        if (checkPermission(context, permission)) { // 用户已授予权限
            callBack.onHasPermission()
        } else {
            if (judgePermission(context, permission))
            // 用户之前已拒绝过权限申请
                callBack.onUserHasAlreadyTurnedDown(permission)
            else
            // 用户之前已拒绝并勾选了不在询问、用户第一次申请权限。
                callBack.onUserHasAlreadyTurnedDownAndDontAsk(permission)
        }
    }

    /**
     * 检测多个权限
     *
     * @describe：具体实现由回调接口决定
     */
    fun checkMorePermissions(context: Context, permissions: Array<String>,
            callBack: PermissionCheckCallBack?) {
        val permissionList = checkMorePermissions(context, permissions)
        if (permissionList.isEmpty()) {  // 用户已授予权限
            callBack?.onHasPermission()
        } else {
            var isFirst = true
            for (i in permissionList.indices) {
                val permission = permissionList[i]
                if (judgePermission(context, permission)) {
                    isFirst = false
                    break
                }
            }
            val unauthorizedMorePermissions = permissionList.toTypedArray()
            if (isFirst)
            // 用户之前已拒绝过权限申请
                callBack?.onUserHasAlreadyTurnedDownAndDontAsk(*unauthorizedMorePermissions)
            else
            // 用户之前已拒绝并勾选了不在询问、用户第一次申请权限。
                callBack?.onUserHasAlreadyTurnedDown(*unauthorizedMorePermissions)

        }
    }

    /**
     * 检测并申请权限
     */
    fun checkAndRequestPermission(context: Context, permission: String, requestCode: Int,
            callBack: PermissionRequestSuccessCallBack) {
        if (checkPermission(context, permission)) {// 用户已授予权限
            callBack.onHasPermission()
        } else {
            requestPermission(context, permission, requestCode)
        }
    }

    /**
     * 检测并申请多个权限
     */
    fun checkAndRequestMorePermissions(context: Context, permissions: Array<String>,
            requestCode: Int, callBack: PermissionRequestSuccessCallBack) {
        val permissionList = checkMorePermissions(context, permissions)
        if (permissionList.isEmpty()) {  // 用户已授予权限
            callBack.onHasPermission()
        } else {
            requestMorePermissions(context, permissionList, requestCode)
        }
    }

    /**
     * 判断权限是否申请成功
     */
    fun isPermissionRequestSuccess(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 用户申请权限返回
     */
    fun onRequestPermissionResult(context: Context, permission: String, grantResults: IntArray,
            callback: PermissionCheckCallBack) {
        if (PermissionUtils.isPermissionRequestSuccess(grantResults)) {
            callback.onHasPermission()
        } else {
            if (PermissionUtils.judgePermission(context, permission)) {
                callback.onUserHasAlreadyTurnedDown(permission)
            } else {
                callback.onUserHasAlreadyTurnedDownAndDontAsk(permission)
            }
        }
    }

    /**
     * 用户申请多个权限返回
     */
    fun onRequestMorePermissionsResult(context: Context, permissions: Array<String>,
            callback: PermissionCheckCallBack) {
        var isBannedPermission = false
        val permissionList = checkMorePermissions(context, permissions)
        if (permissionList.isEmpty())
            callback.onHasPermission()
        else {
            for (i in permissionList.indices) {
                if (!judgePermission(context, permissionList[i])) {
                    isBannedPermission = true
                    break
                }
            }
            //　已禁止再次询问权限
            if (isBannedPermission)
                callback.onUserHasAlreadyTurnedDownAndDontAsk(*permissions)
            else
            // 拒绝权限
                callback.onUserHasAlreadyTurnedDown(*permissions)
        }

    }

    /**
     * 跳转到权限设置界面
     */
    fun toAppSetting(context: Context) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 9) {
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", context.getPackageName(), null)
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.action = Intent.ACTION_VIEW
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName())
        }
        context.startActivity(intent)
    }

    /**
     * 默认请求，先请求是否上次已拒绝过
     * */
    fun defaultCheckPermiss(context: Context, permissions: Array<String>, requestCode: Int,
            onHasPermissionFun: () -> Unit?,
            onUserHasAlreadyTurnedDownFun: (permission: Array<String>) -> Unit?,
            onUserHasAlreadyTurnedDownAndDontAskFun: (permission: Array<String>) -> Unit?) {
        checkMorePermissions(context, permissions, object : PermissionCheckCallBack {
            override fun onHasPermission() {
                PermissionUtils.requestMorePermissions(context, permissions, requestCode)
            }

            override fun onUserHasAlreadyTurnedDown(vararg permission: String) {
                // 上次拒绝 没勾选不在提示
                PermissionUtils.requestMorePermissions(context, permissions, requestCode)
                onUserHasAlreadyTurnedDownFun(
                        permission as @kotlin.ParameterName(name = "permission") Array<String>)
            }

            override fun onUserHasAlreadyTurnedDownAndDontAsk(vararg permission: String) {
                // 上次拒绝 勾选了不在提示, 或者是第一次的时候
                onUserHasAlreadyTurnedDownAndDontAskFun(
                        permission as @kotlin.ParameterName(name = "permission") Array<String>)
            }
        })
    }

    interface PermissionRequestSuccessCallBack {
        /**
         * 用户已授予权限
         */
        fun onHasPermission()
    }

    interface PermissionCheckCallBack {

        /**
         * 用户已授予权限
         */
        fun onHasPermission()

        /**
         * 用户已拒绝过权限
         *
         * @param permission:被拒绝的权限
         */
        fun onUserHasAlreadyTurnedDown(vararg permission: String)

        /**
         * 用户已拒绝过并且已勾选不再询问选项、用户第一次申请权限;
         *
         * @param permission:被拒绝的权限
         */
        fun onUserHasAlreadyTurnedDownAndDontAsk(vararg permission: String)
    }
}