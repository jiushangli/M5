package com.example.m5

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.m5.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectionBinding
    private lateinit var adapter: MusicAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);  //透明状态栏
        binding.selectionRV.setItemViewCacheSize(10)
        binding.selectionRV.setHasFixedSize(true)
        binding.selectionRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, MainActivity.MusicListMA, selectionActivity = true)
        binding.selectionRV.adapter = adapter
        binding.backBtnSA.setOnClickListener { finish() }

        //搜索框监听
        binding.searchViewSA.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                MainActivity.musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for(song in MainActivity.MusicListMA){
                        if(song.title.lowercase().contains(userInput)){
                            MainActivity.musicListSearch.add(song)
                        }
                    }
                    MainActivity.search = true
                    //实时更新页面
                    adapter.updateMusicList(searchList = MainActivity.musicListSearch)
                }
                return true
            }
        })
    }
}