package com.shetj.sdk.video.viedoview

import android.content.Context
import android.util.AttributeSet
import com.shetj.sdk.video.casehelper.KeyListListener
import com.shetj.sdk.video.casehelper.onNext
import me.shetj.sdk.video.base.BaseKitAdapter
import me.shetj.sdk.video.timer.TimerConfigure
import me.shetj.sdk.video.ui.ABUIPlayer

/**
 * 播放器公共逻辑 + 播放列表处理
 */
abstract class AbBaseUIPlayer : ABUIPlayer ,KeyListListener,TimerConfigure.CallBack{

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onTick(progress: Long) {
    }

    override fun onStateChange(state: Int) {
    }

    override fun onChangeModel(repeatMode: Int) {
    }


    override fun nextOneKey() {

    }

    override fun updateListPosition(position: Int) {
    }

    override fun setKeyList(
        name: String?,
        adapter: BaseKitAdapter<*>?,
        position: Int,
        onNext: onNext?
    ) {

    }

}