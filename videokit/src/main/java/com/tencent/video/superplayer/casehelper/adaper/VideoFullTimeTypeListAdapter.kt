package com.tencent.video.superplayer.casehelper.adaper

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.tencent.liteav.superplayer.R
import com.tencent.video.superplayer.base.BaseKitAdapter
import com.tencent.video.superplayer.base.BaseViewHolder
import com.tencent.video.superplayer.base.timer.TimeType

class VideoFullTimeTypeListAdapter(data: ArrayList<TimeType>) :
    BaseKitAdapter<TimeType>(R.layout.superplayer_item_full_time_type, data) {
    private var position = -1
    fun setPosition(targetPos: Int) { //如果不相等，说明有变化
        if (position != targetPos) {
            val old = position
            position = targetPos
            // -1 表示默认不做任何变化
            if (old != -1) {
                notifyItemChanged(old)
            }
            if (targetPos != -1) {
                notifyItemChanged(targetPos)
            }
        }
    }

    override fun convert(
        holder: BaseViewHolder,
        data: TimeType
    ) {
        holder.setText(R.id.name, data.name)
        holder.setTextColor(
            R.id.name, if (position == holder.adapterPosition)
                getColor(
                    holder.itemView.context,
                    R.color.superplayer_orange
                ) else getColor(holder.itemView.context, R.color.superplayer_white)
        )
        holder.getView<TextView>(R.id.name).apply {
            typeface = if (position == holder.adapterPosition) {
                Typeface.defaultFromStyle(Typeface.BOLD)
            } else {
                Typeface.defaultFromStyle(Typeface.NORMAL)
            }
        }
    }
}