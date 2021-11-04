package com.thatcakeid.zrytezene.ui.startup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.databinding.FragmentRegisterBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

// TODO: 10/5/21 move stuff to a viewmodel
class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val binding: FragmentRegisterBinding by viewBinding(FragmentRegisterBinding::bind)
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.registerEmailTie.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (binding.registerEmailTie.text.toString().trim()
                        .matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
                    binding.registerEmailTil.error = null
                } else {
                    binding.registerEmailTil.error = "Invalid email!"
                }
            }

        })

        requireArguments().getString("email")?.let {
            binding.registerEmailTie.setText(it)
        }

        binding.buttonContinue.setOnClickListener {

            val email = binding.registerEmailTie.text.toString().trim()
            val password = binding.registerPasswTie.text.toString().trim()
            val passwordRepeat = binding.registerPassw2Tie.text.toString().trim()

            if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
                Snackbar.make(binding.root, "Invalid email!", Snackbar.LENGTH_LONG).show()

                return@setOnClickListener
            }

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
                binding.registerEmailTie.text.toString().trim(),
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
                    .addOnFailureListener { e ->
                        Snackbar.make(
                            binding.root,
                            e.message!!,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                auth.signOut()
            }.addOnFailureListener { e ->
                Snackbar.make(
                    binding.root,
                    e.message!!,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        binding.textView5.setOnClickListener {
            findNavController()
                .popBackStack()
        }
    }
}