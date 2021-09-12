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
import com.thatcakeid.zrytezene.SettingsActivity.SettingsFragment
import androidx.preference.PreferenceFragmentCompat
import com.thatcakeid.zrytezene.databinding.ActivityRegisterBinding
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {
    private var email_tie: TextInputEditText? = null
    private var passw_tie: TextInputEditText? = null
    private var passw2_tie: TextInputEditText? = null
    private var email_til: TextInputLayout? = null
    private var passw2_til: TextInputLayout? = null
    private var auth: FirebaseAuth? = null
    var binding: ActivityRegisterBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(
            layoutInflater
        )
        val view: View = binding!!.root
        setContentView(view)
        setWatermarkColors(binding!!.textWatermark, binding!!.watermarkRoot)
        email_tie = binding!!.registerEmailTie
        passw_tie = binding!!.registerPasswTie
        passw2_tie = binding!!.registerPassw2Tie
        email_til = binding!!.registerEmailTil
        val passw_til = binding!!.registerPasswTil
        passw2_til = binding!!.registerPassw2Til
        val button_continue = binding!!.buttonContinue
        val textView5 = binding!!.textView5
        FirebaseApp.initializeApp(applicationContext)
        auth = FirebaseAuth.getInstance()
        email_tie!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (email_tie!!.text.toString().trim { it <= ' ' }
                        .matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                    email_til!!.error = null
                } else {
                    email_til!!.error = "Invalid email!"
                }
            }
        })
        val intent = Intent()
        if (intent.getStringExtra("email") != null) email_tie!!.setText(intent.getStringExtra("email"))
        button_continue.setOnClickListener { v: View? ->
            val email = email_tie!!.text.toString().trim { it <= ' ' }
            val password = passw_tie!!.text.toString().trim { it <= ' ' }
            val password_repeat = passw2_tie!!.text.toString().trim { it <= ' ' }
            if (email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                if (password.length == 0) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Password can't be empty!",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                if (password != password_repeat) {
                    passw2_til!!.error = "Password does not match"
                    return@setOnClickListener
                }
                auth!!.createUserWithEmailAndPassword(
                    email_tie!!.text.toString().trim { it <= ' ' },
                    passw_tie!!.text.toString()
                ).addOnSuccessListener { authResult: AuthResult? ->
                    assert(auth!!.currentUser != null)
                    auth!!.currentUser!!
                        .sendEmailVerification().addOnSuccessListener { aVoid: Void? ->
                            Snackbar.make(
                                view,
                                "A verification link has been sent to your email. Please check your inbox or spam box.",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener { e: Exception ->
                            Snackbar.make(
                                view,
                                e.message!!,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    auth!!.signOut()
                }.addOnFailureListener { e: Exception ->
                    Snackbar.make(
                        view,
                        e.message!!,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(view, "Invalid email!", Snackbar.LENGTH_LONG).show()
            }
        }
        textView5.setOnClickListener { v: View? -> finish() }
    }
}