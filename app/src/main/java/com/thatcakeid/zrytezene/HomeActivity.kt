package com.thatcakeid.zrytezene

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.thatcakeid.zrytezene.ExtraMetadata.getExoPlayerCacheEvictor
import com.thatcakeid.zrytezene.ExtraMetadata.getExoPlayerCacheDir
import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import com.thatcakeid.zrytezene.HelperClass.Companion.parseDuration
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter.ClickListener
import com.thatcakeid.zrytezene.databinding.ActivityHomeBinding
import com.thatcakeid.zrytezene.databinding.SheetFpuBinding
import java.util.*

class HomeActivity : AppCompatActivity() {
    private val binding: ActivityHomeBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    private val fpuBinding: SheetFpuBinding by lazy { SheetFpuBinding.inflate(layoutInflater) }

    private var musicEntries: ArrayList<HashMap<String, Any>> = ArrayList()
    private var musicIndexes: ArrayList<String> = ArrayList()
    private var userIndexes: HashMap<String, String> = HashMap()
    private var currentPlaylist: ArrayList<HashMap<String, Any>>? = null
    private var playlistIndex: ArrayList<String>? = null

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val musicCollection: CollectionReference by lazy {
        FirebaseFirestore.getInstance().collection("musics")
    }

    private val userCollection: CollectionReference by lazy {
        FirebaseFirestore.getInstance().collection("users")
    }

    private val bottomSheetBehavior: BottomSheetBehavior<*> by lazy {
        BottomSheetBehavior
            .from(findViewById(R.id.sheet_root))
            .also {
                it.state = BottomSheetBehavior.STATE_HIDDEN
            }
    }

    private val exoPlayer: SimpleExoPlayer by lazy {
        val cache = SimpleCache(
            getExoPlayerCacheDir(applicationContext),
            getExoPlayerCacheEvictor,
            ExoDatabaseProvider(applicationContext)
        )

        val mediaSourceFactory = DefaultMediaSourceFactory(
            CacheDataSource.Factory().setCache(cache).setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory().setUserAgent("ZryteZene")
            )
        )

        SimpleExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .also {
                it.setAudioAttributes(audioAttributes, true)
                it.addListener(playbackStateListener)
            }
    }

    private val audioAttributes: AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()

    private val playbackStateListener = PlaybackStateListener()

    private var handler: Handler = Handler()
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            fpuBinding.apply {
                if (exoPlayer.isPlaying) {
                    seekBar.progress = exoPlayer.currentPosition.toInt() / 100

                    imageView5.setImageResource(R.drawable.ic_pause)
                    imageView8.setImageResource(R.drawable.ic_pause)
                } else {
                    imageView5.setImageResource(R.drawable.ic_play_arrow)
                    imageView8.setImageResource(R.drawable.ic_play_arrow)
                }

                val progress = exoPlayer.bufferedPercentage * exoPlayer.duration.toInt() / 10000

                seekBar.secondaryProgress = progress
                progressBar3.secondaryProgress = progress
            }

            handler.postDelayed(this, 100)
        }
    }

    private var currentPos = -1
    private var isReady = false
    private var isDragging = false

    private var preferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initializeVar()
        refreshRepeatState(preferences!!.getInt("playMode", 0))

        FirebaseApp.initializeApp(this)

        binding.userAppbarHome.setOnClickListener {
            startActivity(
                if (auth.currentUser == null) {
                    Intent(this@HomeActivity, LoginActivity::class.java)
                } else {
                    Intent(this@HomeActivity, ProfileActivity::class.java).apply {
                        putExtra("uid", auth.uid)
                    }
                }
            )
        }

        fpuBinding.apply {
            imageView2.setOnClickListener {
                bottomSheetBehavior.setState(
                    if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                        BottomSheetBehavior.STATE_EXPANDED
                    else
                        BottomSheetBehavior.STATE_COLLAPSED
                )
            }

            imageView4.setOnClickListener { playPrevious() }
            imageView6.setOnClickListener { playNext() }
            imageView5.setOnClickListener { togglePlay() }
            imageView8.setOnClickListener { togglePlay() }
            imageView9.setOnClickListener {
                var mode = preferences!!.getInt("playMode", 0)

                if (mode < 3) {
                    mode++
                } else {
                    mode = 0
                }

                preferences!!.edit().putInt("playMode", mode).apply()
                refreshRepeatState(mode)
            }
        }

        val adapter = HomeItemsRecyclerViewAdapter(
            applicationContext,
            musicEntries,
            userIndexes
        )

        adapter.setOnItemClickListener(object : ClickListener {
            override fun onItemClick(position: Int, v: View) {
                currentPlaylist = ArrayList(musicEntries)
                playlistIndex = ArrayList(musicIndexes)
                currentPos = position

                play()
            }

            override fun onItemLongClick(position: Int, v: View) {}
        })

        fpuBinding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                fpuBinding.textView9.text = parseDuration((progress * 100).toLong())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fpuBinding.progressBar3.setProgress(progress, true)
                } else {
                    fpuBinding.progressBar3.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isDragging = true
                exoPlayer.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isDragging = false

                exoPlayer.seekTo((seekBar.progress * 100).toLong())
                exoPlayer.play()
            }
        })

        binding.rvItemsHome.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            this.adapter = adapter
        }

        userCollection.addSnapshotListener { value: QuerySnapshot?, _ ->
            if (value == null) {
                Toast.makeText(
                    this@HomeActivity,
                    "An error occurred whilst trying to update users: value is null",
                    Toast.LENGTH_SHORT
                ).show()

                return@addSnapshotListener
            }

            for (dc in value.documentChanges) {
                val data = dc.document.data as HashMap<String, Any>

                when (dc.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        userIndexes[dc.document.id] = data["username"] as String

                        if (dc.document.id == auth.uid) {
                            if (data["img_url"] == "") {
                                binding.userAppbarHome.imageTintList = ContextCompat.getColorStateList(
                                        applicationContext,
                                        R.color.imageTint
                                )

                                binding.userAppbarHome.setImageResource(R.drawable.ic_account_circle)
                            } else {
                                binding.userAppbarHome.imageTintList = null
                                Glide.with(applicationContext)
                                        .load(data["img_url"] as String?)
                                        .into(binding.userAppbarHome)
                            }
                        }
                    }

                    DocumentChange.Type.REMOVED -> {
                        userIndexes.remove(dc.document.id)

                        if (dc.document.id == auth.uid) {
                            binding.userAppbarHome.imageTintList = ContextCompat.getColorStateList(
                                    applicationContext,
                                    R.color.imageTint
                            )

                            binding.userAppbarHome.setImageResource(R.drawable.ic_account_circle)
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }
        }

        musicCollection.addSnapshotListener { value: QuerySnapshot?, _ ->
            if (value == null) {
                Toast.makeText(
                    this@HomeActivity,
                    "An error occurred whilst trying to update musics: value is null",
                    Toast.LENGTH_SHORT
                ).show()

                return@addSnapshotListener
            }

            for (dc in value.documentChanges) {
                val data = dc.document.data as HashMap<String, Any>
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        musicEntries.add(data)
                        musicIndexes.add(dc.document.id)
                    }

                    DocumentChange.Type.MODIFIED -> {
                        musicEntries[musicIndexes.indexOf(dc.document.id)] = data

                        if (currentPlaylist != null) currentPlaylist!![playlistIndex!!.indexOf(dc.document.id)] = data
                    }

                    DocumentChange.Type.REMOVED -> {
                        musicEntries.remove(data)
                        musicIndexes.remove(dc.document.id)

                        if (currentPlaylist != null) {
                            currentPlaylist!!.remove(data)
                            playlistIndex!!.remove(dc.document.id)
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && currentPos != -1) {
                    stop()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                fpuBinding.apply {
                    imageView2.rotation =
                        if (slideOffset >= 0) slideOffset * 180f else 0f

                    compactPlayer.alpha =
                        if (slideOffset >= 0) 1 - slideOffset else slideOffset + 1

                    compactPlayer.visibility =
                        if (slideOffset < 1 && slideOffset > -1) View.VISIBLE else View.INVISIBLE
                }
            }
        })
    }

    private fun initializeVar() {
        setWatermarkColors(binding.textWatermark, binding.watermarkRoot)

        fpuBinding.textView4.isSelected = true
        fpuBinding.textView7.isSelected = true

        preferences = getSharedPreferences(getString(R.string.main_preferences_key), MODE_PRIVATE)
    }

    private fun play() {
        if (currentPlaylist != null && currentPos > -1) {
            handler.removeCallbacks(runnable)

            exoPlayer.stop()
            isReady = false

            exoPlayer.setMediaItem(MediaItem.fromUri((currentPlaylist!![currentPos]["music_url"] as String?)!!))
            exoPlayer.prepare()

            fpuBinding.apply {
                textView7.text = currentPlaylist!![currentPos]["title"] as String?
                textView8.text = if (userIndexes.containsKey(currentPlaylist!![currentPos]["author"] as String?)) userIndexes[currentPlaylist!![currentPos]["author"] as String?] else currentPlaylist!![currentPos]["author"] as String?
                textView4.text = currentPlaylist!![currentPos]["title"] as String?
                textView6.text = if (userIndexes.containsKey(currentPlaylist!![currentPos]["author"] as String?)) userIndexes[currentPlaylist!![currentPos]["author"] as String?] else currentPlaylist!![currentPos]["author"] as String?

                resetProgressBar()

                textView9.text = "--:--"
                textView10.text = "--:--"

                if (currentPlaylist!![currentPos]["thumb"] == "") {
                    imageView3.setImageResource(R.drawable.ic_zrytezene)
                    imageView7.setImageResource(R.drawable.ic_zrytezene)

                } else {
                    Glide.with(applicationContext)
                        .load(currentPlaylist!![currentPos]["thumb"] as String?)
                        .into(imageView3)

                    Glide.with(applicationContext)
                        .load(currentPlaylist!![currentPos]["thumb"] as String?)
                        .into(imageView7)
                }

                seekBar.isEnabled = false

                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                musicCollection
                    .document(playlistIndex!![currentPos])
                    .update("plays", (currentPlaylist!![currentPos]["plays"] as Number).toInt() + 1)
            }

        } else {
            Toast.makeText(this,
                    "An error occurred while trying to play a music, playlist is empty",
                    Toast.LENGTH_LONG).show()
        }
    }

    private fun togglePlay() = if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()

    private fun playNext() {
        when (preferences!!.getInt("playMode", 0)) {
            0 -> if (currentPos + 1 < currentPlaylist!!.size) {
                currentPos++
                play()
            } else {
                stop()
            }

            1 -> {
                if (currentPos + 1 < currentPlaylist!!.size) {
                    currentPos++
                } else {
                    currentPos = 0
                }
                play()
            }

            2 -> play()
            3 -> {
                currentPos = Random().nextInt(currentPlaylist!!.size)
                play()
            }
        }
    }

    private fun playPrevious() {
        when (preferences!!.getInt("playMode", 0)) {
            0 -> if (currentPos > 0) {
                currentPos--
                play()
            } else stop()

            1 -> {
                if (currentPos > 0) {
                    currentPos--
                } else {
                    currentPos = currentPlaylist!!.size - 1
                }

                play()
            }

            2 -> play()

            3 -> {
                currentPos = Random().nextInt(currentPlaylist!!.size)
                play()
            }
        }
    }

    private fun stop() {
        handler.removeCallbacks(runnable)
        exoPlayer.stop()
        isReady = false
        currentPos = -1
        currentPlaylist = null
        playlistIndex = null
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun resetProgressBar() {
        fpuBinding.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                seekBar.setProgress(0, true)
            } else {
                seekBar.progress = 0
            }

            seekBar.max = 100
            seekBar.secondaryProgress = 0
            progressBar3.max = 100
            progressBar3.secondaryProgress = 0
        }
    }

    private inner class PlaybackStateListener : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            fpuBinding.apply {
                when (state) {
                    ExoPlayer.STATE_READY -> {
                        if (!isReady) {
                            textView9.text = "0:00"
                            textView10.text = parseDuration(exoPlayer.duration)

                            seekBar.max = exoPlayer.duration.toInt() / 100
                            seekBar.isEnabled = true

                            progressBar3.max = exoPlayer.duration.toInt() / 100

                            isReady = true
                            handler.post(runnable)
                            exoPlayer.play()
                        }

                        progressBar.visibility = View.INVISIBLE
                        progressBar2.visibility = View.INVISIBLE

                        imageView5.visibility = View.VISIBLE
                        imageView8.visibility = View.VISIBLE
                    }

                    ExoPlayer.STATE_BUFFERING -> {
                        progressBar.visibility = View.VISIBLE
                        progressBar2.visibility = View.VISIBLE

                        imageView5.visibility = View.INVISIBLE
                        imageView8.visibility = View.INVISIBLE
                    }

                    ExoPlayer.STATE_ENDED -> playNext()
                }
            }
        }
    }

    private fun refreshRepeatState(mode: Int) {
        fpuBinding.imageView9.apply {
            alpha = 1.0f

            when (mode) {
                0 -> {
                    alpha = 0.5f
                    setImageResource(R.drawable.ic_repeat)
                }

                1 -> setImageResource(R.drawable.ic_repeat)
                2 -> setImageResource(R.drawable.ic_repeat_one)
                3 -> setImageResource(R.drawable.ic_shuffle)
            }
        }
    }
}