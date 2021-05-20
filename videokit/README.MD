### 腾讯的超级播放器
```
https://cloud.tencent.com/document/product/266/45554 参数说明，如果有什么不对就发给后端
```

```
        val model = SuperPlayerModel()
//        model.url =
//            "https://vod2.lycheer.net/73e20c6evodtranscq1253442168/d238a8f65285890807609259635/v.f117442.mp4?t=5fdb67b0&us=ab85cd03d9&sign=2e34d0d67a2f4272d1edf3ad421bbf93"
        model.url =
            "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4"
        mViewBinding.superVodPlayerView.setPlayToSeek(10)
        mViewBinding.superVodPlayerView.playWithModel(model)
        mViewBinding.superVodPlayerView.setAutoPlay(true)
        mViewBinding.superVodPlayerView.setPlayerViewCallback(object : SimPlayerCallBack() {
            override fun onClickShare() {
                super.onClickShare()
                "点击了分享".showToast()
            }

            override fun onStartFullScreenPlay() {
                super.onStartFullScreenPlay()
                "全屏播放".showToast()
                ArmsUtils.fullScreencall(this@TXPlayerActivity)
            }

            override fun onStopFullScreenPlay() {
                super.onStopFullScreenPlay()
                "停止全屏播放".showToast()
                ArmsUtils.statuInScreen2(this@TXPlayerActivity,true)
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
```

 悬浮窗
```
        mViewBinding.btnFloatView.setOnClickListener {
            mViewBinding.superVodPlayerView.switchPlayMode(SuperPlayerDef.PlayerMode.FLOAT)
            finish()
        }
```

设置列表key 切换
```
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
                mViewBinding.superVodPlayerView.setKeyList(null,null)
                iskey = false
            }
            mViewBinding.btnKey.text = "设置播放列表KeyList:$iskey"
        }
```

设置多url
```
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
```
投屏按钮回调
```

        mViewBinding.btnTv.setOnClickListener {
            isTv = if (!isTv) {
                mViewBinding.superVodPlayerView.setTVControl(object : TVControl {
                    override fun startShowTVLink() {
                        "点击了投屏".showToast()
                    }
                })
                true
            }else{
                mViewBinding.superVodPlayerView.setTVControl(null)
                false
            }
            mViewBinding.btnTv.text = "设置TV:$isTv"
        }
```
隐藏全部的view
```
        mViewBinding.btnHide.setOnClickListener {
            GlobalConfig.instance.isHideAll = !GlobalConfig.instance.isHideAll
            if (GlobalConfig.instance.isHideAll){
                mViewBinding.superVodPlayerView.hideAll()
            }
            mViewBinding.btnHide.text = "隐藏控制：${GlobalConfig.instance.isHideAll}"
        }
```