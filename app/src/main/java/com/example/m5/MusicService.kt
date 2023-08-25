package com.example.m5

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null

    //MediaSessionCompat是一个用于与媒体控制器进行通信的类
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder? {
        //创建一个MediaSessionCompat对象,baseContext是上下文,My Music是标签
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    // 作用是返回当前服务
    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    fun showNotification(playPauseBtn: Int, playbackSpeed: Float) {
        val intent = Intent(baseContext, MainActivity::class.java)
        intent.putExtra("index", PlayerActivity.songPosition)
        intent.putExtra("class", "NowPlaying")
        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        //这里的flag实现了什么功能? 不同的安卓版本有不同的实现方式
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        //PendingIntent是一种特殊的Intent,它允许其他应用程序执行您的应用程序的操作,通过广播实现从通知栏影响本地程序
        val prevIntent = Intent(
            baseContext,
            NotificationReceiver::class.java
        ).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, flag)

        val playIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)

        val nextIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, flag)

        val exitIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)

        val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.moni1)
        }

        val notification =
            androidx.core.app.NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
                .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
                .setSmallIcon(R.drawable.music_icon)
                .setLargeIcon(image)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                )
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_skip_previous, "Previous", prevPendingIntent)
                .addAction(playPauseBtn, "Play", playPendingIntent)
                .addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        mediaPlayer!!.duration.toLong()
                    )
                    .build()
            )
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING,mediaPlayer!!.currentPosition.toLong(),playbackSpeed)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build()
            )
        }
        startForeground(13, notification)
    }

    fun createMediaPlayer() {
        try {
            //如果为空就创建一个,在什么时候为空呢,在第一次进入的时候为空
            if (PlayerActivity.musicService!!.mediaPlayer == null)
                PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
            //这里的!!代表非空断言
            //这里在设置播放器
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            //替换播放按钮
            PlayerActivity.binding.playPauseBtnPA.setBackgroundResource(R.drawable.pause_icon)
            PlayerActivity.musicService!!.showNotification(if
                    (PlayerActivity.isPlaying) R.drawable.pause_icon else R.drawable.play_icon,0F)
            //这是进度条两端的文字时间进度
            PlayerActivity.binding.tvSeekBarStart.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarEnd.text =
                formatDuration(mediaPlayer!!.duration.toLong())
            //这是进度条的小圆点
            PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration
            PlayerActivity.binding.seekBarPA.progress = 0
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id

        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }

    fun seekBarSetup() {
        runnable = Runnable {
            PlayerActivity.binding.tvSeekBarStart.text =
                formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 1000)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0) {
            //暂停音乐
            PlayerActivity.binding.playPauseBtnPA.setBackgroundResource(R.drawable.play_icon)
            NowPlaying.binding.playPauseBtnNP.setImageResource(R.drawable.play_icon)
            showNotification(R.drawable.play_icon,0F)
            PlayerActivity.isPlaying = false
            mediaPlayer!!.pause()
        } else {
            //继续音乐
            PlayerActivity.binding.playPauseBtnPA.setBackgroundResource(R.drawable.ic_pause)
            NowPlaying.binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon)
            showNotification(R.drawable.pause_icon,1F)
            PlayerActivity.isPlaying = true
            mediaPlayer!!.start()
        }
    }
}