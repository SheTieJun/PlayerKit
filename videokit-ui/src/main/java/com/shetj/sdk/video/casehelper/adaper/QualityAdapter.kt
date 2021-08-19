package com.shetj.sdk.video.casehelper.adaper

import android.widget.TextView
import com.shetj.sdk.video.ui.R
import me.shetj.sdk.video.base.BaseKitAdapter
import me.shetj.sdk.video.base.BaseViewHolder
import me.shetj.sdk.video.model.VideoQuality

class QualityAdapter(data:ArrayList<VideoQuality>) :
        BaseKitAdapter<VideoQuality>(R.layout.superplayer_quality_item_view,data) {

    private var mClickPos: Int = -1

    override fun convert(holder: BaseViewHolder, data: VideoQuality) {
            holder.getView<TextView>(R.id.superplayer_tv_quality).apply {
                isSelected = mClickPos == holder.adapterPosition
            }.text = data.title
        }

    /**
     * 设置默认选中的清晰度
     *
     * @param position
     */
    fun setDefaultSelectedQuality(position: Int) {
        var tempPosition = position
        if (tempPosition < 0) tempPosition = 0
        mClickPos = tempPosition
        notifyDataSetChanged()
    }


}