package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText email_tie, passw_tie, passw2_tie;
    private TextInputLayout email_til, passw_til, passw2_til;
    private MaterialButton button_continue;
    private FirebaseAuth auth;
    private TextView textView5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email_tie = findViewById(R.id.register_email_tie);
        passw_tie = findViewById(R.id.register_passw_tie);
        passw2_tie = findViewById(R.id.register_passw2_tie);

        email_til = findViewById(R.id.register_email_til);
        passw_til = findViewById(R.id.register_passw_til);
        passw2_til = findViewById(R.id.register_passw2_til);

        button_continue = findViewById(R.id.button_continue);

        textView5 = findViewById(R.id.textView5);

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

        Intent intent = new Intent();
        if (intent.getStringExtra("email") != null) email_tie.setText(intent.getStringExtra("email"));

        button_continue.setOnClickListener(v -> {
            String email = email_tie.getText().toString().trim();
            String password = passw_tie.getText().toString().trim();
            String password_repeat = passw2_tie.getText().toString().trim();

            if (email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                if (password.length() == 0) {
                    Toast.makeText(RegisterActivity.this, "Password can't be empty!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!password.equals(password_repeat)) {
                    passw2_til.setError("Password does not match");
                    return;
                }

                auth.createUserWithEmailAndPassword(email_tie.getText().toString().trim(),
                    passw_tie.getText().toString()).addOnSuccessListener(authResult -> {

                        auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(aVoid -> {
                            //Snackbar.make(view, "A verification link has been sent to your email. Please check your inbox or spam box.", Snackbar.LENGTH_LONG);
                            Toast.makeText(RegisterActivity.this, "A verification link has been sent to your email. Please check your inbox or spam box.", Toast.LENGTH_LONG).show();

                        }).addOnFailureListener(e -> {
                            //Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        });

                        auth.signOut();

                    }).addOnFailureListener(e -> {
                        //Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
            } else {
                //Snackbar.make(view, "Invalid email!", Snackbar.LENGTH_LONG);
                Toast.makeText(RegisterActivity.this, "Invalid email!", Toast.LENGTH_LONG).show();
            }
        });

        textView5.setOnClickListener(v -> finish());
    }
}
