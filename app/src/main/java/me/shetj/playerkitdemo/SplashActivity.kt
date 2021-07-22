package me.shetj.playerkitdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
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
    }
}