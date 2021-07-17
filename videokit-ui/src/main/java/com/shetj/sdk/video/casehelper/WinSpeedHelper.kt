package com.shetj.sdk.video.casehelper

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.shetj.sdk.video.ui.R
import me.shetj.sdk.video.base.ConfigInterface
import me.shetj.sdk.video.base.GlobalConfig
import me.shetj.sdk.video.base.PlayerConfig
import me.shetj.sdk.video.base.UIConfig
import com.shetj.sdk.video.casehelper.adaper.VideoSmallSpeedListAdapter
import com.shetj.sdk.video.viedoview.ui.WindowPlayer


/**
 * 窗口倍数控制
 */
class WinSpeedHelper(private val windowPlayer: WindowPlayer): ConfigInterface {
    private lateinit var playerConfig: PlayerConfig
    private lateinit var uiConfig: UIConfig
    private val mLlSpeedList = windowPlayer.findViewById<View>(R.id.ll_speed_list)
    private lateinit var speedAdapter: VideoSmallSpeedListAdapter
    private val mIvSpeed: ImageView = windowPlayer.findViewById(R.id.iv_speed)
    private val mRecycleView: RecyclerView = windowPlayer.findViewById(R.id.iRecyclerView)

    init {
        mLlSpeedList.setOnClickListener {
            showSpeedList(false)
        }
        mIvSpeed.setOnClickListener {
            showSpeedList(true)
        }
    }

    private fun initSpeed() {
        val speedList = arrayListOf(1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        speedAdapter = VideoSmallSpeedListAdapter(speedList)
        speedAdapter.setCurSpeed(GlobalConfig.speed)
        val i = speedList.indexOf(GlobalConfig.speed)
        speedAdapter.setOnItemClickListener { _, _, position ->
            val speedRate = speedAdapter.getItem(position)
            if (speedRate != GlobalConfig.speed) {
                GlobalConfig.speed = speedRate
                windowPlayer.onSpeedChange(speedRate)
                speedAdapter.setCurSpeed(speedRate)
                speedAdapter.notifyDataSetChanged()
                showSpeedImage()
            }
            showSpeedList(false)
        }
        mRecycleView.adapter = speedAdapter
        mRecycleView.scrollToPosition(i)
    }


    fun showSpeedList(isShow: Boolean) {
        if (isShow) {
            windowPlayer.hide()
            if (!mLlSpeedList.isVisible) {
                mLlSpeedList?.visibility = View.VISIBLE
                mLlSpeedList?.animation = AnimationUtils.loadAnimation(windowPlayer.context, R.anim.slide_right_in)
            }
        } else {
            if (mLlSpeedList.isVisible) {
                mLlSpeedList?.visibility = View.GONE
                mLlSpeedList?.animation = AnimationUtils.loadAnimation(windowPlayer.context, R.anim.slide_right_exit)
            }
        }
    }

    private fun updateSpeed(){
        speedAdapter.setCurSpeed(GlobalConfig.speed)
    }

    fun showSpeedImage() {
        updateSpeed()
        showSpeedImage(GlobalConfig.speed,mIvSpeed)
    }

    fun getSpeedView(): ImageView {
        return mIvSpeed
    }

    companion object{
        fun showSpeedImage(speed: Float,mIvSpeed: ImageView) {
            when (speed) {
                1.0f -> mIvSpeed.setImageResource(R.drawable.superplayer_1_0_speed)
                1.25f -> mIvSpeed.setImageResource(R.drawable.superplayer_1_25_speed)
                1.5f -> mIvSpeed.setImageResource(R.drawable.superplayer_1_5_speed)
                1.75f -> mIvSpeed.setImageResource(R.drawable.superplayer_1_75_speed)
                2.0f -> mIvSpeed.setImageResource(R.drawable.superplayer_2_0_speed)
            }
        }
    }

    override fun updatePlayConfig(config: PlayerConfig) {
        this.playerConfig = config
        initSpeed()
        showSpeedImage()
    }

    override fun updateUIConfig(uiConfig: UIConfig) {
        this.uiConfig = uiConfig
    }
}