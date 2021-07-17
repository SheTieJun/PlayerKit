package com.shetj.sdk.video.casehelper.adaper

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.shetj.sdk.video.ui.R
import me.shetj.sdk.video.base.BaseKitAdapter
import me.shetj.sdk.video.base.BaseViewHolder

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
                R.color.s_player_orange
            ) else ContextCompat.getColor(holder.itemView.context, R.color.s_player_white)
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