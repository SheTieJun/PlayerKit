package com.tencent.video.superplayer.base

import com.tencent.video.superplayer.casehelper.onNext


/**
 * 播放列表配置
 */
class PlayKeyListConfig(
    var keyList: MutableList<*>?,
    var position: Int,
    var onNext: onNext?
) {


    companion object {

        val uiConfig = ofDef()

        /**
         * 带接收者的函数类型,这意味着我们需要向函数传递一个Builder类型的实例
         */
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()


        fun ofDef(): PlayKeyListConfig {
            return build {

            }
        }
    }

    fun clear(){
        keyList = null
        onNext = null
    }

    class Builder {
        var keyList: ArrayList<*> = ArrayList<Any>()
        var position: Int = 0
        var onNext: onNext? = null
        fun build(): PlayKeyListConfig {

            return PlayKeyListConfig(
                keyList, position, onNext
            )
        }
    }
}