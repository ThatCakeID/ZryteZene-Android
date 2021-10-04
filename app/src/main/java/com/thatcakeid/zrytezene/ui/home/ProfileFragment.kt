package com.thatcakeid.zrytezene.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.databinding.FragmentProfileBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val binding: FragmentProfileBinding by viewBinding(FragmentProfileBinding::bind)

    private var database = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setWatermarkColors(binding.textWatermark, binding.watermarkRoot)

        setSupportActionBar(binding.toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        val uid = intent.getStringExtra("uid")

        val userRef = database.collection("users").document(uid!!)

        userRef.addSnapshotListener { value, _ ->
            if (value == null) {
                Toast.makeText(
                    this@ProfileFragment,
                    "An error occured whilst trying to update user: value is null",
                    Toast.LENGTH_LONG
                ).show()

                return@addSnapshotListener
            }

            binding.userName.text = value.getString("username")
            binding.userBio.text = value.getString("description")

            if (value.getString("img_url") == "") {
                binding.userProfilePicture.imageTintList = ContextCompat.getColorStateList(
                        applicationContext,
                    R.color.imageTint
                )

                binding.userProfilePicture.setImageResource(R.drawable.ic_account_circle)
            } else {
                binding.userProfilePicture.imageTintList = null

                Glide.with(applicationContext)
                        .load(value.getString("img_url"))
                        .into(binding.userProfilePicture)
            }
        }

    }
}