package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter;
import com.thatcakeid.zrytezene.databinding.ActivityHomeBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    private View view;
    private ActivityHomeBinding binding;
    private CurrentUserProfile profile;
    private CardView cv_user_appbar;
    private RecyclerView rv_items_home;
    private TextView textView7, textView8, textView9, textView10;
    private ImageView imageView3, imageView4, imageView5, imageView6;
    private ProgressBar progressBar;

    private ArrayList<HashMap<String, Object>> musics_entries;
    private ArrayList<String> musics_indexes;
    private HashMap<String, String> user_indexes;

    private FirebaseAuth auth;
    private FirebaseFirestore musics_db, users_db;
    private BottomSheetBehavior bottomSheetBehavior;

    private SimpleExoPlayer exoPlayer;
    private AudioAttributes audioAttributes;
    private PlaybackStateListener playbackStateListener;
    private ArrayList<HashMap<String, Object>> currentPlaylist;
    private int currentPos;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);

        initializeVar();

        cv_user_appbar.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            } else {
                Intent profile_intent = new Intent(HomeActivity.this, ProfileActivity.class);
                profile_intent.putExtra("uid", profile.uid);
                startActivity(profile_intent);
            }
        });

        imageView4.setOnClickListener(v -> {
            playPrevious();
        });

        imageView5.setOnClickListener(v -> {
        });

        imageView6.setOnClickListener(v -> {
            playNext();
        });

        final HomeItemsRecyclerViewAdapter adapter = new HomeItemsRecyclerViewAdapter(
                getApplicationContext(), musics_entries, user_indexes);
        adapter.setOnItemClickListener(new HomeItemsRecyclerViewAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                currentPlaylist = new ArrayList<>(musics_entries);
                currentPos = position;
                play();
            }

            @Override
            public void onItemLongClick(int position, View v) {

            }
        });

        rv_items_home.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv_items_home.setAdapter(adapter);

        users_db.collection("users").addSnapshotListener((value, error) -> {
            
            if (value == null) {
                Toast.makeText(HomeActivity.this, "An error occurred whilst trying to update users: value is null", Toast.LENGTH_SHORT).show();

                return;
            }
            
            for (DocumentChange dc : value.getDocumentChanges()) {
                HashMap<String, Object> data = (HashMap<String, Object>) dc.getDocument().getData();

                switch (dc.getType()) {
                    case ADDED:

                    case MODIFIED:
                        user_indexes.put((String) data.get("uid"), (String) data.get("username"));
                        adapter.notifyDataSetChanged();
                        break;

                    case REMOVED:
                        user_indexes.remove(data.get("uid"));
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        });

        musics_db.collection("musics").addSnapshotListener((value, error) -> {

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
                        adapter.notifyDataSetChanged();
                        break;

                    case MODIFIED:
                        musics_entries.set(musics_indexes
                                .indexOf(dc.getDocument().getId()), data);
                        adapter.notifyDataSetChanged();
                        break;

                    case REMOVED:
                        musics_entries.remove(data);
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        });
    }

    private void initializeVar() {
        profile = CurrentUserProfile.getInstance();
        ExtraMetadata.setWatermarkColors(binding.textWatermark, binding.watermarkRoot);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.sheet_root));
        cv_user_appbar = binding.cvUserAppbar;
        rv_items_home = binding.rvItemsHome;
        textView7 = findViewById(R.id.textView7);
        textView8 = findViewById(R.id.textView8);
        textView9 = findViewById(R.id.textView9);
        textView10 = findViewById(R.id.textView10);
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);
        imageView5 = findViewById(R.id.imageView5);
        imageView6 = findViewById(R.id.imageView6);
        progressBar = findViewById(R.id.progressBar);

        musics_entries = new ArrayList<>();
        musics_indexes = new ArrayList<>();
        user_indexes = new HashMap<>();
        currentPos = -1;

        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC).build();
        exoPlayer.setAudioAttributes(audioAttributes, true);
        playbackStateListener = new PlaybackStateListener();
        exoPlayer.addListener(playbackStateListener);
        exoPlayer.setPlayWhenReady(true);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        musics_db = FirebaseFirestore.getInstance();
        users_db = FirebaseFirestore.getInstance();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        textView7.setSelected(true);
    }

    private void play() {
        if (currentPlaylist != null && currentPos > -1) {
            exoPlayer.stop();
            exoPlayer.setMediaItem(MediaItem.fromUri(currentPlaylist.get(currentPos)
                    .get("music_url").toString()));
            exoPlayer.prepare();
            textView7.setText(currentPlaylist.get(currentPos).get("title").toString());
            textView8.setText(user_indexes.containsKey(currentPlaylist
                    .get(currentPos).get("author").toString()) ? user_indexes.get(currentPlaylist
                    .get(currentPos).get("author").toString()) : currentPlaylist.get(currentPos)
                    .get("author").toString());
            textView9.setText("--:--");
            textView10.setText("--:--");
            if (currentPlaylist.get(currentPos).get("thumb").toString().equals("")) {
                imageView3.setImageResource(R.drawable.ic_zrytezene);
            } else {
                Glide.with(getApplicationContext())
                        .load(currentPlaylist.get(currentPos).get("thumb").toString()).into(imageView3);
            }
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            Toast.makeText(this,
                    "An error occurred while trying to play a music, playlist is empty",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void playNext() {
        if (currentPos++ < currentPlaylist.size()) {
            currentPos++;
            play();
        } else {
            stop();
        }
    }

    private void playPrevious() {
        if (currentPos-- >= 0) {
            currentPos--;
            play();
        } else {
            stop();
        }
    }

    private void stop() {
        exoPlayer.stop();
        currentPos = -1;
        currentPlaylist = null;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private class PlaybackStateListener implements Player.Listener {
        @Override
        public void onPlaybackStateChanged(int state) {
            switch(state) {
                case ExoPlayer.STATE_READY:
                    textView10.setText(HelperClass.parseDuration(exoPlayer.getDuration()));
                    progressBar.setVisibility(View.INVISIBLE);
                    imageView5.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    imageView5.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_ENDED:
                    playNext();
                    break;
            }
        }
    }
}
