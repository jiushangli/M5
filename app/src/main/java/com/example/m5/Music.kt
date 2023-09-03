package com.example.m5

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
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

fun underBar(
    activity: Activity,
    drawable: Drawable,
    contentView: View
) {
    val old = activity.window.decorView.background
    activity.window.setBackgroundDrawable(drawable)
    contentView.background = old
}

fun transparentStatusBar(window: Window) {
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    var systemUiVisibility = window.decorView.systemUiVisibility
    systemUiVisibility =
        systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    window.decorView.systemUiVisibility = systemUiVisibility
    window.statusBarColor = Color.TRANSPARENT

    //设置状态栏文字颜色
//    setStatusBarTextColor(window, false)
}

fun setStatusBarTextColor(window: Window, light: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility = if (light) { //白色文字
            systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else { //黑色文字
            systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.decorView.systemUiVisibility = systemUiVisibility
    }
}

fun showItemSelectDialog(context: Context, position: Int) {
    val dialog = BottomSheetDialog(context)
    dialog.setContentView(R.layout.item_select_dialog)
    dialog.show()
    dialog.findViewById<RelativeLayout>(R.id.addPlaylist)?.setOnClickListener {
        Toast.makeText(context, "我来啦", Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }
    dialog.findViewById<RelativeLayout>(R.id.playNext)?.setOnClickListener {
        Toast.makeText(context, "我来啦", Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }
    dialog.findViewById<RelativeLayout>(R.id.deleteForever)?.setOnClickListener {
        Toast.makeText(context, "我来啦", Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }


    when (context) {
        is PlayerActivity -> {
//            dialog.findViewById<ShapeableImageView>(R.id.imageMV)?.setImageResource(R.drawable.yqhy)
            dialog.findViewById<ShapeableImageView>(R.id.imageMV)?.let {
                Glide.with(context)
                    .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                    .apply(RequestOptions().placeholder(R.drawable.moni1).centerCrop())
                    .into(it)
            }
            dialog.findViewById<TextView>(R.id.songNameISD)?.text =
                PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            dialog.findViewById<TextView>(R.id.songArtistISD)?.text =
                PlayerActivity.musicListPA[PlayerActivity.songPosition].artist
        }

        is FavouriteActivity -> {
            dialog.findViewById<ShapeableImageView>(R.id.imageMV)?.let {
                Glide.with(context)
                    .load(FavouriteActivity.favouriteSongs[position].artUri)
                    .apply(RequestOptions().placeholder(R.drawable.moni1).centerCrop())
                    .into(it)
            }
            dialog.findViewById<TextView>(R.id.songNameISD)?.text =
                FavouriteActivity.favouriteSongs[position].title
            dialog.findViewById<TextView>(R.id.songArtistISD)?.text =
                FavouriteActivity.favouriteSongs[position].artist

        }

        is PlaylistDetails -> {
            dialog.findViewById<ShapeableImageView>(R.id.imageMV)?.let {
                Glide.with(context)
                    .load(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist[position].artUri)
                    .apply(RequestOptions().placeholder(R.drawable.moni1).centerCrop())
                    .into(it)
            }
            dialog.findViewById<TextView>(R.id.songNameISD)?.text =
                PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist[position].title
            dialog.findViewById<TextView>(R.id.songArtistISD)?.text =
                PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist[position].artist
        }

        is MainActivity -> {
            dialog.findViewById<ShapeableImageView>(R.id.imageMV)?.let {
                Glide.with(context)
                    .load(MainActivity.MusicListMA[position].artUri)
                    .apply(RequestOptions().placeholder(R.drawable.moni1).centerCrop())
                    .into(it)
            }
            dialog.findViewById<TextView>(R.id.songNameISD)?.text =
                MainActivity.MusicListMA[position].title
            dialog.findViewById<TextView>(R.id.songArtistISD)?.text =
                MainActivity.MusicListMA[position].artist
        }
    }

}

fun updateFavourites(newList: ArrayList<Music>) {
    val musicList = ArrayList<Music>()
    musicList.addAll(newList)
}

