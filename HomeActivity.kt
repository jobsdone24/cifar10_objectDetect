package com.example.choison_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.choison_project.databinding.ActivityHomeBinding
import com.example.choison_project.databinding.ActivityMainBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        binding.galleryBtn.setOnClickListener {
            Intent(this,GalleryActivity::class.java).apply {
                startActivity(this)
            }
        }
        binding.realtimeBtn.setOnClickListener {
            Intent(this, RealtimeObjectActivity::class.java).apply {
                startActivity(this)
            }
        }
        binding.cifarBtn.setOnClickListener {
            Intent(this, Cifar10::class.java).apply {
                startActivity(this)
            }
        }
    }
}