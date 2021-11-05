package com.thatcakeid.zrytezene.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.databinding.FragmentHomeBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding: FragmentHomeBinding by viewBinding(FragmentHomeBinding::bind)
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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
    }
}