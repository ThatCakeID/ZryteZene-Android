package com.thatcakeid.zrytezene.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import com.thatcakeid.zrytezene.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setWatermarkColors(binding.textWatermark, binding.watermarkRoot)
    }
}