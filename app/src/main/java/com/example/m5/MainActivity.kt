package com.example.m5

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.m5.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter

    companion object {
        lateinit var MusicListMA: ArrayList<Music>
        lateinit var musicListSearch: ArrayList<Music>
        var search: Boolean = false
        var themeIndex: Int = 2
        val currentTheme = arrayOf(
            R.style.coolWhite,
            R.style.coolRed,
            R.style.coolGreen,
            R.style.coolBlue,
            R.style.coolBlack
        )
        val currentGradient = arrayOf(R.drawable.gradient_green)
        var sortOrder: Int = 0
        val sortingList = arrayOf(
            MediaStore.Audio.Media.DATE_ADDED + " DESC", MediaStore.Audio.Media.TITLE + " DESC",
            MediaStore.Audio.Media.DURATION + " DESC"
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        //调用 getInt 方法从 SharedPreferences 中获取名为 "themeIndex" 的整数值
        //如果 "themeIndex" 键不存在，则返回默认值 0
        themeIndex = themeEditor.getInt("themeIndex", 0)

        setTheme(R.style.coolBlue)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//         创建 ActionBarDrawerToggle 对象，将其与 DrawerLayout 相关联
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        // 将 ActionBarDrawerToggle 添加为 DrawerLayout 的侧滑菜单监听器
        binding.root.addDrawerListener(toggle)
        // 同步 ActionBarDrawerToggle 的状态
        toggle.syncState()
        // 启用导航按钮作为操作栏的一部分
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        if (requestRuntimePermission()) {

            initializeLayout()

            //通过缓存得到音乐列表以及喜欢的音乐
            FavouriteActivity.favouriteSongs = ArrayList()
            //取得我喜欢的列表
            val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
            val jsonString = editor.getString("FavouriteSongs", null)
            val typeToken = object : TypeToken<ArrayList<Music>>() {}.type
            if (jsonString != null) {
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
                FavouriteActivity.favouriteSongs.addAll(data)
            }

            PlaylistActivity.musicPlaylist = MusicPlaylist()
            //取得播放列表
            val jsonStringPlaylist = editor.getString("MusicPlaylist", null)
            if (jsonStringPlaylist != null) {
                val dataPlaylist: MusicPlaylist =
                    GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlaylist::class.java)
                PlaylistActivity.musicPlaylist = dataPlaylist
            }

        }

        //给设置按钮的绑定事件
        //从设置按钮跳到设置界面
        binding.setBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    SettingActivity::class.java
                ).setAction("your.custom.action")
            )
        }

        //给随机播放按钮的绑定事件
        //从随机播放按钮跳到播放界面,并用intent带去一些远方的信息
        binding.shuffleBtn.setOnClickListener {
            val intent = Intent(
                this@MainActivity,
                PlayerActivity::class.java
            ).setAction("your.custom.action")
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            //点击随机播放跳到播放界面
            startActivity(intent)
        }

        //给喜欢按钮的绑定事件
        //从喜欢按钮跳到喜欢的音乐列表
        binding.favouriteBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    FavouriteActivity::class.java
                ).setAction("your.custom.action")
            )
        }

        //给播放列表按钮的绑定事件
        //从播放列表按钮跳到播放列表
        binding.playlistBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    PlaylistActivity::class.java
                ).setAction("your.custom.action")
            )
        }

        //给左边已经打开后的东西设置绑定事件
  /*      binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navFeedback -> {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            FeedbackActivity::class.java
                        ).setAction("your.custom.action")
                    )
                }

                R.id.navSettings -> {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            SettingActivity::class.java
                        ).setAction("your.custom.action")
                    )
                }

                R.id.navAbout -> {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            AboutActivity::class.java
                        ).setAction("your.custom.action")
                    )
                }

                R.id.navExit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("天涯各自远")
                        .setMessage("你想要离开吗?")
                        .setPositiveButton("我意已决") { _, _ ->
                            exitApplication()
                        }
                        .setNegativeButton("回头是岸") { dialog, _ ->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()
//                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
                }
            }
            true
        }*/
    }

    //打开应用时获取权限的函数
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

    //这是一个回调函数吗?答案是:是的
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

    //当用户点击操作栏上的菜单项时，它会检查是否点击了抽屉菜单的开关图标
    // 如果是，则展开或关闭抽屉菜单；如果不是，则将事件传递给父类处理
/*    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)) {

            val window = window
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()
            return true
        }
        return super.onOptionsItemSelected(item)
    }*/

    //界面初始化函数
    @RequiresApi(Build.VERSION_CODES.R)
    private fun initializeLayout() {
        search = false
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        sortOrder = sortEditor.getInt("sortOrder", 0)
        MusicListMA = getAllAudio()
        // 设置 RecyclerView 的固定大小以及缓存的项数，以优化性能
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        // 设置 RecyclerView 的布局管理器为线性布局管理器
        binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        // 创建 MusicAdapter 实例，并传入 MainActivity 和音乐列表作为参数
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)

        // 将 musicAdapter 设置为 musicRV 的适配器
        binding.musicRV.adapter = musicAdapter
//        binding.totalSongs.text = "共" + musicAdapter.itemCount + "首"
    }

    //获取音乐列表的函数,查询本地音乐并且返回一个列表
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
            selection, null, sortingList[sortOrder], null
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

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
            exitApplication()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        //存储我喜欢的歌
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouriteActivity.favouriteSongs)
        editor.putString("FavouriteSongs", jsonString)
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)
        if (sortValue != sortOrder) {
            sortOrder = sortValue
            MusicListMA = getAllAudio()
            musicAdapter.updateMusicList(MusicListMA)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        //渐变处理
//        findViewById<LinearLayout>(R.id.linearLayoutNav)?.setBackgroundResource(currentGradient[themeIndex])

        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in MusicListMA) {
                        if (song.title.lowercase().contains(userInput)) {
                            musicListSearch.add(song)
                        }
                    }
                    search = true
                    //实时更新页面
                    musicAdapter.updateMusicList(searchList = musicListSearch)
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

}