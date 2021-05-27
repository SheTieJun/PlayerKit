package com.tencent.video.superplayer.base.timer

import androidx.core.content.ContextCompat
import com.tencent.liteav.superplayer.R
import com.tencent.video.superplayer.base.BaseKitAdapter
import com.tencent.video.superplayer.base.BaseViewHolder

class TimeTypeListAdapter(data: MutableList<TimeType>) : BaseKitAdapter<TimeType>(R.layout.item_time_type_list, data) {
    private var position = -1
    fun setPosition(targetPos: Int) {
        //如果不相等，说明有变化
        if (position != targetPos) {
            val old = position
            position = targetPos
            // -1 表示默认不做任何变化
            if (old != -1) {
                notifyItemChanged(old )
            }
            if (targetPos != -1) {
                notifyItemChanged(targetPos )
            }
        }
    }

    override fun convert(holder: BaseViewHolder, data: TimeType) {
        holder.setText(R.id.name, data.name)
        holder.setTextColor(R.id.name, if (position == holder.adapterPosition )
            ContextCompat.getColor(holder.itemView.context, R.color.superplayer_orange)
        else ContextCompat.getColor(holder.itemView.context, R.color.superplayer_black))
    }
}