package com.thatcakeid.zrytezene.ui.startup

import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thatcakeid.zrytezene.databinding.FragmentUpdateBinding

class UpdateFragment : AppCompatActivity() {
    private val binding by lazy {
        FragmentUpdateBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setWatermarkColors(binding.textWatermark, binding.watermarkRoot)
    }
}