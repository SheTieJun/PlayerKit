package com.tencent.video.superplayer.model

import com.tencent.rtmp.TXLivePlayer
import com.tencent.video.superplayer.SuperPlayerDef.PlayerType
import com.tencent.video.superplayer.model.entity.PlayImageSpriteInfo
import com.tencent.video.superplayer.model.entity.PlayKeyFrameDescInfo
import com.tencent.video.superplayer.model.entity.VideoQuality

abstract class SuperPlayerObserver {
    /**
     * 开始播放
     * @param name 当前视频名称
     */
    open fun onPlayBegin() {}

    /**
     * 播放暂停
     */
    open fun onPlayPause() {}

    /**
     * 播放器停止
     */
    open fun onPlayStop() {}

    /**
     * 播放器进入Loading状态
     */
    open fun onPlayLoading() {}


    open fun onVideoSize(width:Int,height:Int){

    }
    /**
     * 播放进度回调
     *
     * @param current
     * @param duration
     */
    open fun onPlayProgress(current: Long, duration: Long) {}
    open fun onSeek(position: Int) {}
    open fun onSwitchStreamStart(success: Boolean, playerType: PlayerType, quality: VideoQuality) {}
    open fun onSwitchStreamEnd(success: Boolean, playerType: PlayerType, quality: VideoQuality?) {}
    open fun onError(code: Int, message: String?) {}
    open fun onPlayerTypeChange(playType: PlayerType?) {}
    open fun onPlayTimeShiftLive(player: TXLivePlayer?, url: String?) {}
    open fun onVideoQualityListChange(
        videoQualities: ArrayList<VideoQuality>?,
        defaultVideoQuality: VideoQuality?
    ) {
    }

    open fun onVideoImageSpriteAndKeyFrameChanged(
        info: PlayImageSpriteInfo?,
        list: ArrayList<PlayKeyFrameDescInfo>?
    ) {
    }

    open fun onPlayComplete() {


    }
}