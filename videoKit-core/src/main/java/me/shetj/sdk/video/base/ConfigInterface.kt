package me.shetj.sdk.video.base


/**
 * 通过设置不同的配置，界面展示不同的样式
 */
interface ConfigInterface {
    fun updatePlayConfig(config: PlayerConfig)

    fun updateUIConfig(uiConfig: UIConfig)
}