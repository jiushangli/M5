package com.example.m5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.m5.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolBlue)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "关于"
        binding.aboutText.text = aboutText()
    }

    private fun aboutText(): String {
        return "这是一个简单的音乐播放器\n" +
                "作者: 杨青花一\n"
    }

}