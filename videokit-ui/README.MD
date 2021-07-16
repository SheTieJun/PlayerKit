## 腾讯的超级播放器
> 对demo进行2次封装，优化和添加一下新的功能,因为是KPI,所以主要是为了自己的业务添加的功能做的优化实现

具备播放器基本功能，以及
1. 竖屏切换
2. 悬浮窗播放
3. 画质切换
4. 硬件加速
5. 倍速播放
6. 镜像播放
7. 手势控制
8. 支持直播与点播
9. 缓存播放
10. 截屏
11. 设置播放起始位置
12. 定时播放功能
13. 自动下一集功能
14. 自动开始播放
15. 界面控制
16. 播放回调

#### 基础功能播放
```
     /**
       * 开始播放
       *
       * @param url 视频地址
       */
      fun play(url: String?)

      /**
       * 开始播放
       *
       * @param appId 腾讯云视频appId
       * @param url   直播播放地址
       */
      fun play(appId: Int, url: String?)

      /**
       * 开始播放
       *
       * @param appId  腾讯云视频appId
       * @param fileId 腾讯云视频fileId
       * @param psign  防盗链签名，开启防盗链的视频必填，非防盗链视频可不填
       */
      fun play(appId: Int, fileId: String?, psign: String?)

      /**
       * 多分辨率播放
       * @param appId             腾讯云视频appId
       * @param superPlayerURLS   不同分辨率数据
       * @param defaultIndex      默认播放Index
       */
      fun play(appId: Int, superPlayerURLS: List<VideoPlayerModel.SuperPlayerURL?>?, defaultIndex: Int)


```

#### 1. 全屏切换
需要设置全屏时，显示的位置（必须）
```kotlin
player.setFullInWindow(this@MainActivity.window)
```

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
player.setMirror(false)
```
#### 7. 手势控制

#### 8. 支持直播与点播
设置播放后
内部通过播放url 判断是否是直播
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
1.  默认使用了腾讯云SDK 自带的缓存，
2.  可以用[videoCache](https://github.com/danikula/AndroidVideoCache)

#### 10. 截屏
```kotlin
player.snapshot(...)
```

#### 11. 设置播放起始位置
```kotlin
player.setPlayToSeek(10)
```

#### 12. 定时播放功能
1. 更多菜单里面点击设置
2. 通过代码设置
```
TimerConfigure.instance.showTimePick(this@MainActivity)
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

#### 14. 自动开始播放 和循环播放
需要在设置播放url:`play`方法之前设置，否则无效
```kotlin
player.autoPlay(true)
```
循环播放需要在播放结束之前设置才有效
```
player.isLoop = true
```

#### 15. 界面control控制以及播放配置控制
```kotlin
//后期可能会修改
player.updatePlayConfig( PlayerConfig)
player.superVodPlayerView.updateUIConfig(uiConfig.apply {
                    this.showTop = !isHide
                    this.showBottom = !isHide
                    this.showLock = !isHide
                })
isHide = !isHide
```
```
//全局配置相关
object GlobalConfig {
    var speed: Float = 1.0f //倍数
    var maxCacheItem = 5 //缓存数量
    var cacheMp4ExtName = "mp4" //缓存扩展名称，如果不想被手机扫描到视频可以改成其他,比如：XML
    var enableHWAcceleration = true //是否开启硬件加速
    var enableFloatWindow: Boolean = true //是否开启悬浮窗
 }
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

#### 17.雪碧图和视频关键点功能
1. 腾讯云自带，用fileId播放，设置`SuperPlayerHttpClient`
2. `updateImageSpriteAndKeyFrame` 通过该方法设置