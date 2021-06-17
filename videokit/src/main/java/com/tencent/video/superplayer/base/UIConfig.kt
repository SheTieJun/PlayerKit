package com.tencent.video.superplayer.base


/**
 * 界面配置显示
 */
class UIConfig private constructor(
    var showTV: Boolean, //展示投屏按钮
    var showShare: Boolean, //展示分享按钮
    var showTop: Boolean, //展示顶部条
    var showBottom: Boolean,//展示底部条
    var showSpeed: Boolean,//展示倍数
    var showMore: Boolean,//展示更多
    var showLock: Boolean, //展示Lock
    var keepTop: Boolean, //顶部不隐藏
    var keepBottom: Boolean,//底部不隐藏
    var isShowAccelerate: Boolean,//展示已经加速功能
    var isShowPlayStyle: Boolean,//展示播放模式
    var isShowTimeStyle: Boolean, //展示定时
    var isShowMirror: Boolean,//镜像功能
    var showFull: Boolean,//全屏按钮
) {


    companion object {

        val uiConfig: UIConfig by lazy { ofDef() }

        /**
         * 带接收者的函数类型,这意味着我们需要向函数传递一个Builder类型的实例
         */
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()


        fun ofDef(): UIConfig {
            return build {
                keepBottom = false
                keepTop = false
            }
        }
    }


    class Builder {
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

        fun build(): UIConfig {

            return UIConfig(
                showTV, showShare, showTop, showBottom, showSpeed, showMore,
                showLock, keepTop, keepBottom, isShowAccelerate, isShowPlayStyle,
                isShowTimeStyle, isShowMirror,showFull
            )
        }
    }
}