package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
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
    private CurrentUserProfile profile = CurrentUserProfile.getInstance();
    private CardView cv_user_appbar;
    private RecyclerView rv_items_home;

    private ArrayList<HashMap<String, Object>> musics_entries;
    private ArrayList<String> musics_indexes;
    private HashMap<String, String> user_indexes;
    private SimpleExoPlayer simpleExoPlayer;

    private FirebaseAuth auth;
    FirebaseFirestore musics_db, users_db;
    private BottomSheetBehavior bottomSheetBehavior;

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

        final HomeItemsRecyclerViewAdapter adapter = new HomeItemsRecyclerViewAdapter(musics_entries, user_indexes);
        adapter.setOnItemClickListener(new HomeItemsRecyclerViewAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                MediaItem mediaItem = MediaItem.fromUri((String)musics_entries.get(position).get("music_url"));
                simpleExoPlayer.setMediaItem(mediaItem);
                simpleExoPlayer.prepare();
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

    private class PlayerEventListener implements Player.Listener {

        @Override
        public void onPlaybackStateChanged(int state) {
            switch (state) {
                case Player.STATE_READY:
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    simpleExoPlayer.play();
                    break;

                case Player.STATE_ENDED:
                    break;
            }
        }
    }

    private void initializeVar() {
        ExtraMetadata.setWatermarkColors(binding.textWatermark, binding.watermarkRoot);
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.sheet_root));
        cv_user_appbar = binding.cvUserAppbar;
        rv_items_home = binding.rvItemsHome;

        musics_entries = new ArrayList<>();
        musics_indexes = new ArrayList<>();
        user_indexes = new HashMap<>();
        simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
        simpleExoPlayer.addListener(new PlayerEventListener());

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        musics_db = FirebaseFirestore.getInstance();
        users_db = FirebaseFirestore.getInstance();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}
