## 腾讯的超级播放器
> 对他的demo 进行2次封装，优化和添加一下新的功能

具备播放器基本功能，以及
1. 竖屏切换
2. 悬浮窗播放
3. 画质切换
4. 硬件加速
5. 倍速播放
6. 镜像播放
7. 手势控制
8. 支持直播与点播
9. 功能缓存
10. 截屏
11. 设置播放起始位置
12. 定时播放功能
13. 自动下一集功能
14. 自动开始播放
15. 界面控制



#### 1. 全屏切换

```kotlin
//一般不需自己进行设置
player.switchPlayMode(PlayerMode.WINDOW) //小屏
player.switchPlayMode(PlayerMode.FULLSCREEN) //全屏
```
#### 2. 悬浮窗播放
权限：  ` <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />`
```kotlin
player.switchPlayMode(PlayerMode.FLOAT) //设置
```

#### 3. 画质切换
1. 通过腾讯云自己的的API获取到不同的画质
2. 通过设置播放参数
```kotlin
   SuperPlayerModel().apply {
        val url =  "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4"
        multiURLs = ArrayList<SuperPlayerModel.SuperPlayerURL>().apply {
            add(SuperPlayerModel.SuperPlayerURL(url, "流程"))
            add(SuperPlayerModel.SuperPlayerURL(url, "标清"))
            add(SuperPlayerModel.SuperPlayerURL(url, "高清"))
        }
        player.playWithModel(this)
   }
```

#### 4. 硬件加速
```kotlin
player.enableHardwareDecode(true)
```

#### 5. 倍速播放
```kotlin
player.setPlaySpeed(1.0f)
```
#### 6. 镜像播放
```kotlin
player.setPlaySpeed(1.0f)
```
#### 7. 手势控制

#### 8. 支持直播与点播
通过播放url 判断是否是直播
```
    /**
     * 是否是RTMP协议
     *
     * @param videoURL
     * @return
     */
    private fun isRTMPPlay(videoURL: String?): Boolean {
        return !TextUtils.isEmpty(videoURL) && videoURL!!.startsWith("rtmp")
    }

    /**
     * 是否是HTTP-FLV协议
     *
     * @param videoURL
     * @return
     */
    private fun isFLVPlay(videoURL: String?): Boolean {
        return (!TextUtils.isEmpty(videoURL) && videoURL!!.startsWith("http://")
                || videoURL!!.startsWith("https://")) && videoURL.contains(".flv")
    }

```

#### 9. 功能缓存
SDK 自带的缓存

#### 10. 截屏
```kotlin
player.snapshot(...)
```

#### 11. 设置播放起始位置
```kotlin
player.setPlayToSeek(10)
```

#### 12. 定时播放功能
1. 更多菜单里面
2. 通过代码设置
```
TimerConfigure.instance.startTime(10*60*1000)
```

#### 13. 自动下一集功能
```
player.setKeyList("测试列表",
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

 player.nextOneKey()//下一集，触发 onNext
```

#### 14. 自动开始播放
```kotlin
player.autoPlay(true)
```

#### 15. 界面控制
```kotlin
//后期可能会修改
player. setPlayConfig( PlayerConfig)
player.setUIConfig(UIConfig.build {
                    this.showTV = false
                })
```

#### 16.播放回调
```
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
                //...其他
            })
```