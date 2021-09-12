package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thatcakeid.zrytezene.databinding.ActivityProfileBinding;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;

    ActivityProfileBinding binding;

    String uid;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DocumentReference user_ref;

    String bio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ExtraMetadata.setWatermarkColors(binding.textWatermark, binding.watermarkRoot);

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        if (!Objects.equals(auth.getUid(), uid)) {
            binding.editBio.setVisibility(View.GONE);
        }

        user_ref = database.collection("users").document(uid);

        user_ref.get()
                .addOnSuccessListener(snapshot -> {
                    binding.userName.setText(snapshot.getString("username"));

                    bio = snapshot.getString("description");
                    binding.userBio.setText(bio);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void editBio(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit bio");

        EditText bio_edit = new EditText(this);
        bio_edit.setText(bio);

        builder.setView(bio_edit);
        builder.setPositiveButton("Ok", (dialog, which) ->
            user_ref.update("description", bio_edit.getText().toString())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Bio edited.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
                })
        );

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.create().show();
    }
}