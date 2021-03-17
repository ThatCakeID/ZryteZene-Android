package com.thatcakeid.zrytezene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText tie1, tie2;
    private TextInputLayout til1, til2;
    private MaterialButton button_continue;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tie1 = findViewById(R.id.tie1);
        tie2 = findViewById(R.id.tie2);
        til1 = findViewById(R.id.til1);
        til2 = findViewById(R.id.til2);
        button_continue = findViewById(R.id.button_continue);

        FirebaseApp.initializeApp(getApplicationContext());
        auth = FirebaseAuth.getInstance();

        button_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tie1.getText().toString().trim().length() == 0)
                    til1.setError("Invalid email");
                else {
                    if (tie2.getText().toString().length() == 0)
                        til2.setError("Password can't be empty");
                    else {
                        auth.signInWithEmailAndPassword(tie1.getText().toString().trim(),
                                tie2.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                if (auth.getCurrentUser().isEmailVerified()) {
                                    Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                    finish();
                                } else {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                                    alertDialog.setTitle("Unverified Email");
                                    alertDialog.setMessage("Your email isn't verified. Do you want to re-send a new verification link to your email?");
                                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //Snackbar.make(view, "A verification link has been sent to your email. Please check your inbox or spam box.", Snackbar.LENGTH_LONG);
                                                    Toast.makeText(LoginActivity.this, "A verification link has been sent to your email. Please check your inbox or spam box.", Toast.LENGTH_LONG).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    //Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                                                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    });
                                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            auth.signOut();
                                        }
                                    });
                                    alertDialog.setCancelable(false);
                                    alertDialog.create().show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }
}