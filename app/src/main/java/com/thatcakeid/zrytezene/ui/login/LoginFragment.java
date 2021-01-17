package com.thatcakeid.zrytezene.ui.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.thatcakeid.zrytezene.R;

public class LoginFragment extends Fragment {
    private View view;
    private TextInputEditText tie1, tie2;
    private MaterialButton button_continue;
    private FirebaseAuth auth;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.login_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tie1 = view.findViewById(R.id.tie1);
        tie2 = view.findViewById(R.id.tie2);
        button_continue = view.findViewById(R.id.button_continue);

        button_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tie1.getText().toString().trim().length() == 0)
                    Snackbar.make(view, "Email field can't be empty!", Snackbar.LENGTH_LONG);
                else {
                    if (tie2.getText().toString().length() == 0)
                        Snackbar.make(view, "Password field can't be empty!", Snackbar.LENGTH_LONG);
                    else {
                        auth.createUserWithEmailAndPassword(tie1.getText().toString().trim(),
                                tie2.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar.make(view, "A verification link has been sent to your email. Please check your inbox or spam box.", Snackbar.LENGTH_LONG);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                            }
                        });
                    }
                }
            }
        });
    }

}