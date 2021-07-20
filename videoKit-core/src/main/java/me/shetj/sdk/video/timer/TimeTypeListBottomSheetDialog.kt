package me.shetj.sdk.video.timer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import me.shetj.sdk.video.R
import me.shetj.sdk.video.base.OnItemClickListener
import me.shetj.sdk.video.timer.TimeType.Companion.getTimeTypeList3

class TimeTypeListBottomSheetDialog(private val context: Context, var position: Int) {
    private val bottomSheetDialog: BottomSheetDialog?
    private var onItemClickListener: OnItemClickListener? = null
    private var mAdapter: TimeTypeListAdapter? = null
    private fun buildBottomSheetDialog(): BottomSheetDialog {
        val mBottomSheetDialog =
            BottomSheetDialog(context, R.style.transparent_bottom_dialog_fragment_style)
        val rootView = LayoutInflater.from(context).inflate(R.layout.dialog_time_type_list, null)
        val recyclerView: RecyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        mAdapter = TimeTypeListAdapter(getTimeTypeList3())
        mAdapter!!.setPosition(position)
        mAdapter!!.setOnItemClickListener { _, _, position ->
            mAdapter!!.setPosition(position)
        }
        recyclerView.adapter = mAdapter
        if (position != -1) {
            recyclerView.scrollToPosition(position)
        }
        rootView.findViewById<View>(R.id.no_use_text).setOnClickListener {
            dismissBottomSheet()
        }
        val lectureClose = rootView.findViewById<View>(R.id.cancel)
        lectureClose.setOnClickListener { v: View? -> dismissBottomSheet() }
        mBottomSheetDialog.setContentView(rootView)
        return mBottomSheetDialog
    }


    fun showBottomSheet() {
        if (bottomSheetDialog != null && !bottomSheetDialog.isShowing) bottomSheetDialog.show()
    }

    fun dismissBottomSheet() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing) bottomSheetDialog.dismiss()
    }


    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    init {
        bottomSheetDialog = buildBottomSheetDialog()
    }
}