package com.example.m5

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.m5.databinding.ActivityMainBinding
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter

    companion object {
        var musicListMA = ArrayList<Music>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (requestRuntimePermission()) {
            initializeLayout()
        }

        binding.shuffleBtn.setOnClickListener {
            //点击随机播放跳到播放界面
            startActivity(
                Intent(
                    this@MainActivity,
                    PlayerActivity::class.java
                ).setAction("your.custom.action")
            )
        }

        binding.favouriteBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    FavouriteActivity::class.java
                ).setAction("your.custom.action")
            )
        }

        binding.playlistBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    PlaylistActivity::class.java
                ).setAction("your.custom.action")
            )
        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navFeedback -> {
                    Toast.makeText(baseContext, "屁眼子", Toast.LENGTH_SHORT).show()
                }

                R.id.navSettings -> {
                    Toast.makeText(baseContext, "屁眼子", Toast.LENGTH_SHORT).show()
                }

                R.id.navAbout -> {
                    Toast.makeText(baseContext, "屁眼子", Toast.LENGTH_SHORT).show()
                }

                R.id.navExit -> {
                    exitProcess(1)
                }
            }
            true
        }
    }

    //For requesting permission
    private fun requestRuntimePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )
                return false
            }
        }
        //android 13 permission request
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),
                    13
                )
                return false
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                initializeLayout()
            } else
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initializeLayout() {
        //重写动态获取储存权限
        setTheme(R.style.Theme_M5)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //for nav drawer
        // 创建 ActionBarDrawerToggle 对象，将其与 DrawerLayout 相关联
        toggle = ActionBarDrawerToggle(
            this, binding.root, R.string.open, R.string.close
        )
        // 将 ActionBarDrawerToggle 添加为 DrawerLayout 的侧滑菜单监听器
        binding.root.addDrawerListener(toggle)

        // 同步 ActionBarDrawerToggle 的状态
        toggle.syncState()

        // 启用导航按钮作为操作栏的一部分
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        musicListMA = getAllAudio()

        // 设置 RecyclerView 的固定大小以及缓存的项数，以优化性能
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        // 设置 RecyclerView 的布局管理器为线性布局管理器
        binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        // 创建 MusicAdapter 实例，并传入 MainActivity 和音乐列表作为参数
        musicAdapter = MusicAdapter(this@MainActivity, musicListMA)
        // 将 MusicAdapter 设置为 RecyclerView 的适配器
        binding.musicRV.adapter = musicAdapter

        binding.totalSongs.text = "   一共" + musicAdapter.itemCount + "首歌"
    }

    @SuppressLint("Recycle", "Range")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getAllAudio(): ArrayList<Music> {
        val tempList = ArrayList<Music>()
        //这是一个筛选条件，表示只查询音乐文件
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        // 创建一个包含需要查询的媒体库音乐信息的投影数组
        val projection = arrayOf(
            MediaStore.Audio.Media._ID, // 音乐文件ID
            MediaStore.Audio.Media.TITLE, // 音乐标题
            MediaStore.Audio.Media.ALBUM, // 音乐专辑
            MediaStore.Audio.Media.ARTIST, // 音乐艺术家
            MediaStore.Audio.Media.DURATION, // 音乐时长
            MediaStore.Audio.Media.DATE_ADDED, // 音乐添加日期
            MediaStore.Audio.Media.DATA, // 音乐文件路径
            MediaStore.Audio.Media.ALBUM_ID // 音乐文件路径

        )
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
            selection, null, MediaStore.Audio.Media.DATE_ADDED + " DESC", null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val id =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val title =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val album =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val artist =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val path =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val ablumIdC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    //通过album_id找到专辑封面图片uri
                    val artUriC = Uri.withAppendedPath(uri, ablumIdC).toString()
                    val music = Music(id, title, album, artist, duration, path, artUriC)
                    val file = File(path)
                    if (file.exists()) {
                        tempList.add(music)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return tempList
    }


}