package com.thatcakeid.zrytezene.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.databinding.FragmentProfileBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val binding: FragmentProfileBinding by viewBinding(FragmentProfileBinding::bind)

    private var database = FirebaseFirestore.getInstance()

    private val args: ProfileFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setSupportActionBar(binding.toolbar)
//
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        supportActionBar!!.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        val uid = args.uid

        val userRef = database.collection("users").document(uid)

        userRef.addSnapshotListener { value, _ ->
            if (value == null) {
                Toast.makeText(
                    requireContext(),
                    "An error occured whilst trying to update user: value is null",
                    Toast.LENGTH_LONG
                ).show()

                return@addSnapshotListener
            }

            binding.userName.text = value.getString("username")
            binding.userBio.text = value.getString("description")

            if (value.getString("img_url") == "") {
                binding.userProfilePicture.imageTintList = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.imageTint
                )

                binding.userProfilePicture.setImageResource(R.drawable.ic_account_circle)
            } else {
                binding.userProfilePicture.imageTintList = null

                Glide.with(this)
                        .load(value.getString("img_url"))
                        .into(binding.userProfilePicture)
            }
        }
    }
}