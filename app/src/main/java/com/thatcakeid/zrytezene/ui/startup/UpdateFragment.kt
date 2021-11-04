package com.thatcakeid.zrytezene.ui.startup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.databinding.FragmentUpdateBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class UpdateFragment : Fragment(R.layout.fragment_update) {
    private val binding: FragmentUpdateBinding by viewBinding(FragmentUpdateBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}