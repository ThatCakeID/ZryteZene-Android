package com.thatcakeid.zrytezene;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore versions_db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        versions_db = FirebaseFirestore.getInstance();

        // Set collection reference to 'versions'
        versions_db.collection("versions")
                .get() // Fetch the data to client
                // Set a listener that listen if the data is already received
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        // Check if the task is executed successfully or not
                        if (task.isSuccessful()) {
                            // Convert the result to List<DocumentSnapshot> and get the first item
                            // NOTE: This implementation will be changed soon
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);

                            try {
                                // Get the app's package information
                                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                                // Get the client's app version and compare it with the one in the server
                                if ((int)((long)document.get("version")) > packageInfo.versionCode) {
                                    // There's a newer version!
                                    Intent intent = new Intent(getApplicationContext(), UpdateActivity.class);
                                    startActivity(intent);
                                } else {
                                    // Go to the home screen
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    startActivity(intent);
                                }

                            } catch (PackageManager.NameNotFoundException ignored) {} // Ignored, this error shouldn't happen

                        } else {
                            // Show warning to user
                            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
                        }
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