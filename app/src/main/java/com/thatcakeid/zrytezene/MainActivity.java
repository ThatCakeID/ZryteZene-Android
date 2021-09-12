package com.thatcakeid.zrytezene;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExtraMetadata.setWatermarkColors(findViewById(R.id.text_watermark), findViewById(R.id.watermark_root));

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        CollectionReference versions_db = FirebaseFirestore.getInstance().collection("versions");
        auth = FirebaseAuth.getInstance();

        versions_db.get() // Fetch the data to client
                // Set a listener that listen if the data is already received
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Convert the result to List<DocumentSnapshot> and get the first item
                    // NOTE: This implementation will be changed soon
                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                    try {
                        // Get the app's package information
                        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                        Class<? extends Activity> startActivity;

                        // Get the client's app version and compare it with the one in the server
                        if ((int) ((long) document.get("version")) > packageInfo.versionCode) {
                            // There's a newer version!
                            startActivity = UpdateActivity.class;
                        } else {
                            if (auth.getCurrentUser() == null) {
                                startActivity = LoginActivity.class;
                            } else {
                                if (auth.getCurrentUser().isEmailVerified()) {
                                    FirebaseFirestore users_db = FirebaseFirestore.getInstance();

                                    users_db.collection("users")
                                            .document(auth.getUid())
                                            .get()
                                            .addOnSuccessListener(snapshot -> {
                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                showBottomSheet();
                                            });

                                    return;
                                } else {
                                    auth.signOut();

                                    Toast.makeText(MainActivity.this, "You've been signed out because your current account's email is not verified.", Toast.LENGTH_LONG).show();
                                    startActivity = LoginActivity.class;
                                }
                            }
                        }

                        startActivity(new Intent(getApplicationContext(), startActivity));
                        finish();

                    } catch (PackageManager.NameNotFoundException ignored) {} // Ignored, this error shouldn't happen
                })

                // Set a listener that will listen if there are any errors
                .addOnFailureListener(e -> {
                    // Show the error to user
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                });
    }

    private void showBottomSheet() {
        final CollectionReference user_ref = FirebaseFirestore.getInstance().collection("users");

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sheet_userdata, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);

        view.findViewById(R.id.button_ok).setOnClickListener(v -> {
            HashMap<String, Object> data = new HashMap<>();

            data.put("description", "");
            data.put("img_url", "");
            data.put("mail", auth.getCurrentUser().getEmail());
            data.put("time_creation", Timestamp.now());
            data.put("username", ((TextInputEditText)view.findViewById(R.id.username_tie)).getText());

            user_ref.add(data).addOnSuccessListener(snapshot -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            });
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();
    }
}