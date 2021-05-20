package me.shetj.playerkitdemo

import com.tencent.video.superplayer.base.BaseKitAdapter
import com.tencent.video.superplayer.base.BaseViewHolder


/**
 * Project Name:LiZhiWeiKe
 * Package Name:com.lizhiweike.lecture.adapter
 * Created by tom on 2018/2/1 18:25 .
 *
 *
 * Copyright (c) 2016—2017 https://www.lizhiweike.com all rights reserved.
 */
class KeyListAdapter(data: ArrayList<String>) :
        BaseKitAdapter<String>(R.layout.superplayer_item_new_vod, data) {

    private var currentPosition = -1
    private var oldPosition = -1

    private var isNoShowFree :Boolean = false //是否不展示免费

    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.superplayer_tv,item)
    }

    fun getCurrentPosition() = currentPosition


    fun setCurrentLectureId(position:Int  ) {
            setCurrentPosition(position)
    }

    fun isLastLecture(): Boolean {
      return  currentPosition == itemCount -1
    }

    fun setCurrentPosition(i: Int) {
        if (currentPosition != i) {
            currentPosition = i
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition)
            }
            oldPosition = currentPosition
            notifyItemChanged(currentPosition)
        }
    }

    fun updateListener(isListener:Boolean){
        if (isNoShowFree != isListener) {
            this.isNoShowFree = isListener
            notifyDataSetChanged()
        }
    }
}
