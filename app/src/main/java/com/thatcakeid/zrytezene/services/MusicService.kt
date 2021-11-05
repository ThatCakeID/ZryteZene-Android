package com.thatcakeid.zrytezene.services

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.thatcakeid.zrytezene.ExtraMetadata

class MusicService : MediaBrowserServiceCompat() {
    private var mMediaSession: MediaSessionCompat? = null
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder

    private var mExoPlayer: SimpleExoPlayer? = null
    private var audioSpeed = 0f

    private val playbackStateListener = PlaybackStateListener()

    private var oldUri: Uri? = null
    private val mMediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            if (uri != oldUri)
                play(uri!!)
            else play()
            oldUri = uri
        }

        override fun onPause() {
            super.onPause()
            pause()
        }

        override fun onStop() {
            super.onStop()
            stop()
        }

        override fun onPlay() {
            super.onPlay()
            play()
        }
    }

    private val audioAttributes: AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        mMediaSession = MediaSessionCompat(baseContext, "tag for debugging").apply {
            mStateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)

            setPlaybackState(mStateBuilder.build())
            setCallback(mMediaSessionCallback)

            setSessionToken(sessionToken)
            isActive = true
        }
    }

    private fun initializePlayer() {
        val cache = SimpleCache(
            ExtraMetadata.getExoPlayerCacheDir(applicationContext),
            ExtraMetadata.exoPlayerCacheEvictor,
            ExoDatabaseProvider(applicationContext)
        )

        val mediaSourceFactory = DefaultMediaSourceFactory(
            CacheDataSource.Factory().setCache(cache).setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory().setUserAgent("ZryteZene")
            )
        )

        mExoPlayer = SimpleExoPlayer.Builder(applicationContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .also {
                it.setAudioAttributes(audioAttributes, true)
                it.addListener(playbackStateListener)
            }
    }

    private fun play(uri: Uri) {
        mExoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
        }
    }

    private fun play() {
        mExoPlayer?.apply {
            play()
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            mMediaSession?.isActive = true
        }
    }

    private fun pause() {
        mExoPlayer?.apply {
            pause()
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    private fun stop() {
        mExoPlayer?.stop()
        mExoPlayer?.release()
        mExoPlayer = null
        updatePlaybackState(PlaybackStateCompat.STATE_NONE)
        mMediaSession?.isActive = false
        mMediaSession?.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot {
        return BrowserRoot("", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    private fun updatePlaybackState(state: Int) {
        mMediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                state,
                mExoPlayer!!.currentPosition,
                audioSpeed
            ).build()
        )
    }

    private inner class PlaybackStateListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            when(playbackState) {
                ExoPlayer.STATE_READY -> {
                    play()
                }
                ExoPlayer.STATE_ENDED -> {
                    stop()
                }
                Player.STATE_BUFFERING -> {
                    TODO()
                }
                Player.STATE_IDLE -> {
                    TODO()
                }
            }
        }
    }
}