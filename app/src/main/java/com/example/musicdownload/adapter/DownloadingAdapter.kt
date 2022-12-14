package com.example.musicdownload.adapter

import android.content.Context
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.Settings.Secure.getString
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.Status
import com.example.musicdownload.R
import com.example.musicdownload.data.model.Music
import com.example.musicdownload.databinding.DownloadItemBinding
import com.example.musicdownload.view.activity.MainActivity

class DownloadingAdapter() : RecyclerView.Adapter<DownloadingViewHolder>() {
    companion object {
        var downloadIdOne : Int = 0
    }
    private var list : List<Music> = listOf()
    private lateinit var context: Context
    var dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    .toString() + "/DownloadList/"
    fun setDataDownloading(list: List<Music>,context: Context){
        this.list = list
        this.context = context
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DownloadItemBinding.inflate(inflater, parent, false)
        return DownloadingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadingViewHolder, position: Int) {
        var music = list[position]
        holder.binding.titleTextView.text = list[position].name
        holder.binding.tvArtistDownloading.text = list[position].artistName
        Glide.with(context).load(list[position].image)
            .error(R.drawable.demo_img_download)
            .into(holder.itemView.findViewById(R.id.imgItemDownload))
        holder.binding.actionButton.isEnabled = false
        holder.binding.progressBar.indeterminateDrawable.setColorFilter(
            Color.CYAN, android.graphics.PorterDuff.Mode.SRC_IN
        )
        downloadIdOne = PRDownloader.download(music.audioDownload, dirPath, music.name+".mp3")
            .build()
            .setOnStartOrResumeListener {
                holder.binding.progressBar.isIndeterminate = false
                holder.binding.actionButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            }
            .setOnPauseListener { holder.binding.actionButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24) }
            .setOnCancelListener {

            }
            .setOnProgressListener { progress ->
                val progressPercent = progress!!.currentBytes * 100 / progress!!.totalBytes
                holder.binding.progressBar.progress = progressPercent.toInt()
                holder.binding.progressTextView.text = holder.binding.progressBar.progress.toString()
                holder.binding.progressBar.isIndeterminate = false
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    MainActivity.listDownloading.remove(music)
                    MediaScannerConnection.scanFile(
                     context, arrayOf(dirPath+music.name+".mp3"), null, null
                 )
                }

                override fun onError(error: Error?) {
                    holder.binding.actionButton.setImageResource(R.drawable.ic_baseline_error_outline_24)
                    Toast.makeText(
                        context,
                         "Fail " + "1",
                        Toast.LENGTH_SHORT
                    ).show();
                }

            })
        holder.binding.actionButton.setOnClickListener {
            if (Status.RUNNING == PRDownloader.getStatus(downloadIdOne)) {
                PRDownloader.pause(downloadIdOne)
                return@setOnClickListener
            }
            if (Status.PAUSED == PRDownloader.getStatus(downloadIdOne)) {
                PRDownloader.resume(downloadIdOne);
                return@setOnClickListener
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}

class DownloadingViewHolder(val binding: DownloadItemBinding) : RecyclerView.ViewHolder(binding.root){

}