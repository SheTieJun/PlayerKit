package me.shetj.sdk.video.tx

import android.content.*
import android.util.AttributeSet
import android.view.*
import me.shetj.sdk.tx.R
import com.shetj.sdk.video.viedoview.ui.BaseFloatPlayer
import me.shetj.sdk.tx.databinding.TxPlayerVodPlayerFloatBinding
import me.shetj.sdk.video.player.PlayerDef

/**
 * 悬浮窗模式播放控件
 *
 *
 * 1、滑动以移动悬浮窗，点击悬浮窗回到窗口模式[.onTouchEvent]
 *
 *
 * 2、关闭悬浮窗[.onClick]
 */
internal class TXFloatPlayer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseFloatPlayer(context, attrs, defStyleAttr) {

    private  lateinit var viewBinding: TxPlayerVodPlayerFloatBinding

    override fun initView(context: Context) {
        viewBinding =
            TxPlayerVodPlayerFloatBinding.inflate(LayoutInflater.from(context), this, true)
        viewBinding.txIvClose.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        super.onClick(view)
        if (view.id == R.id.tx_iv_close){
            if (mControllerCallback != null) {
                mControllerCallback!!.onBackPressed(PlayerDef.PlayerMode.FLOAT)
            }
        }
    }

    override fun getPlayerView(): View {
        return viewBinding.txCloudVideoView
    }
}