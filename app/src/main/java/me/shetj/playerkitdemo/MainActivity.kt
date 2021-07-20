package me.shetj.playerkitdemo

import me.shetj.sdk.video.base.UIConfig
import me.shetj.sdk.video.base.VideoViewCallbackBuilder
import me.shetj.sdk.video.timer.TimerConfigure
import com.shetj.sdk.video.kit.PlayerKit
import me.shetj.base.ktx.*
import me.shetj.sdk.video.model.VideoPlayerModel
import me.shetj.base.mvvm.BaseBindingActivity
import me.shetj.base.mvvm.BaseViewModel
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.playerkitdemo.databinding.ActivityMainBinding
import me.shetj.sdk.video.player.PlayerDef
import me.shetj.sdk.video.TXVideoFactory

class MainActivity : BaseBindingActivity<BaseViewModel,ActivityMainBinding>() {
    private var isTv: Boolean = false
    private var iskey: Boolean = false
    protected var isAuto:Boolean = true //自定播放
    protected var isHide:Boolean = false
    private var isActivityPause = false
    override fun onActivityCreate() {
        super.onActivityCreate()
        ArmsUtils.statuInScreen2(this)
        initVideoInfo()
    }

    private fun initVideoInfo() {

        val uiConfig = UIConfig.uiConfig

        mViewBinding.superVodPlayerView.apply {
            val playerImpl = TXVideoFactory.getTXPlayer(this@MainActivity)
            updatePlayer(playerImpl) //设置播放器
            setPlayerView(playerImpl.playView) //设置播放的view
            updateFloatView(TXVideoFactory.getTXFloatView(this@MainActivity)) // 设置悬浮窗

            val model = VideoPlayerModel()
            model.url =
                "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4"
            mViewBinding.superVodPlayerView.autoPlay(isAuto)
            mViewBinding.superVodPlayerView.setPlayToSeek(10)
            mViewBinding.superVodPlayerView.play(model.url)
            mViewBinding.superVodPlayerView.setLoopPlay(true)

            mViewBinding.btnFloatView.setOnClickListener {
                mViewBinding.superVodPlayerView.switchPlayMode(PlayerDef.PlayerMode.FLOAT)
            }

            mViewBinding.btnUrl.setOnClickListener {
                mViewBinding.superVodPlayerView.autoPlay(isAuto)
                VideoPlayerModel().apply {
                    val url =
                        "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4"
                    multiURLs = ArrayList<VideoPlayerModel.SuperPlayerURL>().apply {
                        add(VideoPlayerModel.SuperPlayerURL(url, "流畅"))
                        add(VideoPlayerModel.SuperPlayerURL(url, "标清"))
                        add(VideoPlayerModel.SuperPlayerURL(url, "高清"))
                    }
                    mViewBinding.superVodPlayerView.playWithModel(this)
                    mViewBinding.btnUrl.text = "已设置多url"
                }
            }

            mViewBinding.btnKey.setOnClickListener {
                if (!iskey) {
                    //设置后可以自动或者手动设置
                    mViewBinding.superVodPlayerView.setKeyList(
                        "测试列表",
                        KeyListAdapter(ArrayList<String>().apply {
                            repeat(10) {
                                add("播放item$it")
                            }
                        }).apply {
                            setOnItemClickListener { _, _, position ->
                                getItem(position).showToast()
                            }
                        },
                        onNext = {
                            it.toString().showToast()
                            //这里做具体的下一集
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
                mViewBinding.superVodPlayerView.updateUIConfig(UIConfig.build {
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
                onError ={ _, message ->
                    "onError:$message".logi()
                }
                onComplete = {
                    "onComplete".logi()
                }
            })
            mViewBinding.btnHide.setOnClickListener {
                mViewBinding.superVodPlayerView.updateUIConfig(uiConfig.apply {
                    this.showTop = !isHide
                    this.showBottom = !isHide
                    this.showLock = !isHide
                })
                isHide = !isHide
            }
            mViewBinding.btnShowSpeed.setOnClickListener {
                TimerConfigure.instance.showTimePick(this@MainActivity)
            }
            mViewBinding.btnTestGo.setOnClickListener {
                start<SplashActivity>()
            }
            mViewBinding.btnTestHideFull.setOnClickListener {
                mViewBinding.superVodPlayerView.updateUIConfig(uiConfig.apply {
                    this.showFull = !isHide
                })
                isHide = !isHide
            }
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
        addKeepScreenOn()
        "onResume".logi()
        if (mViewBinding.superVodPlayerView.playerState !=  PlayerDef.PlayerState.PLAYING && isActivityPause) {
            isActivityPause = false
            mViewBinding.superVodPlayerView.resume()
        }
    }


    override fun onPause() {
        super.onPause()
        clearKeepScreenOn()
        // 停止播放
        "onPause".logi()
        if (mViewBinding.superVodPlayerView.playerMode !=  PlayerDef.PlayerMode.FLOAT) {
            isActivityPause = true
            mViewBinding.superVodPlayerView.pause()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mViewBinding.superVodPlayerView.playerMode !=  PlayerDef.PlayerMode.FLOAT) {
            mViewBinding.superVodPlayerView.destroy()
            mViewBinding.superVodPlayerView.release()
        }
    }
}