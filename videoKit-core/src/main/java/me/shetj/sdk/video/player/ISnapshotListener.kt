package me.shetj.sdk.video.player

import android.graphics.Bitmap

interface ISnapshotListener {
    fun onSnapshot(bitmap: Bitmap?)
}