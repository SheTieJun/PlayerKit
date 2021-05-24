package com.tencent.video.superplayer.base

import com.tencent.liteav.basic.log.TXCLog
import com.tencent.rtmp.TXLiveConstants


class PlayerConfig private constructor(
    var renderMode: Int,
    var liveRenderMode: Int,
    var enableHWAcceleration: Boolean,
    var floatViewRect: TXRect,
    var playShiftDomain: String,
    var maxCacheItem: Int,
    var speed: Float,
    var enableFloatWindow: Boolean,
    var header: HashMap<String, String>,
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
        var maxCacheItem = GlobalConfig.maxCacheItem
            set(value) {
                GlobalConfig.maxCacheItem = value
                field = value
            }
        var speed: Float =  GlobalConfig.speed
            set(value) {
                GlobalConfig.speed = value
                field = value
            }
        var enableHWAcceleration = true
        var enableFloatWindow: Boolean = true
        var header = HashMap<String, String>()


        fun build(): PlayerConfig {
            return PlayerConfig(
                renderMode, liveRenderMode, enableHWAcceleration,
                floatViewRect, playShiftDomain, maxCacheItem,
                speed, enableFloatWindow, header
            )
        }

        fun newBuilder(builder: Builder, block: Builder.() -> Unit): Builder {
            return Builder().apply {
                this.renderMode = builder.renderMode
                this.liveRenderMode = builder.liveRenderMode
                this.enableHWAcceleration = builder.enableHWAcceleration
                this.floatViewRect = builder.floatViewRect
                this.playShiftDomain = builder.playShiftDomain
                this.maxCacheItem = builder.maxCacheItem
                this.speed = builder.speed
                this.enableFloatWindow = builder.enableFloatWindow
            }.apply(block)
        }
    }

    class TXRect internal constructor(var x: Int, var y: Int, var width: Int, var height: Int)
}