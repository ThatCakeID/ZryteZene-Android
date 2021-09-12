package com.thatcakeid.zrytezene

import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import com.thatcakeid.zrytezene.ExtraMetadata
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.google.firebase.FirebaseApp
import android.text.TextWatcher
import android.text.Editable
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import android.content.Intent
import com.thatcakeid.zrytezene.MainActivity
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnFailureListener
import com.thatcakeid.zrytezene.RegisterActivity
import com.thatcakeid.zrytezene.databinding.ActivityLoginBinding
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    private var email_tie: TextInputEditText? = null
    private var passw_tie: TextInputEditText? = null
    private var email_til: TextInputLayout? = null
    private var auth: FirebaseAuth? = null
    var binding: ActivityLoginBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        setWatermarkColors(binding!!.textWatermark, binding!!.watermarkRoot)
        email_tie = binding!!.loginEmailTie
        passw_tie = binding!!.loginPasswTie
        email_til = binding!!.loginEmailTil
        val passw_til = binding!!.loginPasswTil
        val button_continue = binding!!.buttonContinue
        val register_text = binding!!.registerText
        val forgot_password_text = binding!!.forgotPasswordText
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
        button_continue.setOnClickListener { v: View? ->
            val email = email_tie!!.text.toString().trim { it <= ' ' }
            val password = passw_tie!!.text.toString()
            if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                Toast.makeText(this@LoginActivity, "Invalid email!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (password.length == 0) {
                Snackbar.make(view, "Password can't be empty!", Snackbar.LENGTH_LONG).show()
            } else {
                auth!!.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult: AuthResult? ->
                        if (auth!!.currentUser!!
                                .isEmailVerified
                        ) {
                            Toast.makeText(this@LoginActivity, "Logged in", Toast.LENGTH_LONG)
                                .show()
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                            finish()
                        } else {
                            val alertDialog = AlertDialog.Builder(this@LoginActivity)
                            alertDialog.setTitle("Unverified Email")
                            alertDialog.setMessage("Your email isn't verified. Do you want to re-send a new verification link to your email?")
                            alertDialog.setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
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
                            }
                            alertDialog.setNegativeButton("No") { dialog: DialogInterface?, which: Int -> auth!!.signOut() }
                            alertDialog.setCancelable(false)
                            alertDialog.create().show()
                        }
                    }.addOnFailureListener { e: Exception ->
                    Snackbar.make(
                        view,
                        e.message!!,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
        forgot_password_text.setOnClickListener { v: View? ->
            val email = email_tie!!.text.toString().trim { it <= ' ' }
            if (email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                Toast.makeText(this@LoginActivity, "Please enter a valid email!", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            auth!!.sendPasswordResetEmail(email)
            Snackbar.make(
                view,
                "A verification link has been sent to your email. Please check your inbox or spam box.",
                Snackbar.LENGTH_LONG
            ).show()
        }
        register_text.setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    applicationContext, RegisterActivity::class.java
                )
                    .putExtra("email", email_tie!!.text.toString())
            )
        }
    }
}