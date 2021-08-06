package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter;
import com.thatcakeid.zrytezene.databinding.ActivityHomeBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private View view;
    private ActivityHomeBinding binding;
    private ConstraintLayout compactPlayer;
    private RecyclerView rv_items_home;
    private TextView textView4, textView6, textView7, textView8, textView9, textView10;
    private ShapeableImageView user_appbar_home;
    private ImageView imageView2, imageView3, imageView4, imageView5, imageView6, imageView7,
            imageView8;
    private ProgressBar progressBar, progressBar2, progressBar3;
    private SeekBar seekBar;

    private ArrayList<HashMap<String, Object>> musics_entries, currentPlaylist;
    private ArrayList<String> musics_indexes, playlistIndex;
    private HashMap<String, String> user_indexes;

    private FirebaseAuth auth;
    private CollectionReference musics_db, users_db;
    private BottomSheetBehavior bottomSheetBehavior;

    private SimpleExoPlayer exoPlayer;
    private AudioAttributes audioAttributes;
    private PlaybackStateListener playbackStateListener;
    private Handler handler;
    private Runnable runnable;
    private int currentPos = -1;
    private boolean isReady = false;
    private boolean isDragging = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        initializeVar();

        user_appbar_home.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            } else {
                Intent profile_intent = new Intent(HomeActivity.this, ProfileActivity.class);

                profile_intent.putExtra("uid", auth.getUid());
                startActivity(profile_intent);
            }
        });

        imageView2.setOnClickListener(v -> {
            bottomSheetBehavior.setState(bottomSheetBehavior.getState() ==
                    BottomSheetBehavior.STATE_COLLAPSED
                        ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_COLLAPSED);
        });

        imageView4.setOnClickListener(v -> playPrevious());
        imageView6.setOnClickListener(v -> playNext());

        imageView5.setOnClickListener(v -> togglePlay());
        imageView8.setOnClickListener(v -> togglePlay());

        final HomeItemsRecyclerViewAdapter adapter =
                new HomeItemsRecyclerViewAdapter(
                        getApplicationContext(),
                        musics_entries,
                        user_indexes
                );

        adapter.setOnItemClickListener(new HomeItemsRecyclerViewAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                currentPlaylist = new ArrayList<>(musics_entries);
                playlistIndex = new ArrayList<>(musics_indexes);
                currentPos = position;

                play();
            }

            @Override public void onItemLongClick(int position, View v) { }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView9.setText(HelperClass.parseDuration(progress * 100));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar3.setProgress(progress, true);
                } else {
                    progressBar3.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isDragging = true;
                exoPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isDragging = false;

                exoPlayer.seekTo(seekBar.getProgress() * 100);
                exoPlayer.play();
            }
        });

        rv_items_home.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv_items_home.setAdapter(adapter);

        users_db.addSnapshotListener((value, error) -> {
            if (value == null) {
                Toast.makeText(HomeActivity.this, "An error occurred whilst trying to update users: value is null", Toast.LENGTH_SHORT).show();
                return;
            }

            for (DocumentChange dc : value.getDocumentChanges()) {
                HashMap<String, Object> data = (HashMap<String, Object>) dc.getDocument().getData();

                switch (dc.getType()) {
                    case ADDED:
                    case MODIFIED:
                        user_indexes.put(dc.getDocument().getId(), (String) data.get("username"));

                        if (dc.getDocument().getId().equals(auth.getUid())) {
                            if (dc.getDocument().getId().equals("")) {
                                user_appbar_home.setImageTintList(
                                        ContextCompat.getColorStateList(
                                                getApplicationContext(),
                                                R.color.imageTint
                                        )
                                );

                                user_appbar_home.setImageResource(R.drawable.ic_account_circle);

                            } else {
                                user_appbar_home.setImageTintList(null);
                                Glide.with(getApplicationContext())
                                     .load((String) data.get("img_url"))
                                     .into(user_appbar_home);
                            }
                        }

                        break;

                    case REMOVED:
                        user_indexes.remove(dc.getDocument().getId());

                        if (dc.getDocument().getId().equals(auth.getUid())) {
                            user_appbar_home.setImageTintList(
                                    ContextCompat.getColorStateList(
                                            getApplicationContext(),
                                            R.color.imageTint
                                    )
                            );

                            user_appbar_home.setImageResource(R.drawable.ic_account_circle);
                        }

                        break;
                }

                adapter.notifyDataSetChanged();
            }
        });

        musics_db.addSnapshotListener((value, error) -> {
            if (value == null) {
                Toast.makeText(HomeActivity.this, "An error occurred whilst trying to update musics: value is null", Toast.LENGTH_SHORT).show();
                return;
            }

            for (DocumentChange dc : value.getDocumentChanges()) {
                HashMap<String, Object> data = (HashMap<String, Object>) dc.getDocument().getData();

                switch (dc.getType()) {
                    case ADDED:
                        musics_entries.add(data);
                        musics_indexes.add(dc.getDocument().getId());

                        break;

                    case MODIFIED:
                        musics_entries.set(
                                musics_indexes.indexOf(dc.getDocument().getId()),
                                data
                        );

                        if(currentPlaylist != null)
                            currentPlaylist.set(
                                    playlistIndex.indexOf(dc.getDocument().getId()),
                                    data
                            );

                        break;

                    case REMOVED:
                        musics_entries.remove(data);
                        musics_indexes.remove(dc.getDocument().getId());

                        if (currentPlaylist != null) {
                            currentPlaylist.remove(data);
                            playlistIndex.remove(dc.getDocument().getId());
                        }

                        break;
                }

                adapter.notifyDataSetChanged();
            }
        });

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && currentPos != -1) {
                    stop();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                imageView2.setRotation(slideOffset >= 0 ? slideOffset * 180 : 0);

                compactPlayer.setAlpha(slideOffset >= 0 ? 1 - slideOffset : slideOffset + 1);
                compactPlayer.setVisibility(slideOffset < 1 && slideOffset > -1 ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    private void initializeVar() {
        ExtraMetadata.setWatermarkColors(binding.textWatermark, binding.watermarkRoot);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.sheet_root));

        rv_items_home = binding.rvItemsHome;
        user_appbar_home = binding.userAppbarHome;

        compactPlayer = findViewById(R.id.compactPlayer);

        textView4 = findViewById(R.id.textView4);
        textView6 = findViewById(R.id.textView6);
        textView7 = findViewById(R.id.textView7);
        textView8 = findViewById(R.id.textView8);
        textView9 = findViewById(R.id.textView9);
        textView10 = findViewById(R.id.textView10);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);
        imageView5 = findViewById(R.id.imageView5);
        imageView6 = findViewById(R.id.imageView6);
        imageView7 = findViewById(R.id.imageView7);
        imageView8 = findViewById(R.id.imageView8);
        progressBar = findViewById(R.id.progressBar);
        progressBar2 = findViewById(R.id.progressBar2);
        progressBar3 = findViewById(R.id.progressBar3);
        seekBar = findViewById(R.id.seekBar);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (exoPlayer.isPlaying()) {
                    seekBar.setProgress((int) exoPlayer.getCurrentPosition() / 100);

                    imageView5.setImageResource(R.drawable.ic_pause);
                    imageView8.setImageResource(R.drawable.ic_pause);

                } else {
                    imageView5.setImageResource(R.drawable.ic_play_arrow);
                    imageView8.setImageResource(R.drawable.ic_play_arrow);
                }

                seekBar.setSecondaryProgress(exoPlayer.getBufferedPercentage() * (int)exoPlayer.getDuration() / 10000);
                progressBar3.setSecondaryProgress(exoPlayer.getBufferedPercentage() * (int)exoPlayer.getDuration() / 10000);
                handler.postDelayed(this, 100);
            }
        };

        musics_entries = new ArrayList<>();
        musics_indexes = new ArrayList<>();
        user_indexes = new HashMap<>();

        SimpleCache cache = new SimpleCache(ExtraMetadata.getExoPlayerCacheDir(getApplicationContext()),
                ExtraMetadata.getExoPlayerCacheEvictor(), new ExoDatabaseProvider(getApplicationContext()));
        MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(
                new CacheDataSource.Factory().setCache(cache).setUpstreamDataSourceFactory(
                        new DefaultHttpDataSource.Factory().setUserAgent("ZryteZene")));

        exoPlayer = new SimpleExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory).build();

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC).build();

        exoPlayer.setAudioAttributes(audioAttributes, true);
        playbackStateListener = new PlaybackStateListener();
        exoPlayer.addListener(playbackStateListener);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        musics_db = FirebaseFirestore.getInstance().collection("musics");
        users_db = FirebaseFirestore.getInstance().collection("users");

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        textView4.setSelected(true);
        textView7.setSelected(true);
    }

    private void play() {
        if (currentPlaylist != null && currentPos > -1) {
            handler.removeCallbacks(runnable);
            exoPlayer.stop();

            isReady = false;
            exoPlayer.setMediaItem(MediaItem.fromUri((String) currentPlaylist.get(currentPos).get("music_url")));
            exoPlayer.prepare();

            textView7.setText((String) currentPlaylist.get(currentPos).get("title"));
            textView8.setText(user_indexes.containsKey((String) currentPlaylist
                    .get(currentPos).get("author")) ? user_indexes.get((String) currentPlaylist
                    .get(currentPos).get("author")) : (String) currentPlaylist.get(currentPos)
                    .get("author"));

            textView4.setText((String) currentPlaylist.get(currentPos).get("title"));
            textView6.setText(user_indexes.containsKey((String) currentPlaylist
                    .get(currentPos).get("author")) ? user_indexes.get((String) currentPlaylist
                    .get(currentPos).get("author")) : (String) currentPlaylist.get(currentPos)
                    .get("author"));

            resetProgressBar();

            textView9.setText("--:--");
            textView10.setText("--:--");

            if (currentPlaylist.get(currentPos).get("thumb").equals("")) {
                imageView3.setImageResource(R.drawable.ic_zrytezene);
                imageView7.setImageResource(R.drawable.ic_zrytezene);

            } else {
                Glide.with(getApplicationContext())
                     .load((String) currentPlaylist.get(currentPos).get("thumb"))
                     .into(imageView3);

                Glide.with(getApplicationContext())
                     .load((String) currentPlaylist.get(currentPos).get("thumb"))
                     .into(imageView7);
            }

            seekBar.setEnabled(false);

            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            Toast.makeText(this,
                    "An error occurred while trying to play a music, playlist is empty",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void togglePlay() {
        if (exoPlayer.isPlaying()) exoPlayer.pause();
        else exoPlayer.play();
    }

    private void playNext() {
        if (currentPos + 1 < currentPlaylist.size()) {
            currentPos++;
            play();
        } else {
            stop();
        }
    }

    private void playPrevious() {
        if (currentPos - 1 >= 0) {
            currentPos--;
            play();
        } else {
            stop();
        }
    }

    private void stop() {
        handler.removeCallbacks(runnable);

        exoPlayer.stop();
        isReady = false;

        currentPos = -1;
        currentPlaylist = null;
        playlistIndex = null;

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void resetProgressBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            seekBar.setProgress(0, true);
        } else {
            seekBar.setProgress(0);
        }

        seekBar.setMax(100);
        seekBar.setSecondaryProgress(0);

        progressBar3.setMax(100);
        progressBar3.setSecondaryProgress(0);
    }

    private class PlaybackStateListener implements Player.Listener {
        @Override
        public void onPlaybackStateChanged(int state) {
            switch(state) {
                case ExoPlayer.STATE_READY:
                    if (!isReady) {
                        textView9.setText("0:00");
                        textView10.setText(HelperClass.parseDuration(exoPlayer.getDuration()));

                        seekBar.setMax((int) exoPlayer.getDuration() / 100);
                        seekBar.setEnabled(true);

                        progressBar3.setMax((int) exoPlayer.getDuration() / 100);
                        isReady = true;

                        handler.post(runnable);
                        exoPlayer.play();
                    }

                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar2.setVisibility(View.INVISIBLE);

                    imageView5.setVisibility(View.VISIBLE);
                    imageView8.setVisibility(View.VISIBLE);

                    break;

                case ExoPlayer.STATE_BUFFERING:
                    imageView5.setVisibility(View.INVISIBLE);
                    imageView8.setVisibility(View.INVISIBLE);

                    progressBar.setVisibility(View.VISIBLE);
                    progressBar2.setVisibility(View.VISIBLE);

                    break;

                case ExoPlayer.STATE_ENDED:
                    playNext();
                    break;
            }
        }
    }
}
