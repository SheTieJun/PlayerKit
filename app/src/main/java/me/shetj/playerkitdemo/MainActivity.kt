package me.shetj.playerkitdemo

import com.tencent.video.superplayer.viedoview.base.SuperPlayerDef
import com.tencent.video.superplayer.base.UIConfig
import com.tencent.video.superplayer.base.VideoViewCallbackBuilder
import com.tencent.video.superplayer.kit.PlayerKit
import com.tencent.video.superplayer.viedoview.model.SuperPlayerModel
import me.shetj.base.ktx.logi
import me.shetj.base.ktx.showToast
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
            mViewBinding.superVodPlayerView.autoPlay(true)

            mViewBinding.btnFloatView.setOnClickListener {
                mViewBinding.superVodPlayerView.switchPlayMode(SuperPlayerDef.PlayerMode.FLOAT)
                finish()
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
                mViewBinding.superVodPlayerView.autoPlay(true)
                mViewBinding.btnUrl.text = "已设置多url"
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
                }else{
                    mViewBinding.superVodPlayerView.setKeyList(null,null,0,null)
                    iskey = false
                }
                mViewBinding.btnKey.text = "设置播放列表KeyList:$iskey"
            }

            /**
             * 设置全屏显示的window
             */
            mViewBinding.superVodPlayerView.setFullInWindow(this@MainActivity.window)

            mViewBinding.btnTv.setOnClickListener {
                isTv = !isTv
                mViewBinding.superVodPlayerView.setUIConfig(UIConfig.build {
                    this.showTV = isTv
                })
                mViewBinding.btnTv.text = "设置TV:$isTv"
            }

            mViewBinding.superVodPlayerView.setPlayerCallback(VideoViewCallbackBuilder.build{
                onStart = {
                    "onStart".logi()
                }
                onPlayProgress = { current, duration ->
                    "onPlayProgress:$current$duration".logi()
                }
                onPause ={
                    "onPause".logi()
                }
            })
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        PlayerKit.onWindowFocusChanged(window,hasFocus =hasFocus )
    }

    override fun onBackPressed() {
        if (mViewBinding.superVodPlayerView.onBackPressed()){
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        // 重新开始播放
        if (mViewBinding.superVodPlayerView.playerState == SuperPlayerDef.PlayerState.PLAYING) {
            mViewBinding.superVodPlayerView.resume()
            if (mViewBinding.superVodPlayerView.playerMode == SuperPlayerDef.PlayerMode.FLOAT) {
                mViewBinding.superVodPlayerView.switchPlayMode(SuperPlayerDef.PlayerMode.WINDOW)
            }
        }
    }


    override fun onPause() {
        super.onPause()
        // 停止播放
        if (mViewBinding.superVodPlayerView.playerMode != SuperPlayerDef.PlayerMode.FLOAT) {
            mViewBinding.superVodPlayerView.pause()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mViewBinding.superVodPlayerView.playerMode != SuperPlayerDef.PlayerMode.FLOAT) {
            mViewBinding.superVodPlayerView.destroy()
            mViewBinding.superVodPlayerView.release()
        }
    }
}