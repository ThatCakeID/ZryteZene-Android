package com.thatcakeid.zrytezene

import android.content.Context
import com.google.android.exoplayer2.upstream.cache.CacheEvictor
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import java.io.File

/**
 * This is a simple class that contains
 * the app's build extra information
 */
object ExtraMetadata {
    fun getExoPlayerCacheDir(mContext: Context): File {
        return File(mContext.cacheDir, "audioCache")
    }

    val exoPlayerCacheEvictor: CacheEvictor =
        LeastRecentlyUsedCacheEvictor((50 * 1024 * 1024).toLong()) // 50MiB cache size
}