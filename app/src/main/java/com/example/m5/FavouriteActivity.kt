package com.example.m5

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.m5.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouriteBinding
    private lateinit var adapter: FavouriteAdapter

    companion object {
        var favouriteSongs: ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolRed)
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //检查并排除已经不存在的音乐
        favouriteSongs = checkPlaylist(favouriteSongs)

        binding.favouriteRV.setHasFixedSize(true)
        binding.favouriteRV.setItemViewCacheSize(13)
        // 设置 RecyclerView 的布局管理器为线性布局管理器
        binding.favouriteRV.layoutManager = LinearLayoutManager(this)
        // 创建 MusicAdapter 实例，并传入 MainActivity 和音乐列表作为参数
        adapter = FavouriteAdapter(this, favouriteSongs)

        // 将 musicAdapter 设置为 musicRV 的适配器
        binding.favouriteRV.adapter = adapter
        if (favouriteSongs.size == 0) binding.shuffleBtnFA.visibility = View.INVISIBLE
        binding.shuffleBtnFA.setOnClickListener {
            val intent = Intent(
                this,
                PlayerActivity::class.java
            ).setAction("your.custom.action")
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavouriteShuffle")
            //点击随机播放跳到播放界面
            startActivity(intent)
        }
    }
}