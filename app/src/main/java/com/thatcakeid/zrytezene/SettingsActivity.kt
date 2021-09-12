package com.thatcakeid.zrytezene

import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import com.thatcakeid.zrytezene.ExtraMetadata
import android.content.Intent
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.android.gms.tasks.OnFailureListener
import android.widget.Toast
import android.widget.EditText
import android.content.DialogInterface
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.google.firebase.FirebaseApp
import android.text.TextWatcher
import android.text.Editable
import android.view.View
import com.google.firebase.auth.AuthResult
import com.google.android.material.snackbar.Snackbar
import com.thatcakeid.zrytezene.R
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import com.thatcakeid.zrytezene.SettingsActivity.SettingsFragment
import androidx.preference.PreferenceFragmentCompat
import com.thatcakeid.zrytezene.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private var binding: ActivitySettingsBinding? = null
    private var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(
            layoutInflater
        )
        val view: View = binding!!.root
        setContentView(view)
        setWatermarkColors(findViewById(R.id.text_watermark), findViewById(R.id.watermark_root))
        toolbar = binding!!.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar!!.setNavigationOnClickListener { v: View? -> onBackPressed() }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}