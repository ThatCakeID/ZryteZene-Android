package com.thatcakeid.zrytezene

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.thatcakeid.zrytezene.databinding.ActivityDebugBinding

class DebugActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityDebugBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view: View = binding.root
        setContentView(view)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        AlertDialog.Builder(this)
            .setTitle("App crashed")
            .setMessage(intent.getStringExtra("error"))
            .setPositiveButton("Exit") { _, _ -> finish() }
            .create().show()
    }
}