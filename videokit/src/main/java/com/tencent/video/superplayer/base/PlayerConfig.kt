package com.tencent.video.superplayer.base

import com.tencent.rtmp.TXLiveConstants


class PlayerConfig private constructor(
    val renderMode: Int,
    val liveRenderMode: Int,
    val floatViewRect: TXRect,
    val playShiftDomain: String,
    val header: HashMap<String, String>,
    val userCache :Boolean
) {
    companion object {

        val playerConfig = ofDef()

        const val OP_SYSTEM_ALERT_WINDOW = 24 // 支持TYPE_TOAST悬浮窗的最高API版本

        /**
         * 带接收者的函数类型,这意味着我们需要向函数传递一个Builder类型的实例
         */
        inline fun build(block: Builder.() -> Unit) =
            Builder().apply(block).build()

        fun ofDef(): PlayerConfig {
            return build {

            }
        }
    }

    class Builder {
        var renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION
        var liveRenderMode = TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN
        var floatViewRect = TXRect(0, 0, 810, 540)
        var playShiftDomain = "liteavapp.timeshift.qcloud.com"
        var header = HashMap<String, String>()
        var userCache:Boolean = false

        fun build(): PlayerConfig {
            return PlayerConfig(
                renderMode, liveRenderMode,
                floatViewRect, playShiftDomain, header,userCache
            )
        }

        fun newBuilder(builder: Builder, block: Builder.() -> Unit): Builder {
            return Builder().apply {
                this.renderMode = builder.renderMode
                this.liveRenderMode = builder.liveRenderMode
                this.floatViewRect = builder.floatViewRect
                this.playShiftDomain = builder.playShiftDomain
                this.userCache = builder.userCache
            }.apply(block)
        }
    }

    class TXRect internal constructor(var x: Int, var y: Int, var width: Int, var height: Int)
}