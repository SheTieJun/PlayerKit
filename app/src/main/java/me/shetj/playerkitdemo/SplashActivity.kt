package me.shetj.playerkitdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import me.shetj.base.ktx.start
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AndroidSchedulers.mainThread().scheduleDirect({
            start<MainActivity>()
        },1000,TimeUnit.MILLISECONDS)
    }
}