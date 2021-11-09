package com.thatcakeid.zrytezene.ui

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.thatcakeid.zrytezene.databinding.ActivityHomeBinding
import com.thatcakeid.zrytezene.databinding.SheetFpuBinding
import com.thatcakeid.zrytezene.services.MusicService

class HomeActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    private val fpuBinding by lazy { SheetFpuBinding.inflate(layoutInflater) }
    private lateinit var mMediaBrowserCompat : MediaBrowserCompat

    private var isServiceConnected = false
    private var playerState = 0

    private val connectionCallback : MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            mMediaBrowserCompat.sessionToken.also { token ->
                val mediaController = MediaControllerCompat(this@HomeActivity, token)
                MediaControllerCompat.setMediaController(this@HomeActivity, mediaController)
            }
            isServiceConnected = true
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            Toast.makeText(applicationContext, "ERROR: Failed to connect to the service", Toast.LENGTH_LONG).show()
        }
    }

    private val bottomSheetBehavior: BottomSheetBehavior<*> by lazy {
        BottomSheetBehavior
            .from(fpuBinding.sheetRoot)
            .also {
                it.state = BottomSheetBehavior.STATE_HIDDEN
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mMediaBrowserCompat = MediaBrowserCompat(
            this,
            ComponentName(this, MusicService::class.java),
            connectionCallback,
            null
        )

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    fpuBinding.fpuCompactPlayer.visibility = View.INVISIBLE
                    // TODO: Stop the media player here
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    fpuBinding.fpuCompactPlayer.visibility = View.INVISIBLE
                } else {
                    fpuBinding.fpuCompactPlayer.visibility = View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                fpuBinding.apply {
                    arrowFpuCompactPlayer.rotation =
                        if (slideOffset >= 0) slideOffset * 180f else 0f

                    fpuCompactPlayer.alpha =
                        if (slideOffset >= 0) 1 - slideOffset else slideOffset + 1
                }
            }
        })

        fpuBinding.apply {
            arrowFpuCompactPlayer.setOnClickListener {
                bottomSheetBehavior.setState(
                    if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                        BottomSheetBehavior.STATE_EXPANDED
                    else
                        BottomSheetBehavior.STATE_COLLAPSED
                )
            }

            playFpu.setOnClickListener { togglePlay() }
            playFpuCompactPlayer.setOnClickListener { togglePlay() }
        }
    }

    private fun togglePlay() {
        if (playerState == PlaybackStateCompat.STATE_PLAYING) {
            mediaController.transportControls.pause()
        } else mediaController.transportControls.play()
    }
}