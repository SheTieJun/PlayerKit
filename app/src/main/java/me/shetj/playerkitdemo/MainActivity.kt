package me.shetj.playerkitdemo

import com.tencent.video.superplayer.GlobalConfig
import com.tencent.video.superplayer.SimPlayerCallBack
import com.tencent.video.superplayer.SuperPlayerDef
import com.tencent.video.superplayer.SuperPlayerModel
import com.tencent.video.superplayer.tv.TVControl
import me.shetj.base.ktx.showToast
import me.shetj.base.ktx.start
import me.shetj.base.mvvm.BaseBindingActivity
import me.shetj.base.mvvm.BaseViewModel
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.playerkitdemo.databinding.ActivityMainBinding

class MainActivity : BaseBindingActivity<BaseViewModel,ActivityMainBinding>() {
    private var isTv: Boolean = false
    private var iskey: Boolean = false
    override fun onActivityCreate() {
        super.onActivityCreate()
        ArmsUtils.statuInScreen2(this)
        initVideoInfo()
    }

    private fun initVideoInfo() {
        mViewBinding.superVodPlayerView.apply {
            val model = SuperPlayerModel()
            model.url =
                "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4"
            mViewBinding.superVodPlayerView.setPlayToSeek(10)
            mViewBinding.superVodPlayerView.play(model.url)
            mViewBinding.superVodPlayerView.setAutoPlay(true)
            mViewBinding.superVodPlayerView.setPlayerViewCallback(object : SimPlayerCallBack() {
                override fun onClickShare() {
                    super.onClickShare()
                    "点击了分享".showToast()
                }

                override fun onStartFullScreenPlay() {
                    super.onStartFullScreenPlay()
                    "全屏播放".showToast()
                    ArmsUtils.fullScreencall(this@MainActivity)
                }

                override fun onStopFullScreenPlay() {
                    super.onStopFullScreenPlay()
                    "停止全屏播放".showToast()
                    ArmsUtils.statuInScreen2(this@MainActivity, true)
                }

                override fun onStartFloatWindowPlay() {
                    super.onStartFloatWindowPlay()
                    "悬浮窗播放".showToast()
                }

                override fun onClickFloatCloseBtn() {
                    super.onClickFloatCloseBtn()
                    "退出悬浮播放".showToast()
                }
            })
            mViewBinding.btnFloatView.setOnClickListener {
                mViewBinding.superVodPlayerView.switchPlayMode(SuperPlayerDef.PlayerMode.FLOAT)
                finish()
            }
            mViewBinding.btnKey.setOnClickListener {
                if (!iskey) {
                    mViewBinding.superVodPlayerView.setKeyList(
                        "测试列表",
                        KeyListAdapter(ArrayList<String>().apply {
                            repeat(10) {
                                add("播放item$it")
                            }
                        }).apply {
                            setOnItemClickListener { adapter, view, position ->
                                getItem(position).showToast()
                            }
                        },
                        onNext = {
                            it.toString().showToast()
                        })
                    iskey = true
                } else {
                    mViewBinding.superVodPlayerView.setKeyList(null, null)
                    iskey = false
                }
                mViewBinding.btnKey.text = "设置播放列表KeyList:$iskey"
            }

            mViewBinding.btnUrl.setOnClickListener {
                val model = SuperPlayerModel().apply {
                    val url =
                        "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4"
                    multiURLs = ArrayList<SuperPlayerModel.SuperPlayerURL>().apply {
                        add(SuperPlayerModel.SuperPlayerURL(url, "流程"))
                        add(SuperPlayerModel.SuperPlayerURL(url, "标清"))
                        add(SuperPlayerModel.SuperPlayerURL(url, "高清"))
                    }
                }
                mViewBinding.superVodPlayerView.playWithModel(model)
                mViewBinding.superVodPlayerView.setAutoPlay(true)
                mViewBinding.btnUrl.text = "已设置多url"
            }

            mViewBinding.btnTv.setOnClickListener {
                isTv = if (!isTv) {
                    mViewBinding.superVodPlayerView.setTVControl(object : TVControl {
                        override fun startShowTVLink() {
                            "点击了投屏".showToast()
                        }

                        override fun stopTV() {

                        }
                    })
                    true
                } else {
                    mViewBinding.superVodPlayerView.setTVControl(null)
                    false
                }
                mViewBinding.btnTv.text = "设置TV:$isTv"
            }
            mViewBinding.btnHide.setOnClickListener {
                GlobalConfig.instance.isHideAll = !GlobalConfig.instance.isHideAll
                if (GlobalConfig.instance.isHideAll) {
                    mViewBinding.superVodPlayerView.hide()
                }
                mViewBinding.btnHide.text = "隐藏控制：${GlobalConfig.instance.isHideAll}"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 重新开始播放
        if (mViewBinding.superVodPlayerView.playerState == SuperPlayerDef.PlayerState.PLAYING) {
            mViewBinding.superVodPlayerView.onResume()
            if (mViewBinding.superVodPlayerView.playerMode == SuperPlayerDef.PlayerMode.FLOAT) {
                mViewBinding.superVodPlayerView.switchPlayMode(SuperPlayerDef.PlayerMode.WINDOW)
            }
        }
    }


    override fun onPause() {
        super.onPause()
        // 停止播放
        if (mViewBinding.superVodPlayerView.playerMode != SuperPlayerDef.PlayerMode.FLOAT) {
            mViewBinding.superVodPlayerView.onPause()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mViewBinding.superVodPlayerView.playerMode != SuperPlayerDef.PlayerMode.FLOAT) {
            mViewBinding.superVodPlayerView.resetPlayer()
            mViewBinding.superVodPlayerView.release()
        }
    }
}