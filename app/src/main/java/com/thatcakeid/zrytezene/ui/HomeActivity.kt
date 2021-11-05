package com.thatcakeid.zrytezene.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.thatcakeid.zrytezene.databinding.ActivityHomeBinding
import com.thatcakeid.zrytezene.databinding.SheetFpuBinding

class HomeActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }
    private val fpuBinding by lazy { SheetFpuBinding.inflate(layoutInflater) }

    private val bottomSheetBehavior: BottomSheetBehavior<*> by lazy {
        BottomSheetBehavior
            .from(fpuBinding.sheetRoot)
            .also {
                it.state = BottomSheetBehavior.STATE_HIDDEN
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fpuBinding.arrowFpuCompactPlayer.setOnClickListener {
            bottomSheetBehavior.setState(
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                    BottomSheetBehavior.STATE_EXPANDED
                else
                    BottomSheetBehavior.STATE_COLLAPSED
            )
        }
    }
}