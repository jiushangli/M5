package com.example.m5

import android.media.MediaMetadataRetriever
import java.io.File
import kotlin.system.exitProcess

data class Music(
    val id: String, val title: String, val album: String,
    val artist: String, val duration: Long = 0, val path: String, val artUri: String
)

class Playlist {
    lateinit var name: String
    lateinit var playlist: ArrayList<Music>
    lateinit var createdBy: String
    lateinit var createdOn: String
}

class MusicPlaylist {
    var ref: ArrayList<Playlist> = ArrayList()
}

//这个函数在整个项目都可以用吗? 答案是可以的
fun formatDuration(duration: Long): String {
    val minute = duration / 1000 / 60
    val second = duration / 1000 % 60
    return String.format("%02d:%02d", minute, second)
}

//从指定的音频文件路径中获取嵌入的图片数据,即专辑封面
fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    //如果不是单曲循环,那么就正常的播放下一首或者上一首
    if (!PlayerActivity.repeat) {
        if (increment) {
            if (PlayerActivity.songPosition == PlayerActivity.musicListPA.size - 1) {
                PlayerActivity.songPosition = 0
            } else PlayerActivity.songPosition++

        } else {
            if (PlayerActivity.songPosition == 0) {
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
            } else PlayerActivity.songPosition--
        }
    }
    //如果是单曲循环,那么就不改变songPosition的值
}

fun exitApplication() {
    if (PlayerActivity.musicService != null) {
        PlayerActivity.musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
        PlayerActivity.musicService!!.stopForeground(true)
//            PlayerActivity.musicService!!.stopSelf()
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null
    }
    exitProcess(1)
}

fun favouriteChecker(id: String): Int {
    PlayerActivity.isFavourite = false
    FavouriteActivity.favouriteSongs.forEachIndexed { index, music ->
        if (id == music.id) {
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1
}

fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music> {
    playlist.forEachIndexed { index, music ->
        val file = File(music.path)
        if (!file.exists())
            playlist.removeAt(index)
    }
    return playlist
}
