package com.example.m5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.m5.databinding.ActivityPlaylistBinding

class PlaylistActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityPlaylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.NoActionBar)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}