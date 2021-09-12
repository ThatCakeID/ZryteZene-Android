package com.thatcakeid.zrytezene

import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import com.canhub.cropper.CropImageView.CropResult.isSuccessful
import com.canhub.cropper.CropImageView.CropResult.uriContent
import com.canhub.cropper.CropImageContractOptions.setGuidelines
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.canhub.cropper.CropImageContractOptions
import android.os.Bundle
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.ExtraMetadata
import android.widget.TextView
import android.widget.LinearLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.canhub.cropper.CropImageContract
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentSnapshot
import android.content.pm.PackageInfo
import android.app.Activity
import com.thatcakeid.zrytezene.UpdateActivity
import com.thatcakeid.zrytezene.LoginActivity
import android.content.Intent
import com.thatcakeid.zrytezene.HomeActivity
import com.google.android.gms.tasks.OnFailureListener
import android.widget.Toast
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.OnCompleteListener
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView.Guidelines
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import java.lang.Exception
import java.util.HashMap
import java.util.concurrent.atomic.AtomicReference

class MainActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null
    private var users_db: CollectionReference? = null
    private val imageUri = AtomicReference<Uri?>()
    private var cropImage: ActivityResultLauncher<CropImageContractOptions>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setWatermarkColors(findViewById(R.id.text_watermark), findViewById(R.id.watermark_root))

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        val versions_db = FirebaseFirestore.getInstance().collection("versions")
        auth = FirebaseAuth.getInstance()
        cropImage = registerForActivityResult(
            CropImageContract(),
            ActivityResultCallback { result: CropResult ->
                if (result.isSuccessful) {
                    imageUri.set(result.uriContent)
                }
            })
        versions_db.get() // Fetch the data to client
            // Set a listener that listen if the data is already received
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                // Convert the result to List<DocumentSnapshot> and get the first item
                // NOTE: This implementation will be changed soon
                val document = queryDocumentSnapshots.documents[0]
                try {
                    // Get the app's package information
                    val packageInfo = packageManager.getPackageInfo(packageName, 0)
                    val startActivity: Class<out Activity?>

                    // Get the client's app version and compare it with the one in the server
                    if ((document["version"] as Long).toInt() > packageInfo.versionCode) {
                        // There's a newer version!
                        startActivity = UpdateActivity::class.java
                    } else {
                        if (auth!!.currentUser == null) {
                            startActivity = LoginActivity::class.java
                        } else {
                            if (auth!!.currentUser!!.isEmailVerified) {
                                users_db = FirebaseFirestore.getInstance().collection("users")
                                users_db!!.document(auth!!.uid!!)
                                    .get()
                                    .addOnSuccessListener { snapshot: DocumentSnapshot ->
                                        if (snapshot.exists()) {
                                            startActivity(
                                                Intent(
                                                    applicationContext,
                                                    HomeActivity::class.java
                                                )
                                            )
                                            finish()
                                        } else {
                                            showBottomSheet()
                                        }
                                    }
                                    .addOnFailureListener { e: Exception ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "An error occured: " + e.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                return@addOnSuccessListener
                            } else {
                                auth!!.signOut()
                                Toast.makeText(
                                    this@MainActivity,
                                    "You've been signed out because your current account's email is not verified.",
                                    Toast.LENGTH_LONG
                                ).show()
                                startActivity = LoginActivity::class.java
                            }
                        }
                    }
                    startActivity(Intent(applicationContext, startActivity))
                    finish()
                } catch (ignored: PackageManager.NameNotFoundException) {
                } // Ignored, this error shouldn't happen
            } // Set a listener that will listen if there are any errors
            .addOnFailureListener { e: Exception ->
                // Show the error to user
                Toast.makeText(
                    this@MainActivity,
                    "An error occured: " + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showBottomSheet() {
        val view = layoutInflater.inflate(R.layout.sheet_userdata, null, false)
        val bottomSheetDialog = BottomSheetDialog(this@MainActivity)
        view.findViewById<View>(R.id.button_ok).setOnClickListener { v: View? ->
            val data: MutableMap<String, Any?> = HashMap()
            data["description"] = ""
            data["img_url"] = ""
            data["mail"] = auth!!.currentUser!!.email
            data["time_creation"] = Timestamp.now()
            data["username"] =
                (view.findViewById<View>(R.id.username_tie) as TextInputEditText).text.toString()
            if (imageUri.get() != null) {
                val user_pfp = FirebaseStorage.getInstance()
                    .reference.child("users/images").child(auth!!.uid!!).child("profile-img")
                val uploadTask = user_pfp.putFile(imageUri.get()!!)
                val urlTask: Task<Uri?> = uploadTask.continueWithTask(
                    Continuation<UploadTask.TaskSnapshot?, Task<Any>> { task: Task<UploadTask.TaskSnapshot?>? -> })
                    .addOnCompleteListener { task: Task<Any?>? -> }
                users_db!!.document(auth!!.uid!!).set(data)
                    .addOnSuccessListener { snapshot: Void? ->
                        bottomSheetDialog.dismiss()
                        startActivity(Intent(applicationContext, HomeActivity::class.java))
                        finish()
                    }.addOnFailureListener { e: Exception ->
                    Toast.makeText(
                        this@MainActivity,
                        "An error occured: " + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                users_db!!.document(auth!!.uid!!).set(data)
                    .addOnSuccessListener { snapshot: Void? ->
                        bottomSheetDialog.dismiss()
                        startActivity(Intent(applicationContext, HomeActivity::class.java))
                        finish()
                    }.addOnFailureListener { e: Exception ->
                    Toast.makeText(
                        this@MainActivity,
                        "An error occured: " + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        view.findViewById<View>(R.id.user_image).setOnClickListener { v: View? ->
            cropImage!!.launch(
                CropImageContractOptions(
                    null,
                    CropImageOptions()
                ).setGuidelines(Guidelines.ON)
            )
        }
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.show()
    }
}