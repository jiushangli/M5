package com.example.m5

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.m5.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        var musicListPA = ArrayList<Music>()
        var songPosition: Int = 0

        //?的含义是可以为空,!的含义是非空断言,!!的含义是非空断言,如果为空就抛出异常
        var musicService: MusicService? = null
        var isPlaying: Boolean = false

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var fIndex: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolWhite)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.data?.scheme.contentEquals("content")) {
            val intentService = Intent(this, MusicService::class.java)
            //绑定服务,其中BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            Glide.with(this)
                .load(getImgArt(musicListPA[songPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.music_player).centerCrop())
                .into(binding.songImgPA)
            binding.songNamePA.text = musicListPA[songPosition].title
            binding.artistPA.text = musicListPA[songPosition].artist

        } else
            initializeLayout()

        binding.playPauseBtnPA.setOnClickListener() {
            if (isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }
        binding.previousBtnPA.setOnClickListener() {
            preNextSong(false)
        }
        binding.nextBtnPA.setOnClickListener() {
            preNextSong(true)
        }
        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            //当拖动条开始拖动的时候调用,Unit是kotlin中的空类型,类似于java中的void
            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
        binding.repeatBtnPA.setOnClickListener() {
            if (!repeat) {
                repeat = true
                binding.repeatBtnPA.setImageResource(R.drawable.repeat_one_icon)
            } else {
                repeat = false
                binding.repeatBtnPA.setImageResource(R.drawable.repeat_icon)
            }
        }
 /*       binding.equalizerBtnPA.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService!!.mediaPlayer!!.audioSessionId
                )
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            } catch (e: Exception) {
                Toast.makeText(this, "似乎不支持音效", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }*/
        binding.timerBtnPA.setOnClickListener {
            val timer = min15 || min30 || min60
            //如果没有设置定时器,就弹出设置定时器的界面
            if (!timer)
                showBottomSheetDialog()
            //如果设置了定时,就询问是否要取消
            else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("取消定时")
                    .setMessage("你希望取消定时吗?")
                    .setPositiveButton("那是自然") { _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.timerBtnPA.setColorFilter(
                            ContextCompat.getColor(
                                this,
                                R.color.blue
                            )
                        )
                    }
                    .setNegativeButton("手滑了") { dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
            }
        }
        //分享音乐文件
        binding.shareBtnPA.setOnClickListener() {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "分享音乐文件给你的朋友"))
        }

        //收藏喜欢音乐
        binding.favouriteBtnPA.setOnClickListener {
            if (isFavourite) {
                isFavourite = false
                binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)
                FavouriteActivity.favouriteSongs.removeAt(fIndex)
            } else {
                isFavourite = true
                binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
                FavouriteActivity.favouriteSongs.add(musicListPA[songPosition])
            }
        }
    }

    private fun getMusicDetails(contentUri: Uri): Music {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }
            return Music(
                id = "Unknown",
                title = path.toString(),
                artist = "Unknown",
                album = "Unknown",
                duration = duration!!,
                path = path.toString(),
                artUri = "Unknown"
            )
        } finally {

        }

    }

    //填充界面的图片以及歌曲名称
    private fun setLayout() {
        fIndex = favouriteChecker(musicListPA[songPosition].id)
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player).centerCrop())
            .into(binding.songImgPA)
        binding.songNamePA.text = musicListPA[songPosition].title
        binding.artistPA.text = musicListPA[songPosition].artist
        if (repeat) binding.repeatBtnPA.setImageResource(R.drawable.repeat_one_icon)
        else binding.repeatBtnPA.setImageResource(R.drawable.repeat_icon)
        if (min15 || min30 || min60)
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.bordeaux_red))
        if (isFavourite) binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
        else binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)

    }

    //创建播放器
    private fun createMediaPlayer() {
        try {
            //如果为空就创建一个,在什么时候为空呢,在第一次进入的时候为空
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            //这里的!!代表非空断言
            //这里在设置播放器
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            //替换播放按钮
            binding.playPauseBtnPA.setBackgroundResource(R.drawable.pause_icon)
            musicService!!.showNotification(if (isPlaying) R.drawable.pause_icon else R.drawable.play_icon)
            //这是进度条两端的文字时间进度
            binding.tvSeekBarStart.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text =
                formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            //这是进度条的小圆点
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id

        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }

    private fun initializeLayout() {
        //获取传递过来的数据,其中index为歌曲在列表中的位置,默认为0
        songPosition = intent.getIntExtra("index", 0)
        //获取传递过来的数据,其中class为传递过来的类名
        when (intent.getStringExtra("class")) {
            "FavouriteAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                //绑定服务,其中BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                setLayout()
            }

            "NowPlaying" -> {
                setLayout()
                binding.tvSeekBarStart.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) binding.playPauseBtnPA.setImageResource(R.drawable.pause_icon)
                else binding.playPauseBtnPA.setImageResource(R.drawable.play_icon)
            }
            //根据传递过来的类名,选择不同的列表
            "MusicAdapterSearch" -> {
                //绑定服务
                val intent = Intent(this, MusicService::class.java)
                //绑定服务,其中BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }

            "MusicAdapter" -> {
                //绑定服务
                val intent = Intent(this, MusicService::class.java)
                //绑定服务,其中BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }

            "MainActivity" -> {
                //绑定服务
                val intent = Intent(this, MusicService::class.java)
                //绑定服务,其中BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                //说明是高贵的主界面的随机按钮传过来的
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }

            "FavouriteShuffle" -> {
                val intent = Intent(this, MusicService::class.java)
                //绑定服务,其中BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                //说明是高贵的主界面的随机按钮传过来的
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                musicListPA.shuffle()
                setLayout()
            }

            "PlaylistDetailsAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                //绑定服务,其中BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                //说明是高贵的主界面的随机按钮传过来的
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                setLayout()
            }

            "PlaylistDetailsShuffle" -> {
                val intent = Intent(this, MusicService::class.java)
                //绑定服务,其中BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                //说明是高贵的主界面的随机按钮传过来的
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }

    private fun playMusic() {
        binding.playPauseBtnPA.setImageResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic() {
        binding.playPauseBtnPA.setImageResource(R.drawable.play_icon)
        musicService!!.showNotification(R.drawable.play_icon)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun preNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        } else {
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    //为什么要用service的方式,这是为了让音乐播放器在后台运行,而不是在前台运行
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        //定义了一个 MusicService.MyBinder 的对象
        val binder = service as MusicService.MyBinder
        //通过 MyBinder 对象获取到 MusicService 的实例
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()
        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager!!.requestAudioFocus(
            musicService,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(p0: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || resultCode == RESULT_OK) {
            return
        }
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(baseContext, "15分钟之后播放结束", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.bordeaux_red))
            min15 = true
            Thread {
                Thread.sleep(15 * 60000)
                if (min15) exitApplication()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(baseContext, "30分钟之后播放结束", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.bordeaux_red))
            min30 = true
            Thread {
                Thread.sleep(30 * 60000)
                if (min30) exitApplication()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(baseContext, "60分钟之后播放结束", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.bordeaux_red))
            min60 = true
            Thread {
                Thread.sleep(60 * 60000)
                if (min60) exitApplication()
            }.start()
            dialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPA[songPosition].id == "Unknown" && !isPlaying) exitApplication()
    }
}