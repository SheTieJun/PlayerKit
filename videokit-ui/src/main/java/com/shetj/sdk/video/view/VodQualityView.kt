package com.shetj.sdk.video.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shetj.sdk.video.ui.R
import com.shetj.sdk.video.casehelper.adaper.QualityAdapter
import me.shetj.sdk.video.model.VideoQuality
import java.util.*
import kotlin.collections.ArrayList

/**
 * 视频画质选择弹框
 * 1、设置画质列表[.setVideoQualityList]
 * 2、设置默认选中的画质[.setDefaultSelectedQuality]
 */
class VodQualityView : FrameLayout {
    private var mContext: Context? = null
    private var mCallback: Callback? = null// 回调

    private var mListView: RecyclerView? = null// 画质listView

    private var mAdapter: QualityAdapter? = null// 画质列表适配器

    private var mClickPos = -1 // 当前的画质下表
    private var title: View? = null

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
        mContext = context
        LayoutInflater.from(mContext).inflate(R.layout.superplayer_quality_popup_view, this)
        mListView = findViewById(R.id.superplayer_lv_quality)
        title = findViewById(R.id.title)
        mAdapter = QualityAdapter(ArrayList()).apply {
            setOnItemClickListener { _, _, position ->
                if (mCallback != null) {
                    val quality = getItem(position)
                    mCallback!!.onQualitySelect(quality)
                }
                mClickPos = position
                mAdapter!!.notifyDataSetChanged()
            }
        }
        mListView!!.layoutManager = LinearLayoutManager(mContext)
        mListView!!.adapter = mAdapter
    }

    /**
     * 设置回调
     *
     * @param callback
     */
    fun setCallback(boolean: Boolean, callback: Callback) {
        title?.isVisible = boolean
        mCallback = callback
    }

    /**
     * 设置画质列表
     *
     * @param ArrayList
     */
    fun setVideoQualityList(list: ArrayList<VideoQuality>?) {
        mAdapter?.setList(list)
    }

    /**
     * 设置默认选中的清晰度
     *
     * @param position
     */
    fun setDefaultSelectedQuality(position: Int) {
        mAdapter?.setDefaultSelectedQuality(position)
    }

    fun showQualityView(isShow: Boolean) {
        if (isShow) {
            if (!isVisible) {
                isVisible = true
                animation = AnimationUtils.loadAnimation(context, R.anim.slide_right_in)
            }
        } else {
            if (isVisible) {
                isVisible = false
                animation = AnimationUtils.loadAnimation(context, R.anim.slide_right_exit)
            }
        }
    }


    /**
     * 回调
     */
    interface Callback {
        /**
         * 画质选择回调
         *
         * @param quality
         */
        fun onQualitySelect(quality: VideoQuality)
    }
}