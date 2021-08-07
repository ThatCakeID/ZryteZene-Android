package com.thatcakeid.zrytezene;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;

import com.thatcakeid.zrytezene.databinding.ActivityDebugBinding;

public class DebugActivity extends AppCompatActivity {

    private ActivityDebugBinding binding;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebugBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("App crashed");
        alertDialog.setMessage(getIntent().getStringExtra("error"));
        alertDialog.setPositiveButton("Exit", (dialog, which) -> finish());
        alertDialog.create().show();

    }
}