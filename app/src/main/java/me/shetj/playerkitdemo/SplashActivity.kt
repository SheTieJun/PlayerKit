package me.shetj.playerkitdemo

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.delay
import me.shetj.base.ktx.launch
import me.shetj.base.ktx.logi
import me.shetj.base.ktx.start
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        findViewById<Button>(R.id.btn_test_go).setOnClickListener {
            start<MainActivity>()
        }
        findViewById<Button>(R.id.btn_test_go2).setOnClickListener {
            start<Player2Activity>()
        }
        findViewById<Button>(R.id.btn_test_go3).setOnClickListener {
            start<Player3Activity>()
        }
        findViewById<Button>(R.id.btn_test_go4).setOnClickListener {
            launch {
                delay(3000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activityManager.appTasks?.first {
                        it.taskInfo.baseIntent.component?.packageName == packageName
                    }?.apply {
                        moveToFront()
                    }
                }
            }
        }
    }

    protected val activityManager: ActivityManager by lazy { getActivityManagerService() }



    private fun getActivityManagerService(): ActivityManager {
        return getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }
}