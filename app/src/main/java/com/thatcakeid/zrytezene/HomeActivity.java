package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    private SimpleExoPlayer player;

    private ArrayList<HashMap<String, Object>> musics_entries;
    private ArrayList<String> musics_indexes;
    private HashMap<String, String> user_indexes;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        player = new SimpleExoPlayer.Builder(this).build();

        ImageView user_appbar_home = findViewById(R.id.user_appbar_home);
        RecyclerView rv_items_home = findViewById(R.id.rv_items_home);

        musics_entries = new ArrayList<>();
        musics_indexes = new ArrayList<>();
        user_indexes = new HashMap<>();

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        FirebaseFirestore musics_db = FirebaseFirestore.getInstance();
        FirebaseFirestore users_db = FirebaseFirestore.getInstance();

        user_appbar_home.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            } else {
                // TODO: Implement profile
            }
        });

        final HomeItemsRecyclerViewAdapter adapter = new HomeItemsRecyclerViewAdapter(musics_entries, user_indexes);
        adapter.setOnItemClickListener(new HomeItemsRecyclerViewAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                player.setMediaItem(MediaItem.fromUri(musics_entries.get(position).get("music_url").toString()));
                player.prepare();
                player.play();
            }

            @Override
            public void onItemLongClick(int position, View v) {

            }
        });
        rv_items_home.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv_items_home.setAdapter(adapter);

        users_db.collection("users").addSnapshotListener((EventListener<QuerySnapshot>) (value, error) -> {
            
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
                        user_indexes.remove((String) data.get("uid"));
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        });

        musics_db.collection("musics").addSnapshotListener((EventListener<QuerySnapshot>) (value, error) -> {

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
}
