package me.shetj.playerkitdemo

import me.shetj.sdk.video.base.BaseKitAdapter
import me.shetj.sdk.video.base.BaseViewHolder


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

    override fun convert(holder: BaseViewHolder, data: String) {
        holder.setText(R.id.superplayer_tv,data)
    }

}
