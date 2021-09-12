package com.thatcakeid.zrytezene

import com.thatcakeid.zrytezene.HelperClass.Companion.parseDuration
import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import com.thatcakeid.zrytezene.ExtraMetadata.getExoPlayerCacheDir
import com.thatcakeid.zrytezene.ExtraMetadata.exoPlayerCacheEvictor
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.thatcakeid.zrytezene.HomeActivity.PlaybackStateListener
import android.content.SharedPreferences
import android.os.Bundle
import android.content.Intent
import com.thatcakeid.zrytezene.LoginActivity
import com.thatcakeid.zrytezene.ProfileActivity
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter.ClickListener
import android.widget.SeekBar.OnSeekBarChangeListener
import com.thatcakeid.zrytezene.HelperClass
import android.os.Build
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.ContextCompat
import com.thatcakeid.zrytezene.R
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.thatcakeid.zrytezene.ExtraMetadata
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.firebase.FirebaseApp
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.firebase.firestore.*
import com.thatcakeid.zrytezene.databinding.ActivityHomeBinding
import java.util.*

class HomeActivity : AppCompatActivity() {
    private var view: View? = null
    private var binding: ActivityHomeBinding? = null
    private var compactPlayer: ConstraintLayout? = null
    private var rv_items_home: RecyclerView? = null
    private var textView4: TextView? = null
    private var textView6: TextView? = null
    private var textView7: TextView? = null
    private var textView8: TextView? = null
    private var textView9: TextView? = null
    private var textView10: TextView? = null
    private var user_appbar_home: ShapeableImageView? = null
    private var imageView2: ImageView? = null
    private var imageView3: ImageView? = null
    private var imageView4: ImageView? = null
    private var imageView5: ImageView? = null
    private var imageView6: ImageView? = null
    private var imageView7: ImageView? = null
    private var imageView8: ImageView? = null
    private var imageView9: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var progressBar2: ProgressBar? = null
    private var progressBar3: ProgressBar? = null
    private var seekBar: SeekBar? = null
    private var musics_entries: ArrayList<HashMap<String, Any>>? = null
    private var currentPlaylist: ArrayList<HashMap<String, Any>>? = null
    private var musics_indexes: ArrayList<String>? = null
    private var playlistIndex: ArrayList<String>? = null
    private var user_indexes: HashMap<String?, String?>? = null
    private var auth: FirebaseAuth? = null
    private var musics_db: CollectionReference? = null
    private var users_db: CollectionReference? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var exoPlayer: SimpleExoPlayer? = null
    private var audioAttributes: AudioAttributes? = null
    private var playbackStateListener: PlaybackStateListener? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var currentPos = -1
    private var isReady = false
    private var isDragging = false
    private var preferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        view = binding!!.root
        setContentView(view)
        initializeVar()
        refreshRepeatState(preferences!!.getInt("playMode", 0))
        user_appbar_home!!.setOnClickListener { v: View? ->
            if (auth!!.currentUser == null) {
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
            } else {
                val profile_intent = Intent(this@HomeActivity, ProfileActivity::class.java)
                profile_intent.putExtra("uid", auth!!.uid)
                startActivity(profile_intent)
            }
        }
        imageView2!!.setOnClickListener { v: View? ->
            bottomSheetBehavior!!.setState(if (bottomSheetBehavior!!.state ==
                    BottomSheetBehavior.STATE_COLLAPSED) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED)
        }
        imageView4!!.setOnClickListener { v: View? -> playPrevious() }
        imageView6!!.setOnClickListener { v: View? -> playNext() }
        imageView5!!.setOnClickListener { v: View? -> togglePlay() }
        imageView8!!.setOnClickListener { v: View? -> togglePlay() }
        imageView9!!.setOnClickListener { v: View? ->
            var mode = preferences!!.getInt("playMode", 0)
            if (mode < 3) {
                mode++
            } else {
                mode = 0
            }
            preferences!!.edit().putInt("playMode", mode).apply()
            refreshRepeatState(mode)
        }
        val adapter = HomeItemsRecyclerViewAdapter(
                applicationContext,
                musics_entries,
                user_indexes
        )
        adapter.setOnItemClickListener(object : ClickListener {
            override fun onItemClick(position: Int, v: View) {
                currentPlaylist = ArrayList(musics_entries)
                playlistIndex = ArrayList(musics_indexes)
                currentPos = position
                play()
            }

            override fun onItemLongClick(position: Int, v: View) {}
        })
        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView9!!.text = parseDuration((progress * 100).toLong())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar3!!.setProgress(progress, true)
                } else {
                    progressBar3!!.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isDragging = true
                exoPlayer!!.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isDragging = false
                exoPlayer!!.seekTo((seekBar.progress * 100).toLong())
                exoPlayer!!.play()
            }
        })
        rv_items_home!!.layoutManager = LinearLayoutManager(applicationContext)
        rv_items_home!!.adapter = adapter
        users_db!!.addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            if (value == null) {
                Toast.makeText(this@HomeActivity, "An error occurred whilst trying to update users: value is null", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            for (dc in value.documentChanges) {
                val data = dc.document.data as HashMap<String, Any>
                when (dc.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        user_indexes!![dc.document.id] = data["username"] as String?
                        if (dc.document.id == auth!!.uid) {
                            if (data["img_url"] == "") {
                                user_appbar_home!!.imageTintList = ContextCompat.getColorStateList(
                                        applicationContext,
                                        R.color.imageTint
                                )
                                user_appbar_home!!.setImageResource(R.drawable.ic_account_circle)
                            } else {
                                user_appbar_home!!.imageTintList = null
                                Glide.with(applicationContext)
                                        .load(data["img_url"] as String?)
                                        .into(user_appbar_home!!)
                            }
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        user_indexes!!.remove(dc.document.id)
                        if (dc.document.id == auth!!.uid) {
                            user_appbar_home!!.imageTintList = ContextCompat.getColorStateList(
                                    applicationContext,
                                    R.color.imageTint
                            )
                            user_appbar_home!!.setImageResource(R.drawable.ic_account_circle)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }
        musics_db!!.addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            if (value == null) {
                Toast.makeText(this@HomeActivity, "An error occurred whilst trying to update musics: value is null", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            for (dc in value.documentChanges) {
                val data = dc.document.data as HashMap<String, Any>
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        musics_entries!!.add(data)
                        musics_indexes!!.add(dc.document.id)
                    }
                    DocumentChange.Type.MODIFIED -> {
                        musics_entries!![musics_indexes!!.indexOf(dc.document.id)] = data
                        if (currentPlaylist != null) currentPlaylist!![playlistIndex!!.indexOf(dc.document.id)] = data
                    }
                    DocumentChange.Type.REMOVED -> {
                        musics_entries!!.remove(data)
                        musics_indexes!!.remove(dc.document.id)
                        if (currentPlaylist != null) {
                            currentPlaylist!!.remove(data)
                            playlistIndex!!.remove(dc.document.id)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }
        bottomSheetBehavior!!.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && currentPos != -1) {
                    stop()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                imageView2!!.setRotation(if (slideOffset >= 0) slideOffset * 180 else 0)
                compactPlayer!!.alpha = if (slideOffset >= 0) 1 - slideOffset else slideOffset + 1
                compactPlayer!!.visibility = if (slideOffset < 1 && slideOffset > -1) View.VISIBLE else View.INVISIBLE
            }
        })
    }

    private fun initializeVar() {
        setWatermarkColors(binding!!.textWatermark, binding!!.watermarkRoot)
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.sheet_root))
        rv_items_home = binding!!.rvItemsHome
        user_appbar_home = binding!!.userAppbarHome
        compactPlayer = findViewById(R.id.compactPlayer)
        textView4 = findViewById(R.id.textView4)
        textView6 = findViewById(R.id.textView6)
        textView7 = findViewById(R.id.textView7)
        textView8 = findViewById(R.id.textView8)
        textView9 = findViewById(R.id.textView9)
        textView10 = findViewById(R.id.textView10)
        imageView2 = findViewById(R.id.imageView2)
        imageView3 = findViewById(R.id.imageView3)
        imageView4 = findViewById(R.id.imageView4)
        imageView5 = findViewById(R.id.imageView5)
        imageView6 = findViewById(R.id.imageView6)
        imageView7 = findViewById(R.id.imageView7)
        imageView8 = findViewById(R.id.imageView8)
        imageView9 = findViewById(R.id.imageView9)
        progressBar = findViewById(R.id.progressBar)
        progressBar2 = findViewById(R.id.progressBar2)
        progressBar3 = findViewById(R.id.progressBar3)
        seekBar = findViewById(R.id.seekBar)
        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                if (exoPlayer!!.isPlaying) {
                    seekBar.setProgress(exoPlayer!!.currentPosition.toInt() / 100)
                    imageView5.setImageResource(R.drawable.ic_pause)
                    imageView8.setImageResource(R.drawable.ic_pause)
                } else {
                    imageView5.setImageResource(R.drawable.ic_play_arrow)
                    imageView8.setImageResource(R.drawable.ic_play_arrow)
                }
                seekBar.setSecondaryProgress(exoPlayer!!.bufferedPercentage * exoPlayer!!.duration.toInt() / 10000)
                progressBar3.setSecondaryProgress(exoPlayer!!.bufferedPercentage * exoPlayer!!.duration.toInt() / 10000)
                handler!!.postDelayed(this, 100)
            }
        }
        musics_entries = ArrayList()
        musics_indexes = ArrayList()
        user_indexes = HashMap()
        val cache = SimpleCache(getExoPlayerCacheDir(applicationContext),
                exoPlayerCacheEvictor, ExoDatabaseProvider(applicationContext))
        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(
                CacheDataSource.Factory().setCache(cache).setUpstreamDataSourceFactory(
                        DefaultHttpDataSource.Factory().setUserAgent("ZryteZene")))
        exoPlayer = SimpleExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory).build()
        audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC).build()
        exoPlayer!!.setAudioAttributes(audioAttributes!!, true)
        playbackStateListener = PlaybackStateListener()
        exoPlayer!!.addListener(playbackStateListener!!)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        musics_db = FirebaseFirestore.getInstance().collection("musics")
        users_db = FirebaseFirestore.getInstance().collection("users")
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
        textView4.setSelected(true)
        textView7.setSelected(true)
        preferences = getSharedPreferences(getString(R.string.main_preferences_key),
                MODE_PRIVATE)
    }

    private fun play() {
        if (currentPlaylist != null && currentPos > -1) {
            handler!!.removeCallbacks(runnable!!)
            exoPlayer!!.stop()
            isReady = false
            exoPlayer!!.setMediaItem(MediaItem.fromUri((currentPlaylist!![currentPos]["music_url"] as String?)!!))
            exoPlayer!!.prepare()
            textView7!!.text = currentPlaylist!![currentPos]["title"] as String?
            textView8!!.text = if (user_indexes!!.containsKey(currentPlaylist!![currentPos]["author"] as String?)) user_indexes!![currentPlaylist!![currentPos]["author"] as String?] else currentPlaylist!![currentPos]["author"] as String?
            textView4!!.text = currentPlaylist!![currentPos]["title"] as String?
            textView6!!.text = if (user_indexes!!.containsKey(currentPlaylist!![currentPos]["author"] as String?)) user_indexes!![currentPlaylist!![currentPos]["author"] as String?] else currentPlaylist!![currentPos]["author"] as String?
            resetProgressBar()
            textView9!!.text = "--:--"
            textView10!!.text = "--:--"
            if (currentPlaylist!![currentPos]["thumb"] == "") {
                imageView3!!.setImageResource(R.drawable.ic_zrytezene)
                imageView7!!.setImageResource(R.drawable.ic_zrytezene)
            } else {
                Glide.with(applicationContext)
                        .load(currentPlaylist!![currentPos]["thumb"] as String?)
                        .into(imageView3!!)
                Glide.with(applicationContext)
                        .load(currentPlaylist!![currentPos]["thumb"] as String?)
                        .into(imageView7!!)
            }
            seekBar!!.isEnabled = false
            if (bottomSheetBehavior!!.state == BottomSheetBehavior.STATE_HIDDEN) bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            musics_db!!.document(playlistIndex!![currentPos]).update("plays",
                    (currentPlaylist!![currentPos]["plays"] as Number?)!!.toInt() + 1)
        } else {
            Toast.makeText(this,
                    "An error occurred while trying to play a music, playlist is empty",
                    Toast.LENGTH_LONG).show()
        }
    }

    private fun togglePlay() {
        if (exoPlayer!!.isPlaying) exoPlayer!!.pause() else exoPlayer!!.play()
    }

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
            } else {
                stop()
            }
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
        handler!!.removeCallbacks(runnable!!)
        exoPlayer!!.stop()
        isReady = false
        currentPos = -1
        currentPlaylist = null
        playlistIndex = null
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun resetProgressBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            seekBar!!.setProgress(0, true)
        } else {
            seekBar!!.progress = 0
        }
        seekBar!!.max = 100
        seekBar!!.secondaryProgress = 0
        progressBar3!!.max = 100
        progressBar3!!.secondaryProgress = 0
    }

    private inner class PlaybackStateListener : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                ExoPlayer.STATE_READY -> {
                    if (!isReady) {
                        textView9!!.text = "0:00"
                        textView10!!.text = parseDuration(exoPlayer!!.duration)
                        seekBar!!.max = exoPlayer!!.duration.toInt() / 100
                        seekBar!!.isEnabled = true
                        progressBar3!!.max = exoPlayer!!.duration.toInt() / 100
                        isReady = true
                        handler!!.post(runnable!!)
                        exoPlayer!!.play()
                    }
                    progressBar!!.visibility = View.INVISIBLE
                    progressBar2!!.visibility = View.INVISIBLE
                    imageView5!!.visibility = View.VISIBLE
                    imageView8!!.visibility = View.VISIBLE
                }
                ExoPlayer.STATE_BUFFERING -> {
                    imageView5!!.visibility = View.INVISIBLE
                    imageView8!!.visibility = View.INVISIBLE
                    progressBar!!.visibility = View.VISIBLE
                    progressBar2!!.visibility = View.VISIBLE
                }
                ExoPlayer.STATE_ENDED -> playNext()
            }
        }
    }

    private fun refreshRepeatState(mode: Int) {
        imageView9!!.alpha = 1.0f
        when (mode) {
            0 -> {
                imageView9!!.alpha = 0.5f
                imageView9!!.setImageResource(R.drawable.ic_repeat)
            }
            1 -> imageView9!!.setImageResource(R.drawable.ic_repeat)
            2 -> imageView9!!.setImageResource(R.drawable.ic_repeat_one)
            3 -> imageView9!!.setImageResource(R.drawable.ic_shuffle)
        }
    }
}