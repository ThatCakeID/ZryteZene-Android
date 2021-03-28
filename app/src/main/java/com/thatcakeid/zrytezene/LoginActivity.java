package com.thatcakeid.zrytezene;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText email_tie, passw_tie;
    private TextInputLayout email_til, passw_til;
    private MaterialButton button_continue;
    private FirebaseAuth auth;
    private TextView register_text, forgot_password_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email_tie = findViewById(R.id.login_email_tie);
        passw_tie = findViewById(R.id.login_passw_tie);
        email_til = findViewById(R.id.login_email_til);
        passw_til = findViewById(R.id.login_passw_til);

        button_continue = findViewById(R.id.button_continue);

        register_text = findViewById(R.id.register_text);
        forgot_password_text = findViewById(R.id.forgot_password_text);

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