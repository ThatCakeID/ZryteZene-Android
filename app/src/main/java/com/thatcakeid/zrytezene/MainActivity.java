package com.thatcakeid.zrytezene;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        FirebaseFirestore versions_db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set collection reference to 'versions'
        versions_db.collection("versions")
                .get() // Fetch the data to client
                // Set a listener that listen if the data is already received
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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
                                        startActivity = HomeActivity.class;
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
                    }
                })

                // Set a listener that will listen if there are any errors
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show the error to user
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}