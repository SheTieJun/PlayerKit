package me.shetj.sdk.video.base


class PlayerConfig   {
    val RENDER_MODE_ADJUST_RESOLUTION = 1
    val RENDER_MODE_FULL_FILL_SCREEN = 0
    var renderMode =  RENDER_MODE_ADJUST_RESOLUTION
    var liveRenderMode = RENDER_MODE_FULL_FILL_SCREEN
    var floatViewRect = TXRect(0, 0, 800, 450)
    var playShiftDomain = "liteavapp.timeshift.qcloud.com"
    var header = HashMap<String, String>()

    companion object {

        val playerConfig :PlayerConfig by lazy { ofDef()}

        const val OP_SYSTEM_ALERT_WINDOW = 24 // 支持TYPE_TOAST悬浮窗的最高API版本

        /**
         * 带接收者的函数类型,这意味着我们需要向函数传递一个Builder类型的实例
         */
        inline fun build(block: PlayerConfig.() -> Unit) =
            PlayerConfig().apply(block)

        fun ofDef(): PlayerConfig {
            return build {

            }
        }
    }

    class TXRect internal constructor(var x: Int, var y: Int, var width: Int, var height: Int)
}