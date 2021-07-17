package com.shetj.sdk.video.casehelper.adaper

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.shetj.sdk.video.ui.R
import me.shetj.sdk.video.base.BaseKitAdapter
import me.shetj.sdk.video.base.BaseViewHolder


class VideoFullSpeedListAdapter(data: ArrayList<Float>) : BaseKitAdapter<Float>(R.layout.superplayer_item_video_full_speed, data) {
    private var curSpeed = 0f

    fun setCurSpeed(curSpeed: Float) {
        this.curSpeed = curSpeed
    }

    override fun convert(holder: BaseViewHolder, data: Float) {
        data.let {
            holder.setText(R.id.content,  data.toString())
            holder.setTextColor(R.id.content, if (curSpeed == data) getColor(holder.itemView.context,R.color.s_player_orange) else getColor(holder.itemView.context,R.color.s_player_white))
            holder.getView<TextView>(R.id.content).apply {
                typeface = if (curSpeed == data) {
                    Typeface.defaultFromStyle(Typeface.BOLD)
                }else {
                    Typeface.defaultFromStyle(Typeface.NORMAL)
                }
            }
        }
    }

}