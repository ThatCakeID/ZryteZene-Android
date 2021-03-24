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
    private HashMap<String, String> user_indexes, comments_count;
    private FirebaseFirestore users_db, musics_db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        user_appbar_home = findViewById(R.id.user_appbar_home);
        rv_items_home = findViewById(R.id.rv_items_home);
        musics_entries = new ArrayList<>();
        user_indexes = new HashMap<>();
        comments_count = new HashMap<>();

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        musics_db = FirebaseFirestore.getInstance();
        users_db = FirebaseFirestore.getInstance();

        user_appbar_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.getCurrentUser() == null) {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                } else {
                    // TODO: Implement profile
                }
            }
        });

        rv_items_home.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        HomeItemsRecyclerViewAdapter adapter = new HomeItemsRecyclerViewAdapter(musics_entries,
                user_indexes, comments_count);
        rv_items_home.setAdapter(adapter);

        musics_db.collection("musics").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange dc : value.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            musics_entries.add((HashMap<String, Object>) dc.getDocument().getData());
                            rv_items_home.getAdapter().notifyDataSetChanged();
                            break;
                        /*case MODIFIED:
                            break;
                        case REMOVED:
                            break;*/
                    }
                }
            }
        });
    }
}
