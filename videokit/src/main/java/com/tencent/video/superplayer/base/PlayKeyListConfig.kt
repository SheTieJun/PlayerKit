package com.tencent.video.superplayer.base

import com.tencent.video.superplayer.casehelper.onNext


/**
 * 播放列表配置
 */
class PlayKeyListConfig {
    var keyList: MutableList<*> ?= ArrayList<Any>()
    var position: Int = 0
    var onNext: onNext? = null

    companion object {

        val uiConfig :PlayKeyListConfig by lazy { ofDef() }
        /**
         * 带接收者的函数类型,这意味着我们需要向函数传递一个Builder类型的实例
         */
        inline fun build(block: PlayKeyListConfig.() -> Unit) = PlayKeyListConfig().apply(block)

        fun ofDef(): PlayKeyListConfig {
            return build {

            }
        }
    }

    fun clear(){
        keyList = null
        onNext = null
    }
}