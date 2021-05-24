package com.tencent.video.superplayer.casehelper

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tencent.liteav.superplayer.R
import com.tencent.video.superplayer.base.BaseKitAdapter
import com.tencent.video.superplayer.viedoview.ui.FullScreenPlayer


typealias onNext = (Any) -> Unit

interface KeyListListener{

    fun nextOne()

    fun updatePosition(position:Int)

    fun setKeyList(name: String?, adapter: BaseKitAdapter<*>?, position: Int = 0, onNext: onNext?=null)

}

class PlayKeyListHelper(private val fullScreenPlayer: FullScreenPlayer) :
     KeyListListener {
    private val mLlSpeedList = fullScreenPlayer.findViewById<View>(R.id.ll_data_list)
    private val mTvKey: TextView = fullScreenPlayer.findViewById(R.id.superplayer_tv_play_list)
    private val mTvName: TextView = fullScreenPlayer.findViewById(R.id.tv_name)
    private val mRecycleView: RecyclerView = fullScreenPlayer.findViewById(R.id.iRecyclerView_key)
    private val mIvNext: ImageView = fullScreenPlayer.findViewById(R.id.iv_next)
    private var mAdapter: BaseKitAdapter< *>? = null
    private var position: Int = 0
    private var onNext: onNext? = null

    init {

        mRecycleView.apply {
            layoutManager = LinearLayoutManager(fullScreenPlayer.context)
        }
        mLlSpeedList.setOnClickListener {
            showLectureList(false)
        }
        mTvKey.setOnClickListener {
            showLectureList(true)
        }
        mTvName.setOnClickListener { showLectureList(true) }
        mIvNext.setOnClickListener {
            nextOne()
        }
    }

    override fun nextOne() {
        if ((mRecycleView.adapter as? BaseKitAdapter< *>)?.data?.size ?: 0 > position + 1) {
            (mRecycleView.adapter as? BaseKitAdapter< *>)?.getItem(position + 1)?.apply {
                onNext?.invoke(this)
                updatePosition(position + 1)
            }
        }
    }


    private fun showLectureList(isShow: Boolean) {
        if (isShow) {
            fullScreenPlayer.hide()
            mLlSpeedList?.visibility = View.VISIBLE
            mLlSpeedList?.animation = AnimationUtils.loadAnimation(fullScreenPlayer.context, R.anim.slide_right_in)
            mRecycleView.scrollToPosition(position)
        } else {
            mLlSpeedList?.visibility = View.GONE
            mLlSpeedList?.animation = AnimationUtils.loadAnimation(fullScreenPlayer.context, R.anim.slide_right_exit)
        }
    }

    override fun updatePosition(position: Int) {
        this.position = position
        this.mIvNext.isVisible = position + 1 < mAdapter?.itemCount ?: 0
        this.mAdapter?.notifyDataSetChanged()
    }

    override fun setKeyList(name: String?, adapter: BaseKitAdapter<*>?, position: Int, onNext: onNext?) {
        mTvName.text = name
        mTvKey.text = name
        mRecycleView.adapter = adapter
        adapter?.onAttachedToRecyclerView(mRecycleView)
        this.mAdapter = adapter
        this.onNext = onNext
        mTvKey.isVisible = adapter != null
        if (position != 0) {
            this.position = position
        }
        updatePosition(this.position)
        adapter?.setOnItemClickListener { _, _, pos ->
            (mRecycleView.adapter as? BaseKitAdapter<*>)?.getItem(pos)?.apply {
                onNext?.invoke(this)
                updatePosition(pos)
                showLectureList(false)
            }
        }
    }

}