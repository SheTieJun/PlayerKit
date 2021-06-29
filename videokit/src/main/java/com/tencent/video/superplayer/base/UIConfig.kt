package com.tencent.video.superplayer.base


/**
 * 界面配置显示
 */
class UIConfig  {
    var showTV: Boolean = true
    var showShare: Boolean = true
    var showTop: Boolean = true
    var showBottom: Boolean = true
    var showSpeed: Boolean = true
    var showMore: Boolean = true
    var showLock: Boolean = true
    var keepTop: Boolean = false
    var keepBottom: Boolean = false
    var isShowAccelerate: Boolean = true
    var isShowPlayStyle: Boolean = true
    var isShowTimeStyle: Boolean = true
    var isShowMirror = false
    var showFull = true

    companion object {

        val uiConfig: UIConfig by lazy { ofDef() }

        /**
         * 带接收者的函数类型,这意味着我们需要向函数传递一个Builder类型的实例
         */
        inline fun build(block: UIConfig.() -> Unit) = UIConfig().apply(block)


        fun ofDef(): UIConfig {
            return build {
                keepBottom = false
                keepTop = false
            }
        }
    }

}