package me.shetj.playerkitdemo

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import me.shetj.base.S


class BaseInitialize:Initializer<Unit> {

    override fun create(context: Context) {
        S.init(context.applicationContext as Application, false, "https://xxxx.com")
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return  mutableListOf()
    }
}