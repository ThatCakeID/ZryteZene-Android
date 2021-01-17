package com.thatcakeid.zrytezene;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView rv_items_home;
    private ArrayList<Map<String, Object>> musics_entries;
    private Map<String, String> user_indexes, comments_count;
    private FirebaseFirestore users_db, musics_db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        rv_items_home = findViewById(R.id.rv_items_home);
        musics_entries = new ArrayList<>();
        user_indexes = new HashMap<>();
        comments_count = new HashMap<>();

        rv_items_home.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        HomeItemsRecyclerViewAdapter adapter = new HomeItemsRecyclerViewAdapter(musics_entries,
                user_indexes, comments_count);
        rv_items_home.setAdapter(adapter);
    }
}
