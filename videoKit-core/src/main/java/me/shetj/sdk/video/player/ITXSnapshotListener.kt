package me.shetj.sdk.video.player

import android.graphics.Bitmap

interface ITXSnapshotListener {
    fun onSnapshot(bitmap: Bitmap?)
}