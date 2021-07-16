package com.tencent.video.superplayer.casehelper

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tencent.liteav.superplayer.R
import me.shetj.sdk.video.base.*
import com.tencent.video.superplayer.casehelper.adaper.VideoFullPlayModeListAdapter
import com.tencent.video.superplayer.casehelper.adaper.VideoFullSpeedListAdapter
import com.tencent.video.superplayer.casehelper.adaper.VideoFullTimeTypeListAdapter
import me.shetj.sdk.video.base.timer.TimeType.Companion.getPlayModeList
import me.shetj.sdk.video.base.timer.TimeType.Companion.getTimeTypeList2
import me.shetj.sdk.video.base.timer.TimerConfigure
import me.shetj.sdk.video.base.timer.TimerConfigure.Companion.REPEAT_MODE_ALL
import me.shetj.sdk.video.base.timer.TimerConfigure.Companion.REPEAT_MODE_ONE
import com.tencent.video.superplayer.ui.view.VodMoreView
import java.text.SimpleDateFormat
import java.util.*

/**
 * 视频的[com.tencent.liteav.superplayer.SuperPlayerView]的菜单按钮操作
 * 1.倍数切换
 * 2.定时功能
 * 3.循环功能
 */

class VideoCaseHelper(private val mVodMoreView: VodMoreView): TimerConfigure.CallBack,
    ConfigInterface {
    private lateinit var playerConfig: PlayerConfig
    private lateinit var uiConfig: UIConfig
    private var mTimeAdapter: VideoFullTimeTypeListAdapter? = null
    private var adapter: VideoFullSpeedListAdapter? =null
    private var context :Context   = mVodMoreView.context
    private var iRecyclerViewSpeed :  RecyclerView? =null
    private var iRecyclerViewTime :  RecyclerView? =null
    private var iRecyclerViewPlayMode :  RecyclerView? =null
    private var content:View ?= null
    private var timeShow :TextView ?= null
    private var playMode = if (!TimerConfigure.instance.isRepeatOne()) "顺序播放" else "单曲循环"
    private var mAdapter = VideoFullPlayModeListAdapter(getPlayModeList())
    init {
        initView(mVodMoreView)
        TimerConfigure.instance.addCallBack(this)
    }

    private fun initView(rootView: ViewGroup?) {
        rootView?.apply {
            content = findViewById(R.id.ll_case_list)
            timeShow = findViewById(R.id.time_show)
            iRecyclerViewSpeed = findViewById(R.id.iRecyclerView_case_speed)
            iRecyclerViewTime = findViewById(R.id.iRecyclerView_case_time)
            iRecyclerViewPlayMode = findViewById(R.id.iRecyclerView_case_play_mode)
            content?.setOnClickListener {
                showCase(false)
            }
        }
    }

    private fun showCase(isShow: Boolean) {
        if (isShow) {
            mVodMoreView.visibility = View.VISIBLE
            mVodMoreView.animation = AnimationUtils.loadAnimation(context, R.anim.slide_right_in)
        } else {
            mVodMoreView.visibility = View.GONE
            mVodMoreView.animation = AnimationUtils.loadAnimation(context, R.anim.slide_right_exit)
        }
    }

    fun setCurSpeed(speedRate: Float) {
        adapter?.setCurSpeed(speedRate)
        adapter?.notifyDataSetChanged()
    }


    private fun initPlayMode() {
        mAdapter.setCurPlayMode(playMode)
        iRecyclerViewPlayMode?.layoutManager =  LinearLayoutManager(context,  LinearLayoutManager.HORIZONTAL, false)
        mAdapter.setOnItemClickListener { _: BaseKitAdapter<*>?, _: View?, position: Int ->
            mAdapter.getItem(position).let {
                if (playMode != it) {
                    mAdapter.setCurPlayMode(it)
                    TimerConfigure.instance.changePlayMode()
                    showCase(false)
                }
            }
        }
        iRecyclerViewPlayMode?.adapter = mAdapter
    }

    private fun initTime() {
        mTimeAdapter = VideoFullTimeTypeListAdapter(getTimeTypeList2())
        mTimeAdapter?.setPosition(TimerConfigure.instance.getTimeTypePosition())
        iRecyclerViewTime?.layoutManager =  GridLayoutManager(context, 4)
        mTimeAdapter?.setOnItemClickListener { _: BaseKitAdapter<*>?, _: View?, position: Int ->
            mTimeAdapter!!.setPosition(position)
            TimerConfigure.instance.setTimeType(mTimeAdapter!!.getItem(position), position)
            showCase(false)
        }
        iRecyclerViewTime?.adapter = mTimeAdapter
    }


    private fun initSpeed() {
        val speedList = arrayListOf(1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        adapter = VideoFullSpeedListAdapter(speedList)
        adapter?.setCurSpeed(GlobalConfig.speed)
        iRecyclerViewSpeed?.layoutManager = GridLayoutManager(context, 5)
        adapter?.setOnItemClickListener { adapter1, _, position ->
            val speedRate = adapter1.getItem(position) as Float
            if (speedRate != GlobalConfig.speed) {
                GlobalConfig.speed = speedRate
                mVodMoreView.onCheckedChanged(speedRate)
                setCurSpeed(speedRate)
                showCase(false)
            }
        }
        iRecyclerViewSpeed?.adapter = adapter
    }

    //region 定时关闭
    override fun onTick(progress: Long) {
        val (date, format) = pairTime(progress)
        timeShow?.text = String.format("%s后关闭", format.format(date))
    }

    private fun pairTime(progress: Long): Pair<Date, SimpleDateFormat> {
        var pattern = "mm:ss"
        if (progress >= 1000 * 60 * 60) {
            pattern = "HH:mm:ss"
        }
        val date = Date(progress)
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        return Pair(date, format)
    }

    override fun onStateChange(state: Int) {
        when (state) {
            TimerConfigure.STATE_COMPLETE -> {
                timeShow?.text = ""
                mTimeAdapter?.setPosition(0)
            }
            TimerConfigure.STATE_CLOSE -> {
                timeShow?.text = ""
            }
            TimerConfigure.STATE_COURSE -> {
            }
            else -> {
            }
        }
    }


    override fun onChangeModel(repeatMode: Int) {
        playMode = when (repeatMode) {
             REPEAT_MODE_ONE -> {
                "单课循环"
            }
             REPEAT_MODE_ALL -> {
                "顺序播放"
            }
            else -> "顺序播放"
        }
        mAdapter.setCurPlayMode(playMode)
    }
    //endregion定时关闭

    fun onDestroy() {
        TimerConfigure.instance.removeCallBack(this)
    }

    override fun updatePlayConfig(config: PlayerConfig) {
         this.playerConfig  = config
        initSpeed()
        initTime()
        initPlayMode()
    }

    override fun updateUIConfig(uiConfig: UIConfig) {
        this.uiConfig = uiConfig
    }
}