package com.thatcakeid.zrytezene;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import com.thatcakeid.zrytezene.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ExtraMetadata.setWatermarkColors(findViewById(R.id.text_watermark), findViewById(R.id.watermark_root));

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsActivity.SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}
