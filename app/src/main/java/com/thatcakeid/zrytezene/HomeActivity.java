package com.thatcakeid.zrytezene;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter;
import com.thatcakeid.zrytezene.databinding.ActivityHomeBinding;
import com.thatcakeid.zrytezene.services.PlaybackService;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;
    CurrentUserProfile profile = CurrentUserProfile.getInstance();

    private ArrayList<HashMap<String, Object>> musics_entries;
    private ArrayList<String> musics_indexes;
    private HashMap<String, String> user_indexes;

    private FirebaseAuth auth;

    private PlaybackService playbackService;
    private boolean isServiceBounded = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackService.LocalBinder binder = (PlaybackService.LocalBinder) service;
            playbackService = binder.getService();
            isServiceBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBounded = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ExtraMetadata.setWatermarkColors(binding.textWatermarkHome);

        ImageView user_appbar_home = binding.userAppbarHome;
        RecyclerView rv_items_home = binding.rvItemsHome;

        musics_entries = new ArrayList<>();
        musics_indexes = new ArrayList<>();
        user_indexes = new HashMap<>();

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        FirebaseFirestore musics_db = FirebaseFirestore.getInstance();
        FirebaseFirestore users_db = FirebaseFirestore.getInstance();

        user_appbar_home.setOnClickListener(v -> {
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
                if (!isServiceBounded) {
                    Intent playerIntent = new Intent(HomeActivity.this, PlaybackService.class);
                    playerIntent.putExtra("source", (String) musics_entries.get(position).get("music_url"));
                    startService(playerIntent);
                    bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                } else {
                    //Service is active
                    //Send media with BroadcastReceiver
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBounded) {
            unbindService(serviceConnection);
            playbackService.stopSelf();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("PlaybackServiceState", isServiceBounded);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isServiceBounded = savedInstanceState.getBoolean("PlaybackServiceState");
    }
}
