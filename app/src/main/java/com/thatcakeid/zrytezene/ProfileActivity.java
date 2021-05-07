package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thatcakeid.zrytezene.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;

    String uid;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    DocumentReference user_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        user_ref = database.collection("users").document(uid);

        user_ref.get()
                .addOnSuccessListener(snapshot -> {
                    binding.userName.setText(snapshot.getString("username"));
                    binding.userBio.setText(snapshot.getString("description"));
                });
    }
}