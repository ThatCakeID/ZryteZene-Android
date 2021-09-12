package com.thatcakeid.zrytezene

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import android.widget.LinearLayout
import com.thatcakeid.zrytezene.ExtraMetadata
import com.google.android.exoplayer2.upstream.cache.CacheEvictor
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import java.io.File
import java.lang.IllegalStateException

/**
 * This is a simple class that contains
 * the app's build extra information
 */
object ExtraMetadata {
    const val SHOW_WATERMARK = true
    const val BUILD_INFO = "dev"
    @JvmStatic
    fun setWatermarkColors(watermark: TextView, root: LinearLayout) {
        val color: Int
        color = when (BUILD_INFO) {
            "dev" -> -0x48e3e4
            "beta" -> -0xde690d
            "stable" -> -0xd182ce
            else -> throw IllegalStateException("Unexpected value: " + BUILD_INFO)
        }
        watermark.setBackgroundColor(color)
        watermark.setTextColor(Color.parseColor("#FFFFFF"))
        root.visibility = if (SHOW_WATERMARK) View.VISIBLE else View.GONE
        watermark.text = BUILD_INFO
    }

    @JvmStatic
    fun getExoPlayerCacheDir(mContext: Context): File {
        return File(mContext.cacheDir, "audioCache")
    }

    // 50MB cache size
    @JvmStatic
    val exoPlayerCacheEvictor: CacheEvictor
        get() {
            val cacheSize = 50 * 1024 * 1024 // 50MB cache size
            return LeastRecentlyUsedCacheEvictor(cacheSize.toLong())
        }
}