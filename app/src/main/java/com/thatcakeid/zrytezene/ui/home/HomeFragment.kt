package com.thatcakeid.zrytezene.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.databinding.FragmentHomeBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding: FragmentHomeBinding by viewBinding(FragmentHomeBinding::bind)
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var database = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userAppbarHome.setOnClickListener {
            if (auth.currentUser == null) {
                findNavController()
                    .navigate(R.id.action_homeFragment_to_loginFragment)
            } else {
                findNavController()
                    .navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment(auth.uid!!))
            }
        }

        val userRef = database.collection("users").document(auth.uid!!)

        userRef.addSnapshotListener { value, _ ->
            if (value == null) {
                Toast.makeText(
                    requireContext(),
                    "An error occured whilst trying to update user: value is null",
                    Toast.LENGTH_LONG
                ).show()

                return@addSnapshotListener
            }

            if (value.getString("img_url") == "") {
                binding.userAppbarHome.imageTintList = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.imageTint
                )

                binding.userAppbarHome.setImageResource(R.drawable.ic_account_circle)
            } else {
                binding.userAppbarHome.imageTintList = null
                Glide.with(requireContext())
                    .load(value.getString("img_url"))
                    .into(binding.userAppbarHome)
            }
        }
    }
}