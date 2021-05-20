package com.tencent.video.superplayer.base

import com.tencent.video.superplayer.model.entity.VideoQuality
import com.tencent.video.superplayer.ui.player.Player


typealias OnSeekComplete = (player: Player) -> Unit

typealias OnVideoSize = (width: Int, height: Int) -> Unit

typealias OnClickShare = () -> Unit

typealias OnStartFullScreen = () -> Unit

typealias OnStartFloatWindow = () -> Unit

typealias OnStartFullScreenPlay = () -> Unit

typealias OnStopFullScreen = () -> Unit

typealias OnSpeedUpdate = (speed: Float) -> Unit

typealias OnQualityChange = (quality: VideoQuality) -> Unit

typealias OnPause = () -> Unit

typealias OnResume = () -> Unit

typealias OnStart = () -> Unit

typealias OnLoading = () -> Unit

typealias OnComplete = () -> Unit

typealias OnShare = (player: Player) -> Unit

typealias OnStopSmallWindow = (player: Player) -> Unit

typealias OnSnapshot = (path: String) -> Unit