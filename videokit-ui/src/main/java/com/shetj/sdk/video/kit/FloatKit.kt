package com.shetj.sdk.video.kit

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import me.shetj.sdk.video.base.PlayerConfig


object FloatKit {

    fun Context.getWinManager(): WindowManager {
        return applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun getWindowParams(): WindowManager.LayoutParams {
        val mWindowParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        mWindowParams.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        mWindowParams.format = PixelFormat.TRANSLUCENT
        mWindowParams.gravity = Gravity.START or Gravity.TOP
        return mWindowParams
    }

    fun Context.checkFloatPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 6.0动态申请悬浮窗权限
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
                return false
            }
        } else {
            if (!PlayerKit.checkOp(this, PlayerConfig.OP_SYSTEM_ALERT_WINDOW)) {
                return false
            }
        }
        return true
    }
}