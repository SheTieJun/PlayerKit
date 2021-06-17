package com.tencent.video.superplayer.casehelper.adaper

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.tencent.liteav.superplayer.R
import com.tencent.video.superplayer.base.BaseKitAdapter
import com.tencent.video.superplayer.base.BaseViewHolder

class VideoFullPlayModeListAdapter(data: ArrayList<String>) :
    BaseKitAdapter<String>(R.layout.superplayer_item_video_full_speed, data) {
    private var curModel = "单课播放"

    fun setCurPlayMode(curSpeed: String) {
        this.curModel = curSpeed
        notifyDataSetChanged()
    }

    override fun convert(holder: BaseViewHolder, data: String) {
        holder.setText(R.id.content, data)
        holder.setTextColor(
            R.id.content,
            if (curModel == data) getColor(holder.itemView.context, R.color.superplayer_orange) else getColor(
                holder.itemView.context,
                R.color.superplayer_white
            )
        )
        holder.getView<TextView>(R.id.content).apply {
            typeface = if (curModel == data) {
                Typeface.defaultFromStyle(Typeface.BOLD)
            } else {
                Typeface.defaultFromStyle(Typeface.NORMAL)
            }
        }
    }

}