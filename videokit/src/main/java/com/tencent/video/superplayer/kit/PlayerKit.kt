package com.tencent.video.superplayer.kit

import android.app.AppOpsManager
import android.content.Context
import android.os.Binder
import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import com.tencent.liteav.basic.log.TXCLog


object PlayerKit {


    /**
     * 检查悬浮窗权限
     *
     *
     * API <18，默认有悬浮窗权限，不需要处理。无法接收无法接收触摸和按键事件，不需要权限和无法接受触摸事件的源码分析
     * API >= 19 ，可以接收触摸和按键事件
     * API >=23，需要在manifest中申请权限，并在每次需要用到权限的时候检查是否已有该权限，因为用户随时可以取消掉。
     * API >25，TYPE_TOAST 已经被谷歌制裁了，会出现自动消失的情况
     */
      fun checkOp(context: Context?, op: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val manager = context!!.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            try {
                val method = AppOpsManager::class.java.getDeclaredMethod(
                    "checkOp",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                return AppOpsManager.MODE_ALLOWED == method.invoke(
                    manager,
                    op,
                    Binder.getCallingUid(),
                    context.packageName
                ) as Int
            } catch (e: Exception) {
                TXCLog.e("SuperPlayerView", Log.getStackTraceString(e))
            }
        }
        return true
    }

    fun onWindowFocusChanged(window: Window,hasFocus: Boolean) {
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            if (Build.VERSION.SDK_INT < 30) {
                val decorView: View = window.decorView
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            }else{
                window.insetsController?.hide(WindowInsets.Type.systemBars())
            }
        }
    }


}