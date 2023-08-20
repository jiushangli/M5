package com.example.m5

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.m5.databinding.PlaylistViewBinding

class PlaylistViewAdapter(private val context: Context, private var playlistList: ArrayList<Playlist>) :
    RecyclerView.Adapter<PlaylistViewAdapter.MyHolder>() {

    class MyHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.playlistImg
        val name = binding.playlistName
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewAdapter.MyHolder {
        return MyHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: PlaylistViewAdapter.MyHolder, position: Int) {
        holder.name.text = playlistList[position].name
        holder.name.isSelected = true
        //用来删除歌单
/*        holder.delete.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(playlistList[position].name)
                .setMessage("你想要删除当前歌单吗?")
                .setPositiveButton("我意已决") { dialog, _ ->
                    PlaylistActivity.musicPlaylist.ref.removeAt(position)
                    refreshPlaylist()
                        dialog.dismiss()
                }
                .setNegativeButton("缓缓回头") { dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
        }*/
        holder.root.setOnClickListener {
            val intent = Intent(context, PlaylistDetails::class.java).setAction("your.custom.action")
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }
        if(PlaylistActivity.musicPlaylist.ref[position].playlist.size>0){
            Glide.with(context)
                .load(PlaylistActivity.musicPlaylist.ref[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_player).centerCrop())
                .into(holder.image)
        }
    }

    override fun getItemCount(): Int {
        return playlistList.size
    }

    fun refreshPlaylist(){
        playlistList = ArrayList()
        playlistList.addAll(PlaylistActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }


}