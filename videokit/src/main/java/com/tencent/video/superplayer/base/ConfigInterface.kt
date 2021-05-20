package com.tencent.video.superplayer.base

import com.tencent.video.superplayer.casehelper.VideoCaseHelper


interface ConfigInterface {
    fun setPlayConfig(config: PlayerConfig)

    fun setUIConfig(uiConfig: UIConfig)
}