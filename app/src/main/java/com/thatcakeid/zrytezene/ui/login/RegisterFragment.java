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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.thatcakeid.zrytezene.R;

public class RegisterFragment extends Fragment {
    private View view;
    private TextInputEditText tie1, tie2, tie3, tie4;
    private TextInputLayout til1, til2, til3, til4;
    private MaterialButton button_continue;
    private FirebaseAuth auth;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.register_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tie1 = view.findViewById(R.id.tie1);
        tie2 = view.findViewById(R.id.tie2);
        tie3 = view.findViewById(R.id.tie3);
        tie4 = view.findViewById(R.id.tie4);
        til1 = view.findViewById(R.id.til1);
        til2 = view.findViewById(R.id.til2);
        til3 = view.findViewById(R.id.til3);
        til4 = view.findViewById(R.id.til4);
        button_continue = view.findViewById(R.id.button_continue);

        FirebaseApp.initializeApp(getActivity());
        auth = FirebaseAuth.getInstance();

        button_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tie1.getText().toString().trim().length() == 0)
                    til1.setError("Email field can't be empty!");
                else {
                    if (tie2.getText().toString().trim().length() < 4)
                        til2.setError("Username field can't be empty or less than 4 characters!");
                    else {
                        if (tie3.getText().toString().length() == 0)
                            til3.setError("Password field can't be empty!");
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
                                                Snackbar.make(view, "A verification link has been sent to your email. Please check your inbox or spam box.", Snackbar.LENGTH_LONG);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
                                            }
                                        });
                                        auth.signOut();
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
                }
            }
        });
    }

}
