package com.thatcakeid.zrytezene

import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import android.text.TextWatcher
import android.text.Editable
import com.google.android.material.snackbar.Snackbar
import com.thatcakeid.zrytezene.databinding.ActivityRegisterBinding
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setWatermarkColors(binding.textWatermark, binding.watermarkRoot)

        binding.registerEmailTie.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (binding.registerEmailTie.text.toString().trim()
                        .matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
                    binding.registerEmailTie.error = null
                } else {
                    binding.registerEmailTie.error = "Invalid email!"
                }
            }

        })

        if (intent.getStringExtra("email") != null)
            binding.registerEmailTie.setText(intent.getStringExtra("email"))

        binding.buttonContinue.setOnClickListener {

            val email = binding.registerEmailTie.text.toString().trim()
            val password = binding.registerPasswTie.text.toString().trim()
            val passwordRepeat = binding.registerPassw2Tie.text.toString().trim()

            if (email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
                if (password.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "Password can't be empty!",
                        Snackbar.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }

                if (password != passwordRepeat) {
                    Snackbar.make(
                        binding.root,
                        "Password does not match",
                        Snackbar.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }
                auth.createUserWithEmailAndPassword(
                    binding.registerEmailTie.text.toString().trim { it <= ' ' },
                    binding.registerPasswTie.text.toString()
                ).addOnSuccessListener {
                    assert(auth.currentUser != null)
                    auth.currentUser!!
                        .sendEmailVerification().addOnSuccessListener {
                            Snackbar.make(
                                binding.root,
                                "A verification link has been sent to your email. Please check your inbox or spam box.",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener { e: Exception ->
                            Snackbar.make(
                                binding.root,
                                e.message!!,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    auth.signOut()
                }.addOnFailureListener { e: Exception ->
                    Snackbar.make(
                        binding.root,
                        e.message!!,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(binding.root, "Invalid email!", Snackbar.LENGTH_LONG).show()
            }
        }
        binding.textView5.setOnClickListener { finish() }
    }
}