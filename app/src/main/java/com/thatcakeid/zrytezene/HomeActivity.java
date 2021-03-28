package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.thatcakeid.zrytezene.adapters.HomeItemsRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {
    private ImageView user_appbar_home;
    private RecyclerView rv_items_home;
    private ArrayList<HashMap<String, Object>> musics_entries;
    private ArrayList<HashMap<String, Object>> users_entries;
    private HashMap<String, String> user_indexes;
    private FirebaseFirestore users_db, musics_db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        user_appbar_home = findViewById(R.id.user_appbar_home);
        rv_items_home = findViewById(R.id.rv_items_home);
        musics_entries = new ArrayList<>();
        users_entries = new ArrayList<>();
        user_indexes = new HashMap<>();

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        musics_db = FirebaseFirestore.getInstance();
        users_db = FirebaseFirestore.getInstance();

        user_appbar_home.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            } else {
                // TODO: Implement profile
            }
        });

        rv_items_home.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        HomeItemsRecyclerViewAdapter adapter = new HomeItemsRecyclerViewAdapter(musics_entries,
                user_indexes);
        rv_items_home.setAdapter(adapter);

        users_db.collection("users").addSnapshotListener((EventListener<QuerySnapshot>) (value, error) -> {
            for (DocumentChange dc : value.getDocumentChanges()) {
                switch (dc.getType()) {
                    case ADDED:
                        users_entries.add((HashMap<String, Object>) dc.getDocument().getData());
                        user_indexes.put(((HashMap<String, Object>) dc.getDocument().getData()).get("uid").toString(),
                                ((HashMap<String, Object>) dc.getDocument().getData()).get("username").toString());
                        rv_items_home.getAdapter().notifyDataSetChanged();
                        break;
                    case MODIFIED:
                        int mod_pos = users_entries.indexOf((HashMap<String, Object>) dc.getDocument().getData());
                        users_entries.remove(mod_pos);
                        users_entries.add(mod_pos, (HashMap<String, Object>) dc.getDocument().getData());
                        user_indexes.remove(((HashMap<String, Object>) dc.getDocument().getData()).get("uid").toString());
                        user_indexes.put(((HashMap<String, Object>) dc.getDocument().getData()).get("uid").toString(),
                                ((HashMap<String, Object>) dc.getDocument().getData()).get("username").toString());
                        break;
                    case REMOVED:
                        users_entries.remove((HashMap<String, Object>) dc.getDocument().getData());
                        user_indexes.remove(((HashMap<String, Object>) dc.getDocument().getData()).get("uid").toString());
                        rv_items_home.getAdapter().notifyDataSetChanged();
                        break;
                }
            }
        });

        musics_db.collection("musics").addSnapshotListener((EventListener<QuerySnapshot>) (value, error) -> {
            for (DocumentChange dc : value.getDocumentChanges()) {
                switch (dc.getType()) {
                    case ADDED:
                        musics_entries.add((HashMap<String, Object>) dc.getDocument().getData());
                        rv_items_home.getAdapter().notifyDataSetChanged();
                        break;
                    case MODIFIED:
                        int mod_pos = musics_entries.indexOf((HashMap<String, Object>) dc.getDocument().getData());
                        musics_entries.remove(mod_pos);
                        musics_entries.add(mod_pos, (HashMap<String, Object>) dc.getDocument().getData());
                        rv_items_home.getAdapter().notifyDataSetChanged();
                        break;
                    case REMOVED:
                        musics_entries.remove((HashMap<String, Object>) dc.getDocument().getData());
                        rv_items_home.getAdapter().notifyDataSetChanged();
                        break;
                }
            }
        });
    }
}
