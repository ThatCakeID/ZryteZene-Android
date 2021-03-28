package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private ArrayList<HashMap<String, Object>> musics_entries, users_entries;
    private HashMap<String, String> user_indexes;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView user_appbar_home = findViewById(R.id.user_appbar_home);
        RecyclerView rv_items_home = findViewById(R.id.rv_items_home);

        musics_entries = new ArrayList<>();
        users_entries = new ArrayList<>();

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
                        users_entries.add(data);
                        user_indexes.put((String) data.get("uid"), (String) data.get("username"));

                        adapter.notifyDataSetChanged();
                        break;

                    case MODIFIED:
                        int mod_pos = users_entries.indexOf(data);

                        users_entries.remove(mod_pos);
                        users_entries.add(mod_pos, data);

                        user_indexes.remove((String) data.get("uid"));
                        user_indexes.put((String) data.get("uid"), (String) data.get("username"));
                        break;

                    case REMOVED:
                        users_entries.remove(data);
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
                        adapter.notifyDataSetChanged();
                        break;

                    case MODIFIED:
                        int mod_pos = musics_entries.indexOf(data);

                        musics_entries.remove(mod_pos);
                        musics_entries.add(mod_pos, data);

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
