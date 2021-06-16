package me.shetj.playerkitdemo

import android.content.Intent
import com.tencent.video.superplayer.base.UIConfig
import com.tencent.video.superplayer.base.VideoViewCallbackBuilder
import com.tencent.video.superplayer.base.timer.TimerConfigure
import com.tencent.video.superplayer.kit.PlayerKit
import com.tencent.video.superplayer.viedoview.base.SuperPlayerDef
import com.tencent.video.superplayer.viedoview.model.VideoPlayerModel
import me.shetj.base.ktx.logi
import me.shetj.base.ktx.showToast
import me.shetj.base.ktx.start
import me.shetj.base.mvvm.BaseBindingActivity
import me.shetj.base.mvvm.BaseViewModel
import me.shetj.base.tools.app.ArmsUtils
import me.shetj.playerkitdemo.databinding.ActivityMainBinding

class MainActivity : BaseBindingActivity<BaseViewModel,ActivityMainBinding>() {
    private var isTv: Boolean = false
    private var iskey: Boolean = false
    protected var isAuto:Boolean = false //自定播放
    protected var isHide:Boolean = false
    override fun onActivityCreate() {
        super.onActivityCreate()
        ArmsUtils.statuInScreen2(this)
        initVideoInfo()
    }

    override fun onNewIntent(intent: Intent?) {
    }

    private fun initVideoInfo() {

        val uiConfig = UIConfig.uiConfig

        mViewBinding.superVodPlayerView.apply {
            val model = VideoPlayerModel()
            model.url =
                "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4"
            mViewBinding.superVodPlayerView.autoPlay(isAuto)
            mViewBinding.superVodPlayerView.setPlayToSeek(10)
            mViewBinding.superVodPlayerView.play(model.url)
            mViewBinding.superVodPlayerView.isLoop = true
            mViewBinding.btnFloatView.setOnClickListener {
                mViewBinding.superVodPlayerView.switchPlayMode(SuperPlayerDef.PlayerMode.FLOAT)
            }

            mViewBinding.btnUrl.setOnClickListener {
                mViewBinding.superVodPlayerView.autoPlay(isAuto)
                VideoPlayerModel().apply {
                    val url =
                        "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4"
                    multiURLs = ArrayList<VideoPlayerModel.SuperPlayerURL>().apply {
                        add(VideoPlayerModel.SuperPlayerURL(url, "流程"))
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
        "onResume".logi()
        if (mViewBinding.superVodPlayerView.playerState != SuperPlayerDef.PlayerState.PLAYING) {
            mViewBinding.superVodPlayerView.resume()
        }
    }


    override fun onPause() {
        super.onPause()
        // 停止播放
        "onPause".logi()
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