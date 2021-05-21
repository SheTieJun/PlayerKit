package com.tencent.video.superplayer.casehelper

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.tencent.liteav.superplayer.R
import com.tencent.video.superplayer.base.ConfigInterface
import com.tencent.video.superplayer.base.PlayerConfig
import com.tencent.video.superplayer.base.UIConfig
import com.tencent.video.superplayer.casehelper.adaper.VideoSmallSpeedListAdapter
import com.tencent.video.superplayer.viedoview.ui.WindowPlayer


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
        speedAdapter.setCurSpeed(playerConfig.speed)
        val i = speedList.indexOf(playerConfig.speed)
        speedAdapter.setOnItemClickListener { adapter1, view, position ->
            val speedRate = adapter1.getItem(position) as Float
            if (speedRate != playerConfig.speed) {
                playerConfig.speed = speedRate
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
        speedAdapter.setCurSpeed(playerConfig.speed)
    }

    fun showSpeedImage() {
        updateSpeed()
        showSpeedImage(playerConfig,mIvSpeed)
    }

    fun getSpeedView(): ImageView {
        return mIvSpeed
    }

    companion object{
        fun showSpeedImage(config: PlayerConfig,mIvSpeed: ImageView) {
            when (config.speed) {
                1.0f -> mIvSpeed.setImageResource(R.drawable.superplayer_1_0_speed)
                1.25f -> mIvSpeed.setImageResource(R.drawable.superplayer_1_25_speed)
                1.5f -> mIvSpeed.setImageResource(R.drawable.superplayer_1_5_speed)
                1.75f -> mIvSpeed.setImageResource(R.drawable.superplayer_1_75_speed)
                2.0f -> mIvSpeed.setImageResource(R.drawable.superplayer_2_0_speed)
            }
        }
    }

    override fun setPlayConfig(config: PlayerConfig) {
        this.playerConfig = config
        initSpeed()
        showSpeedImage()
    }

    override fun setUIConfig(uiConfig: UIConfig) {
        this.uiConfig = uiConfig
    }
}