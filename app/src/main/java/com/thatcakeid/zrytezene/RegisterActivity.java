package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
    private TextInputEditText tie1, tie2, tie3;
    private TextInputLayout til1, til2, til3;
    private MaterialButton button_continue;
    private FirebaseAuth auth;
    private TextView textView5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tie1 = findViewById(R.id.tie1);
        tie2 = findViewById(R.id.tie2);
        tie3 = findViewById(R.id.tie3);
        til1 = findViewById(R.id.til1);
        til2 = findViewById(R.id.til2);
        til3 = findViewById(R.id.til3);
        button_continue = findViewById(R.id.button_continue);

        textView5 = findViewById(R.id.textView5);

        FirebaseApp.initializeApp(getApplicationContext());
        auth = FirebaseAuth.getInstance();

        tie1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tie1.getText().toString().trim().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                    til1.setError(null);
                } else {
                    til1.setError("Invalid email!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Empty
            }
        });

        Intent intent = new Intent();
        if (intent.getStringExtra("email") != null) tie1.setText(intent.getStringExtra("email"));

        button_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tie1.getText().toString().trim().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                    if (tie2.getText().toString().length() == 0)
                        //Snackbar.make(view, "Password can't be empty!", Snackbar.LENGTH_LONG);
                        Toast.makeText(RegisterActivity.this, "Password can't be empty!", Toast.LENGTH_LONG).show();
                    else {
                        if (!tie3.getText().toString().equals(tie3.getText().toString()))
                            til3.setError("Password does not match");
                        else {
                            auth.createUserWithEmailAndPassword(tie1.getText().toString().trim(),
                                    tie2.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //Snackbar.make(view, "A verification link has been sent to your email. Please check your inbox or spam box.", Snackbar.LENGTH_LONG);
                                            Toast.makeText(RegisterActivity.this, "A verification link has been sent to your email. Please check your inbox or spam box.", Toast.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    auth.signOut();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                } else {
                    //Snackbar.make(view, "Invalid email!", Snackbar.LENGTH_LONG);
                    Toast.makeText(RegisterActivity.this, "Invalid email!", Toast.LENGTH_LONG).show();
                }
            }
        });

        textView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
