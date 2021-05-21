package com.tencent.video.superplayer.casehelper.adaper

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.tencent.liteav.superplayer.R
import com.tencent.video.superplayer.base.BaseKitAdapter
import com.tencent.video.superplayer.base.BaseViewHolder


class VideoFullSpeedListAdapter(data: ArrayList<Float>) : BaseKitAdapter<Float>(R.layout.superplayer_item_video_full_speed, data) {
    private var curSpeed = 0f

    fun setCurSpeed(curSpeed: Float) {
        this.curSpeed = curSpeed
    }

    override fun convert(holder: BaseViewHolder, item: Float) {
        item.let {
            holder.setText(R.id.content,  item.toString())
            holder.setTextColor(R.id.content, if (curSpeed == item) getColor(holder.itemView.context,R.color.superplayer_orange) else getColor(holder.itemView.context,R.color.superplayer_white))
            holder.getView<TextView>(R.id.content)?.apply {
                typeface = if (curSpeed == item) {
                    Typeface.defaultFromStyle(Typeface.BOLD);
                }else {
                    Typeface.defaultFromStyle(Typeface.NORMAL);
                }
            }
        }
    }

}