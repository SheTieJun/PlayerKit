package com.tencent.video.superplayer.casehelper.adaper

import android.widget.TextView
import com.tencent.liteav.superplayer.R
import me.shetj.sdk.video.base.BaseKitAdapter
import me.shetj.sdk.video.base.BaseViewHolder
import com.tencent.video.superplayer.model.entity.VideoQuality

class QualityAdapter(data:ArrayList<VideoQuality>) :
        BaseKitAdapter<VideoQuality>(R.layout.superplayer_quality_item_view,data) {

    private var mClickPos: Int = -1

    override fun convert(holder: BaseViewHolder, item: VideoQuality) {
            holder.getView<TextView>(R.id.superplayer_tv_quality).apply {
                isSelected = mClickPos == holder.adapterPosition
            }.text = item.title
        }

    /**
     * 设置默认选中的清晰度
     *
     * @param position
     */
    fun setDefaultSelectedQuality(position: Int) {
        var position = position
        if (position < 0) position = 0
        mClickPos = position
        notifyDataSetChanged()
    }


}