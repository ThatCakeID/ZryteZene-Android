package com.thatcakeid.zrytezene

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.thatcakeid.zrytezene.databinding.ActivityDebugBinding

class DebugActivity : AppCompatActivity() {
    private var binding: ActivityDebugBinding? = null
    private var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebugBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        toolbar = binding!!.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar!!.setNavigationOnClickListener { v: View? -> onBackPressed() }
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("App crashed")
        alertDialog.setMessage(intent.getStringExtra("error"))
        alertDialog.setPositiveButton("Exit") { dialog: DialogInterface?, which: Int -> finish() }
        alertDialog.create().show()
    }
}