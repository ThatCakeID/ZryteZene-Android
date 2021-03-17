package com.thatcakeid.zrytezene;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText tie1, tie2, tie3, tie4;
    private TextInputLayout til1, til2, til3, til4;
    private MaterialButton button_continue;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tie1 = findViewById(R.id.tie1);
        tie2 = findViewById(R.id.tie2);
        tie3 = findViewById(R.id.tie3);
        tie4 = findViewById(R.id.tie4);
        til1 = findViewById(R.id.til1);
        til2 = findViewById(R.id.til2);
        til3 = findViewById(R.id.til3);
        til4 = findViewById(R.id.til4);
        button_continue = findViewById(R.id.button_continue);

        FirebaseApp.initializeApp(getApplicationContext());
        auth = FirebaseAuth.getInstance();

        button_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tie1.getText().toString().trim().length() == 0)
                    til1.setError("Invalid email");
                else {
                    if (tie2.getText().toString().trim().length() < 4)
                        til2.setError("Username field can't be less than 4 characters");
                    else {
                        if (tie3.getText().toString().length() == 0)
                            til3.setError("Password can't be empty");
                        else {
                            if (!tie3.getText().toString().equals(tie4.getText().toString()))
                                til4.setError("Password does not match");
                            else {
                                auth.createUserWithEmailAndPassword(tie1.getText().toString().trim(),
                                        tie3.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
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
                    }
                }
            }
        });
    }
}
