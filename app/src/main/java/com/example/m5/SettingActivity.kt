package com.example.m5

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.m5.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.leaves)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "设置"
        when (MainActivity.themeIndex) {
            0 -> binding.coolCyanTheme.setBackgroundColor(Color.WHITE)
            1 -> binding.coolRedTheme.setBackgroundColor(Color.WHITE)
            2 -> binding.coolGreenTheme.setBackgroundColor(Color.WHITE)
            3 -> binding.coolBlueTheme.setBackgroundColor(Color.WHITE)
            4 -> binding.coolBlackTheme.setBackgroundColor(Color.WHITE)
        }

        binding.coolCyanTheme.setOnClickListener { saveTheme(0) }
        binding.coolRedTheme.setOnClickListener { saveTheme(1) }
        binding.coolGreenTheme.setOnClickListener { saveTheme(2) }
        binding.coolBlueTheme.setOnClickListener { saveTheme(3) }
        binding.coolBlackTheme.setOnClickListener { saveTheme(4) }
//        binding.versionName.text = setVersionDetails()
        //用于排序到时候用得上
/*        binding.sortBtn.setOnClickListener {
            val menuList = arrayOf("最近添加", "按名称排序", "按时长排序")
            var currentSort = MainActivity.sortOrder

            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Sorting")
                .setPositiveButton("OK") { _, _ ->
                    val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("sortOrder", currentSort)
                    editor.apply()
                }
                .setSingleChoiceItems(menuList, currentSort) { _, which ->
                    currentSort = which
                }
            val customDialog = builder.create()
            customDialog.show()
        }*/
    }

    private fun saveTheme(index: Int) {
        if (MainActivity.themeIndex != index) {
            val editor = getSharedPreferences("THEMES", MODE_PRIVATE).edit()
            editor.putInt("themeIndex", index)
            editor.apply()
            //直接重启应用颜色
            exitApplication()
        }
    }

    private fun setVersionDetails(): String {
        return "Version Name: 1.0"
    }

}