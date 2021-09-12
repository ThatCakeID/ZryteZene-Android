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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.thatcakeid.zrytezene.SettingsActivity.SettingsFragment
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.firestore.DocumentReference
import com.thatcakeid.zrytezene.databinding.ActivityProfileBinding
import java.lang.Exception

class ProfileActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    var binding: ActivityProfileBinding? = null
    var uid: String? = null
    var database = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()
    var user_ref: DocumentReference? = null
    var bio: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)
        setWatermarkColors(binding!!.textWatermark, binding!!.watermarkRoot)
        toolbar = binding!!.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar!!.setNavigationOnClickListener { v: View? -> onBackPressed() }
        val intent = intent
        uid = intent.getStringExtra("uid")
        if (auth.uid != uid) {
            binding!!.editBio.visibility = View.GONE
        }
        user_ref = database.collection("users").document(uid!!)
        user_ref!!.get()
            .addOnSuccessListener { snapshot: DocumentSnapshot ->
                binding!!.userName.text = snapshot.getString("username")
                bio = snapshot.getString("description")
                binding!!.userBio.text = bio
            }
            .addOnFailureListener { e: Exception ->
                e.printStackTrace()
                Toast.makeText(this, "An error occurred: " + e.message, Toast.LENGTH_LONG).show()
            }
    }

    fun editBio(view: View?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit bio")
        val bio_edit = EditText(this)
        bio_edit.setText(bio)
        builder.setView(bio_edit)
        builder.setPositiveButton(
            "Ok"
        ) { dialog: DialogInterface?, which: Int ->
            user_ref!!.update("description", bio_edit.text.toString())
                .addOnSuccessListener { aVoid: Void? ->
                    Toast.makeText(
                        this,
                        "Bio edited.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e: Exception ->
                    e.printStackTrace()
                    Toast.makeText(this, "An error occurred: " + e.message, Toast.LENGTH_LONG)
                        .show()
                }
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        builder.create().show()
    }
}