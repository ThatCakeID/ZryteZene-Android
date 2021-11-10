package com.thatcakeid.zrytezene.ui.startup

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView.Guidelines
import com.canhub.cropper.options
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.databinding.FragmentStartupBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class StartupFragment : Fragment(R.layout.fragment_startup) {
    private val binding: FragmentStartupBinding by viewBinding(FragmentStartupBinding::bind)

    private val auth = FirebaseAuth.getInstance()
    private val imageUri = AtomicReference<Uri?>()

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri.set(result.uriContent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.splashSignupButton.setOnClickListener {
            findNavController()
                .navigate(
                    R.id.action_splashFragment_to_registerFragment,
                    null,
                    null,
                    FragmentNavigatorExtras(binding.zzImage to "icon")
                )
        }

        binding.splashSigninButton.setOnClickListener {
            findNavController()
                .navigate(
                    R.id.action_splashFragment_to_loginFragment,
                    null,
                    null,
                    FragmentNavigatorExtras(binding.zzImage to "icon")
                )
        }

        // fetch the android version on the database
        FirebaseFirestore
            .getInstance()
            .document("versions/android")
            .get()
            .addOnSuccessListener { document ->
                // first we compare the current version and the version in the database
                // if there's a new version then update
                val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
                val appVersionCode =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode
                    else packageInfo.versionCode.toLong()

                if (document.getLong("version")!! > appVersionCode) {
                    // go update, there's a new version
                    findNavController()
                        .navigate(R.id.action_splashFragment_to_updateFragment)

                    return@addOnSuccessListener
                }

                // check if the user is logged in, then perform a check for missing user info.
                // then ask the user to fill out those if they are missing
                // ==
                // also check if their email is verified or not, if they don't, then sign out
                // and go to the login page (where they can verify their email)
                val user = auth.currentUser
                if (user != null) {
                    if (!user.isEmailVerified) {
                        auth.signOut()

                        Toast.makeText(
                            requireContext(),
                            "You've been signed out because your current account's email is not verified.",
                            Toast.LENGTH_LONG
                        ).show()

                        findNavController()
                            .navigate(R.id.action_splashFragment_to_home_nav)

                        return@addOnSuccessListener
                    }

                    FirebaseFirestore
                        .getInstance()
                        .document("users/${user.uid}")
                        .get()
                        .addOnSuccessListener { userInfo ->
                            if (!userInfo.exists()) {
                                // sounds like this person has some fields empty (also hide the progbar)
                                binding.splashProgressBar.visibility = View.GONE
                                showBottomSheet()
                            } else {
                                // user's logged in, go to homepage
                                findNavController()
                                    .navigate(R.id.action_splashFragment_to_home_nav)
                            }
                        }
                        .addOnFailureListener { err ->
                            Toast.makeText(
                                requireContext(),
                                "Couldn't fetch your user info: $err",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    return@addOnSuccessListener
                }
                
                // the user is not logged in, thus show the sign in & register button
                binding.splashSigninButton.visibility = View.VISIBLE
                binding.splashSignupButton.visibility = View.VISIBLE

                // hide the progress bar
                binding.splashProgressBar.visibility = View.GONE
            }
            .addOnFailureListener { err ->
                Toast.makeText(
                    requireContext(),
                    "Couldn't connect to the server: $err",
                    Toast.LENGTH_LONG
                ).show()
            }

        // hide the progressbar just in case
        binding.splashProgressBar.visibility = View.GONE
    }

    private fun showBottomSheet() {
        val view = layoutInflater.inflate(R.layout.sheet_userdata, null, false)
        (view.parent as View).setBackgroundColor(0x00000000)
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        view.findViewById<Button>(R.id.button_ok).setOnClickListener {
            val data = HashMap<String, Any>()

            data["description"] = ""
            data["img_url"] = ""
            data["mail"] = auth.currentUser!!.email!!
            data["time_creation"] = Timestamp.now()
            data["username"] =
                view.findViewById<TextView>(R.id.username_tie).text.toString()

            if (imageUri.get() != null) {
                val userPfp = FirebaseStorage.getInstance()
                    .reference.child("users/images").child(auth.uid!!).child("profile-img")

                val uploadTask = userPfp.putFile(imageUri.get()!!)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    userPfp.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        data["img_url"] = task.result.toString()

                        FirebaseFirestore
                            .getInstance()
                            .document("users/${auth.uid!!}")
                            .set(data)
                            .addOnSuccessListener {
                                bottomSheetDialog.dismiss()
                                findNavController()
                                    .navigate(R.id.action_splashFragment_to_home_nav)

                            }.addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "An error occured: " + e.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        Snackbar.make(
                            binding.root,
                            "An error occured: " + task.exception!!.message,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                FirebaseFirestore
                    .getInstance()
                    .document("users/${auth.uid!!}")
                    .set(data)
                    .addOnSuccessListener {
                        bottomSheetDialog.dismiss()
                        findNavController()
                            .navigate(R.id.action_splashFragment_to_home_nav)
                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "An error occured: " + e.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

        view.findViewById<View>(R.id.user_image).setOnClickListener {
            cropImage.launch(
                options {
                    setGuidelines(Guidelines.ON)
                }
            )
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.show()
    }
}