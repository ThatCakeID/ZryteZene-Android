package com.thatcakeid.zrytezene;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private CollectionReference users_db;
    private AtomicReference<Uri> imageUri = new AtomicReference<>();
    private ActivityResultLauncher<CropImageContractOptions> cropImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExtraMetadata.setWatermarkColors(findViewById(R.id.text_watermark), findViewById(R.id.watermark_root));

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        CollectionReference versions_db = FirebaseFirestore.getInstance().collection("versions");
        auth = FirebaseAuth.getInstance();

        cropImage = registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                imageUri.set(result.getUriContent());
            }
        });

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
                                    users_db = FirebaseFirestore.getInstance().collection("users");

                                    users_db.document(auth.getUid())
                                            .get()
                                            .addOnSuccessListener(snapshot -> {
                                                if (snapshot.exists()) {
                                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                                    finish();
                                                } else {
                                                    showBottomSheet();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(MainActivity.this, "An error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, "An error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showBottomSheet() {
        View view = getLayoutInflater().inflate(R.layout.sheet_userdata, null, false);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);

        view.findViewById(R.id.button_ok).setOnClickListener(v -> {
            Map<String, Object> data = new HashMap<>();

            data.put("description", "");
            data.put("img_url", "");
            data.put("mail", auth.getCurrentUser().getEmail());
            data.put("time_creation", Timestamp.now());
            data.put("username", ((TextInputEditText) view.findViewById(R.id.username_tie)).getText().toString());

            if (imageUri.get() != null) {
                StorageReference user_pfp = FirebaseStorage.getInstance()
                        .getReference().child("users/images").child(auth.getUid()).child("profile-img");
                UploadTask uploadTask = user_pfp.putFile(imageUri.get());

                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {

                }).addOnCompleteListener(task -> {

                });

                users_db.document(auth.getUid()).set(data).addOnSuccessListener(snapshot -> {
                    bottomSheetDialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "An error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

            } else {

                users_db.document(auth.getUid()).set(data).addOnSuccessListener(snapshot -> {
                    bottomSheetDialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "An error occured: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

            }
        });

        view.findViewById(R.id.user_image).setOnClickListener(v -> {
            cropImage.launch(new CropImageContractOptions(
                    null,
                    new CropImageOptions()).setGuidelines(CropImageView.Guidelines.ON)
            );
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();
    }
}