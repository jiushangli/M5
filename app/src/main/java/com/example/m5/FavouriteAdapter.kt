package com.example.m5

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.m5.databinding.FavouriteViewBinding

class FavouriteAdapter(private val context: Context, private var musicList: ArrayList<Music>) :
    RecyclerView.Adapter<FavouriteAdapter.MyHolder>() {

    class MyHolder(binding: FavouriteViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.songImgFV
        val name = binding.songNameFV
        val artist = binding.songArtistFV
        val root = binding.root
        val duration = binding.songDuration
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteAdapter.MyHolder {
        return MyHolder(FavouriteViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: FavouriteAdapter.MyHolder, position: Int) {
        holder.name.text = musicList[position].title
        holder.artist.text = musicList[position].artist
        holder.duration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.moni2).centerCrop())
            .into(holder.image)

        holder.root.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java).setAction("your.custom.action")
            intent.putExtra("index", position)
            intent.putExtra("class", "FavouriteAdapter")
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateFavourites(newList: ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(newList)
        notifyDataSetChanged()
    }
}