package com.tencent.video.superplayer.casehelper

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

typealias OnAudioLoss = () ->Unit

class AudioManagerHelper(context: Context) {

    private val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var onAudioLoss:OnAudioLoss ?=null
    private var focusChangeListener: AudioManager.OnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS ->
                //长时间丢失焦点,当其他应用申请的焦点为AUDIOFOCUS_GAIN时，
                audioLoss()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                //短暂性丢失焦点，当其他应用申请AUDIOFOCUS_GAIN_TRANSIENT或AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE时，
                audioLoss()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
            AudioManager.AUDIOFOCUS_GAIN -> {
            }
        }
    }

    private fun audioLoss() {
        onAudioLoss?.invoke()
    }

    fun setOnAudioLoss(onAudioLoss: OnAudioLoss){
        this.onAudioLoss = onAudioLoss
    }

    fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {            //Android 8.0+
            val audioFocusRequest =
                    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                            .setOnAudioFocusChangeListener(focusChangeListener).build()
            audioFocusRequest.acceptsDelayedFocusGain()
            mAudioManager.requestAudioFocus(audioFocusRequest)
        } else {
            mAudioManager.requestAudioFocus(
                    focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }
}