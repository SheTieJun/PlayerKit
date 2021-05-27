package com.tencent.video.superplayer.ui.view

import android.content.*
import android.util.AttributeSet
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import com.tencent.liteav.superplayer.*
import com.tencent.liteav.superplayer.databinding.SuperplayerControlCaseViewBinding
import com.tencent.video.superplayer.base.ConfigInterface
import com.tencent.video.superplayer.base.GlobalConfig
import com.tencent.video.superplayer.base.PlayerConfig
import com.tencent.video.superplayer.base.UIConfig
import com.tencent.video.superplayer.casehelper.VideoCaseHelper


class VodMoreView : FrameLayout, CompoundButton.OnCheckedChangeListener, ConfigInterface {
    private var mCallback: Callback? = null
    private lateinit var playerConfig: PlayerConfig
    private lateinit var uiConfig: UIConfig
    private var mCaseHelper: VideoCaseHelper? = null


    private lateinit var mViewBinding: SuperplayerControlCaseViewBinding

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        mViewBinding = SuperplayerControlCaseViewBinding.inflate(LayoutInflater.from(context),this,true)
        mCaseHelper = VideoCaseHelper(this)
    }

    override fun updatePlayConfig(config: PlayerConfig) {
        this.playerConfig = config
        mCaseHelper?.setCurSpeed(GlobalConfig.speed)
        mCaseHelper?.updatePlayConfig(config)
        mViewBinding.superplayerSwitchAccelerate.isChecked = GlobalConfig.enableHWAcceleration
        mViewBinding.superplayerSwitchAccelerate.setOnCheckedChangeListener(this)
        mViewBinding.superplayerSwitchMirror.setOnCheckedChangeListener(this)
    }

    override fun updateUIConfig(uiConfig: UIConfig) {
        this.uiConfig = uiConfig
        mViewBinding.apply {
            llTimeStyle.isVisible = uiConfig.isShowTimeStyle
            iRecyclerViewCaseTime.isVisible  = uiConfig.isShowTimeStyle

            tvPlaStyle.isVisible  = uiConfig.isShowPlayStyle
            iRecyclerViewCasePlayMode.isVisible = uiConfig.isShowPlayStyle

            superplayerSwitchAccelerate.isVisible = uiConfig.isShowAccelerate
            superplayerLlMirror.isVisible = uiConfig.isShowMirror
        }
        mCaseHelper?.updateUIConfig(uiConfig)
    }


    /**
     * 设置回调
     *
     * @param callback
     */
    fun setCallback(callback: Callback?) {
        mCallback = callback
    }

    /**
     * 倍速选择监听
     */
    fun onCheckedChanged(speed: Float) {
        if (mCallback != null) {
            mCallback!!.onSpeedChange(speed)
        }
        GlobalConfig.speed = speed
    }

    fun onDestroyTimeCallBack() {
        mCaseHelper?.onDestroy()
    }

    fun updateSpeedChange(speedLevel: Float) {
        mCaseHelper?.setCurSpeed(speedLevel)
    }

    /**
     * 回调
     */
    interface Callback {
        /**
         * 播放速度更新回调
         *
         * @param speedLevel
         */
        fun onSpeedChange(speedLevel: Float)

        /**
         * 镜像开关回调
         *
         * @param isMirror
         */
        fun onMirrorChange(isMirror: Boolean)

        /**
         * 硬解开关回调
         *
         * @param isAccelerate
         */
        fun onHWAcceleration(isAccelerate: Boolean)
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        if (compoundButton.id == R.id.superplayer_switch_mirror) {
            if (mCallback != null) {
                mCallback!!.onMirrorChange(isChecked)
            }
        } else if (compoundButton.id == R.id.superplayer_switch_accelerate) {
            GlobalConfig.enableHWAcceleration = !GlobalConfig.enableHWAcceleration
            mViewBinding.superplayerSwitchAccelerate.isChecked = GlobalConfig.enableHWAcceleration
            if (mCallback != null) {
                mCallback!!.onHWAcceleration(GlobalConfig.enableHWAcceleration)
            }
        }
    }
}