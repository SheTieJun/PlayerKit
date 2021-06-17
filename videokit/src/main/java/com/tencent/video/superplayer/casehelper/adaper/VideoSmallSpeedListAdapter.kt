package com.tencent.video.superplayer.casehelper.adaper

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.tencent.liteav.superplayer.R
import com.tencent.video.superplayer.base.BaseKitAdapter
import com.tencent.video.superplayer.base.BaseViewHolder

class VideoSmallSpeedListAdapter(data: ArrayList<Float>) :
    BaseKitAdapter<Float>(R.layout.superplayer_item_video_full_speed_2, data) {
    private var curSpeed = 0f
    override fun convert(
        holder: BaseViewHolder,
        data: Float
    ) {
        holder.setText(R.id.content, data.toString() + "倍")
        holder.setTextColor(
            R.id.content,
            if (curSpeed == data) ContextCompat.getColor(
                holder.itemView.context,
                R.color.superplayer_orange
            ) else ContextCompat.getColor(holder.itemView.context, R.color.superplayer_white)
        )
        holder.getView<TextView>(R.id.content).apply {
            typeface = if (curSpeed == data) {
                Typeface.defaultFromStyle(Typeface.BOLD)
            } else {
                Typeface.defaultFromStyle(Typeface.NORMAL)
            }
        }
    }

    fun setCurSpeed(curSpeed: Float) {
        this.curSpeed = curSpeed
    }
}