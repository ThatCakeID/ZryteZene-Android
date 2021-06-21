package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import com.thatcakeid.zrytezene.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText email_tie, passw_tie;
    private TextInputLayout email_til;
    private FirebaseAuth auth;

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ExtraMetadata.setWatermarkColors(binding.textWatermarkLogin);

        email_tie = binding.loginEmailTie;
        passw_tie = binding.loginPasswTie;
        email_til = binding.loginEmailTil;

        TextInputLayout passw_til = binding.loginPasswTil;

        MaterialButton button_continue = binding.buttonContinue;

        TextView register_text = binding.registerText;
        TextView forgot_password_text = binding.forgotPasswordText;

        FirebaseApp.initializeApp(getApplicationContext());
        auth = FirebaseAuth.getInstance();

        email_tie.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (email_tie.getText().toString().trim().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                    email_til.setError(null);
                } else {
                    email_til.setError("Invalid email!");
                }
            }
        });

        button_continue.setOnClickListener(v -> {
            String email = email_tie.getText().toString().trim();
            String password = passw_tie.getText().toString();

            if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                Toast.makeText(LoginActivity.this, "Invalid email!", Toast.LENGTH_LONG).show();
                return;
            }

            if (password.length() == 0) {
                //Snackbar.make(view, "Password can't be empty!", Snackbar.LENGTH_LONG);
                Toast.makeText(LoginActivity.this, "Password can't be empty!", Toast.LENGTH_LONG).show();

            } else {
                auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                    if (auth.getCurrentUser().isEmailVerified()) {
                        Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        finish();

                    } else {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);

                        alertDialog.setTitle("Unverified Email");
                        alertDialog.setMessage("Your email isn't verified. Do you want to re-send a new verification link to your email?");

                        alertDialog.setPositiveButton("Yes", (dialog, which) -> auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(aVoid -> {
                            //Snackbar.make(view, "A verification link has been sent to your email. Please check your inbox or spam box.", Snackbar.LENGTH_LONG);
                            Toast.makeText(LoginActivity.this, "A verification link has been sent to your email. Please check your inbox or spam box.", Toast.LENGTH_LONG).show();
                        }).addOnFailureListener(e -> {
                            //Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }));

                        alertDialog.setNegativeButton("No", (dialog, which) -> auth.signOut());
                        alertDialog.setCancelable(false);
                        alertDialog.create().show();
                    }

                }).addOnFailureListener(e -> {
                    //Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });

        forgot_password_text.setOnClickListener(v -> {
            String email = email_tie.getText().toString().trim();

            if (email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                Toast.makeText(LoginActivity.this, "Please enter a valid email!", Toast.LENGTH_LONG).show();
                return;
            }

            auth.sendPasswordResetEmail(email);
            //Snackbar.make(view, "A verification link has been sent to your email. Please check your inbox or spam box.", Snackbar.LENGTH_LONG);
            Toast.makeText(LoginActivity.this, "A verification link has been sent to your email. Please check your inbox or spam box.", Toast.LENGTH_LONG).show();
        });

        register_text.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
            i.putExtra("email", email_tie.getText().toString());
            startActivity(i);
        });
    }
}